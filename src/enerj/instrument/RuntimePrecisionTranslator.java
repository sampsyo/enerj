package enerj.instrument;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;

import checkers.runtime.instrument.HelpfulTreeTranslator;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.util.ElementUtils;
import checkers.util.TreeUtils;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.util.List;

import enerj.PrecisionChecker;

// Adds calls to the runtime system to keep track of the dynamic precision
// state of each object as it is instantiated.
public class RuntimePrecisionTranslator extends HelpfulTreeTranslator<PrecisionChecker> {
    public RuntimePrecisionTranslator(PrecisionChecker checker,
                                      ProcessingEnvironment env,
                                      TreePath p) {
        super(checker, env, p);
    }

    @Override
    public void visitNewClass(JCNewClass tree) {
    	super.visitNewClass(tree);

    	if (tree.clazz instanceof JCTree.JCIdent) {
    		Symbol sym = ((JCTree.JCIdent)tree.clazz).sym;
    		if ((sym.flags() & Flags.ENUM) != 0) {
    			// Instantiating an enum. Don't instrument.
    			return;
    		}
    	}

    	/*
    	 * We transform object instantiations
    	 *
    	 *   new @Mod C();
    	 *
    	 * to
    	 *
    	 *   wrappedNew(
    	 *     PrecisionRuntime.impl.beforeCreation(this, @Mod==@Approx),
    	 *     new @Mod C(),
    	 *     this
    	 *   );
    	 *
    	 * where
    	 *
    	 *   <T> T wrappedNew(boolean before, T created, Object creator) {
    	 *     PrecisionRuntime.impl.afterCreation(creator, created);
    	 *     return created;
    	 *   }
    	 *
    	 * The call to beforeCreation needs to happen before the real "new".
    	 * Therefore, we use it as the first argument in wrappedNew.
    	 * The call to afterCreation can happen directly after the "new", but needs
    	 * access to the newly created object. Therefore, instead of also making it an
    	 * argument, we call afterCreation in wrappedNew.
    	 *
    	 * In a static environment, instead of "this" we use the current Thread as creator.
    	 */
    	MethodTree enclMeth = TreeUtils.enclosingMethod(path);
    	boolean envIsStatic;
    	if (enclMeth==null) {
    		envIsStatic = true;
    	} else {
    		envIsStatic = ElementUtils.isStatic(TreeUtils.elementFromDeclaration(enclMeth));
    	}

    	JCTree.JCExpression beforeMeth = dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.beforeCreation");
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);
    	JCExpression isApprox;
        if ( type.hasEffectiveAnnotation(checker.APPROX) ) {
        	isApprox = maker.Literal(TypeTags.BOOLEAN, 1);
        } else if ( type.hasEffectiveAnnotation(checker.CONTEXT) ) {
        	JCTree.JCExpression curIsApproxMeth = dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.isApproximate");
        	isApprox = maker.Apply(null, curIsApproxMeth, List.of(thisExp()));
        } else {
        	isApprox = maker.Literal(TypeTags.BOOLEAN, 0);
        }

        int[] sizes = PrecisionChecker.objectSizes(
    	    type, atypeFactory, typeutils, checker
    	);
    	JCTree.JCExpression preciseSizeExp = maker.Literal(sizes[0]);
    	JCTree.JCExpression approxSizeExp  = maker.Literal(sizes[1]);

    	List<JCExpression> beforeArgs;
    	if (envIsStatic) {
        	JCTree.JCExpression curThreadMeth = dotsExp("Thread.currentThread");
        	JCTree.JCMethodInvocation curThreadCall = maker.Apply(null, curThreadMeth, List.<JCExpression>nil());

    		beforeArgs = List.of(curThreadCall, isApprox,
    		                     preciseSizeExp, approxSizeExp);
    	} else {
    		beforeArgs = List.of(thisExp(), isApprox,
    		                     preciseSizeExp, approxSizeExp);

    	}

    	JCTree.JCMethodInvocation beforeCall = maker.Apply(null, beforeMeth, beforeArgs);

    	JCTree.JCExpression wrappedNewMeth = dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.wrappedNew");
    	List<JCExpression> wrappedArgs;
    	if (envIsStatic) {
        	JCTree.JCExpression curThreadMeth = dotsExp("Thread.currentThread");
        	JCTree.JCMethodInvocation curThreadCall = maker.Apply(null, curThreadMeth, List.<JCExpression>nil());

    		wrappedArgs = List.of(beforeCall, tree, curThreadCall);
    	} else {
    		wrappedArgs = List.of(beforeCall, tree, thisExp());
    	}

    	JCTree.JCMethodInvocation wrappedCall = maker.Apply(
    	    null, wrappedNewMeth, wrappedArgs);

    	attribute(wrappedCall, tree);

    	this.result = wrappedCall;
    }

    private void giveTypeToNewArray(JCTree.JCExpression elemtype,
                                    JCTree.JCNewArray newArray) {
        if (newArray.getInitializers() != null && newArray.elemtype == null) {
            newArray.elemtype = elemtype;

            // Recurse if multidimensional array.
            for (JCTree.JCExpression elem : newArray.getInitializers())
                if (elem instanceof JCTree.JCNewArray)
                    giveTypeToNewArray(
                        ((JCTree.JCArrayTypeTree)elemtype).elemtype,
                        (JCTree.JCNewArray)elem
                    );
        }
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl tree) {
        // Give explicit types to array literals.
        if (tree.init instanceof JCTree.JCNewArray)
            giveTypeToNewArray(((JCTree.JCArrayTypeTree)tree.vartype).elemtype,
                               (JCTree.JCNewArray)tree.init);
        super.visitVarDef(tree);
    }

    private final Set<JCTree.JCNewArray> subInits = new HashSet<JCTree.JCNewArray>();

    @Override
    public void visitNewArray(JCNewArray tree) {
        // Don't instrument array initializations inside of array
        // initialization literals.
        if (subInits.contains(tree)) {
            super.visitNewArray(tree);
            return;
        }
        if (tree.getInitializers() != null) {
            for (JCTree.JCExpression init : tree.getInitializers()) {
                if (init instanceof JCTree.JCNewArray) {
                    subInits.add((JCTree.JCNewArray)init);
                }
            }
        }

    	super.visitNewArray(tree);

    	// Translate array creations. We'll transform this expression:
    	//     new T[n]
    	// into this one:
    	//     enerj.rt.PrecisionRuntimeRoot.impl.newArray(
    	//          new T[n], 1, preciseElSize, approxElSize
        //     )

    	AnnotatedArrayType type =
    	    (atypeFactory.getAnnotatedType(tree));

    	// Recurse to true basic element type.
    	AnnotatedTypeMirror elType = type;
    	for (int i = 0; i < tree.dims.length(); ++i) {
    	    elType = ((AnnotatedArrayType)elType).getComponentType();
    	}
    	// ... and get the size of that element type.
        int[] sizes = PrecisionChecker.typeSizes(
            elType,
            true,
            checker
            // Get the approx-context size and switch to precise in precise
            // contexts (determined by call below).
        );

        JCTree.JCExpression isApprox;
        if ( elType.hasEffectiveAnnotation(checker.APPROX) ) {
        	isApprox = boolExp(true);
        } else if ( elType.hasEffectiveAnnotation(checker.CONTEXT) ) {
        	isApprox = maker.Apply(null,
        	    dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.isApproximate"),
        	    List.of(thisExp())
        	);
        } else {
        	isApprox = boolExp(false);
        }

    	JCTree.JCExpression call = maker.Apply(null,
    	    dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.newArray"),
    	    List.of(
    	        tree,
    	        maker.Literal(tree.dims.length()),
    	        isApprox,
    	        maker.Literal(sizes[0]),
    	        maker.Literal(sizes[1])
    	    )
    	);
    	attribute(call, tree);
    	result = call;
    }

    @Override
    public void visitMethodDef(JCMethodDecl tree) {
    	super.visitMethodDef(tree);

    	if (TreeUtils.isConstructor(tree)) {
    		/*
    		 * We transform constructor definitions by adding as first statement:
    		 *
    		 *   PrecisionRuntime.impl.enterConstructor( this );
    		 */
    		JCTree.JCExpression enterSel = dotsExp("enerj.rt.PrecisionRuntimeRoot.impl.enterConstructor");
    		JCTree.JCMethodInvocation enterCall = maker.Apply(null, enterSel, com.sun.tools.javac.util.List.of(thisExp()));
    		JCTree.JCStatement enterStmt = maker.Exec(enterCall);

    		// Start attribution of the new AST part
    		attr.attribStat(enterStmt, getAttrEnv(tree));

    		List<JCStatement> stmts = tree.body.getStatements();
    		JCStatement first = stmts.head;

    		/*
    		// Did I really need to make sure that the first statement is a constructor call??
    		if (first instanceof JCExpressionStatement) {
    			JCExpression exp = ((JCExpressionStatement)first).getExpression();
        		if (exp instanceof JCMethodInvocation) {
        			JCExpression rcv = ((JCMethodInvocation) exp).getMethodSelect();
        			if (rcv instanceof JCIdent) {
        				Name n = ((JCIdent) rcv).getName();
        				if (!names.init.equals(n)) {
        					// error
        				}
        			} else {
        				// error
        			}
        		} else {
        			// error
        		}
    		} else {
    			// error
    		}
    		*/

    		// Change the constructor body
    		tree.body = maker.Block(0, List.of(first).appendList(stmts.tail.prepend(enterStmt)));
    	}
    }
}
