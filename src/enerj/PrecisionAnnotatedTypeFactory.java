package enerj;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeKind;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;

import checkers.basetype.BaseTypeChecker;
import checkers.source.Result;
import checkers.types.AnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.AnnotatedTypeMirror.AnnotatedTypeVariable;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.types.TreeAnnotator;
import checkers.util.Pair;

/**
 * The annotated type factory for the precision checker.
 * We currently do not want automatic flow inference, so turn it off.
 */
public class PrecisionAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<PrecisionChecker> {

	public PrecisionAnnotatedTypeFactory(PrecisionChecker checker,
			CompilationUnitTree root) {
		// false turns off flow inference
		super(checker, root, false);
        postInit();
	}

	/**
     * The type of "this" is always "context" if its type is approximable. Otherwise it
     * is precise.
     */
    @Override
    public AnnotatedDeclaredType getSelfType(Tree tree) {
    	AnnotatedDeclaredType type = super.getSelfType(tree);

    	if (type!=null) {
    	    // Check whether the type of "this" is approximable.
    		type.clearAnnotations();
    		if (PrecisionChecker.isApproximable(type.getUnderlyingType())) {
        		type.addAnnotation(checker.CONTEXT);
    		} else {
        	    type.addAnnotation(checker.PRECISE);
    		}
    	}
        return type;
    }

    /**
     * Adapt the type of field accesses.
     */
    @Override
    protected void postAsMemberOf(AnnotatedTypeMirror type,
            AnnotatedTypeMirror owner, Element element) {
		assert type != null;
		assert owner != null;
		assert element != null;

    	if(type.getKind() != TypeKind.DECLARED &&
    		type.getKind() != TypeKind.ARRAY &&
    		!type.getKind().isPrimitive()) {
    		// nothing to do
    		return;
    	}
    	if(element.getKind() == ElementKind.LOCAL_VARIABLE) {
    		// the type of local variables also does not need to change
    		return;
    	}

    	/*
		System.out.println("\npostasmemberof:");
    	System.out.println("in type: " + type);
    	System.out.println("owner: " + owner);
    	System.out.println("element: " + element);
    	*/

		// Combine annotations
    	AnnotatedTypeMirror combinedType;
    	try {
    		combinedType = combineTypeWithType(owner, type);
    	} catch (ContextInTopAccessException exc) {
    		//TODO:
    		// It would have been nice to catch this this earlier so we can
    		// report exactly where the error occurred. To do that, we should
    		// eventually modify
    		// TypeFromTree$TypeFromExpression.visitMemberSelect
    		exc.report(element);
    		return;
    	}
		// System.out.println("combined type: " + combinedType);

		type.clearAnnotations();
		type.addAnnotations(combinedType.getAnnotations());
		// System.out.println("result: " + type.toString());

		// TODO: HACK: we needed to make setTypeArguments public to work
		// around a limitation of the framework.
		// If the method had a return value, like constructor/methodFromUse, we could probably get around this.

		if (type.getKind() == TypeKind.DECLARED
				&& combinedType.getKind() == TypeKind.DECLARED) {
			AnnotatedDeclaredType declaredType = (AnnotatedDeclaredType) type;
			AnnotatedDeclaredType declaredCombinedType = (AnnotatedDeclaredType) combinedType;
			declaredType.setTypeArguments(declaredCombinedType.getTypeArguments());
		} else if (type.getKind() == TypeKind.ARRAY
				&& combinedType.getKind() == TypeKind.ARRAY) {
			AnnotatedArrayType arrayType = (AnnotatedArrayType) type;
			AnnotatedArrayType arrayCombinedType = (AnnotatedArrayType) combinedType;
			arrayType.setComponentType(arrayCombinedType.getComponentType());
		}

		// System.out.println("out type: " + type);
    }

    private class ContextInTopAccessException extends Exception {
        public void report(Object tree) {
    		checker.report(Result.failure("access.top.context"), tree);
    	}
    }

	private AnnotatedTypeMirror combineTypeWithType(
			AnnotatedTypeMirror target, AnnotatedTypeMirror type) throws ContextInTopAccessException {

		AnnotationMirror targetam = target.getAnnotations().iterator().next();
        if (type.getAnnotations().isEmpty()) {
            // Type parameters (i.e. generics) have no annotations.
            return type;
        }
		AnnotationMirror typeam = type.getAnnotations().iterator().next();

		AnnotatedTypeMirror result = type.getCopy(true);

		// This is perhaps another framework bug: the annotations on a type
		// variable don't seem to be copied by type.getCopy(). This is also
		// unsound, but for now pass the type through unmodified.
		if (type instanceof AnnotatedTypeMirror.AnnotatedTypeVariable &&
				result.getAnnotations().isEmpty()) {
			return type;
		}

		// TODO: why is .equals directly not working?
		if (typeam.toString().equals(checker.CONTEXT.toString())) {

			// Don't allow @Top to substitute for @Context.
			if (targetam.toString().equals(checker.TOP.toString())) {
				throw new ContextInTopAccessException();
			}

			result.clearAnnotations();
			result.addAnnotation(targetam);
		}

		if (type.getKind() == TypeKind.DECLARED) {
            AnnotatedDeclaredType declaredType = (AnnotatedDeclaredType) result;

            if ( declaredType.getTypeArguments().size() > 0) {
            	Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mapping = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

            	// Get the combined type arguments
            	for (AnnotatedTypeMirror typeArgument : declaredType.getTypeArguments()) {
            		AnnotatedTypeMirror recTypeArgument = combineTypeWithType(target, typeArgument);
            		mapping.put(typeArgument, recTypeArgument);
            	}

            	// Construct result type
            	result = result.substitute(mapping);
            }
		}
		if (type.getKind() == TypeKind.ARRAY) {
            // Create a copy
            AnnotatedArrayType  arrayType = (AnnotatedArrayType) result;

            AnnotatedTypeMirror elemType = arrayType.getComponentType().getCopy(true);

            AnnotationMirror elemam = elemType.getAnnotations().iterator().next();

    		// TODO: why is .equals directly not working?
    		if (elemam.toString().equals(checker.CONTEXT.toString())) {

    			// Don't allow @Top to substitute for @Context.
    			if (targetam.toString().equals(checker.TOP.toString())) {
    				throw new ContextInTopAccessException();
    			}

    			elemType.clearAnnotations();
    			elemType.addAnnotation(targetam);
    			arrayType.setComponentType(elemType);
    		}
        }

		return result;
	}

	/**
	 * Adapt the types of constructors.
	 *
	 * @param tree the new class tree.
	 * @return the modified constructor.
	 */
	@Override
	public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> constructorFromUse(NewClassTree tree) {
		assert tree != null;

		Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> fromUse = super.constructorFromUse(tree);
		AnnotatedExecutableType constructor = fromUse.first;

		Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mappings = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

		// Get the result type
		AnnotatedTypeMirror resultType = getAnnotatedType(tree);

		// Modify parameters
		for (AnnotatedTypeMirror parameterType : constructor.getParameterTypes()) {
			AnnotatedTypeMirror combinedType;
			try {
				combinedType = combineTypeWithType(resultType, parameterType);
			} catch (ContextInTopAccessException e) {
				e.report(tree);
				return Pair.of(constructor, fromUse.second);
			}
			mappings.put(parameterType, combinedType);
		}

		// TODO: upper bounds, throws?

		constructor = constructor.substitute(mappings);

		Map<AnnotatedTypeVariable, AnnotatedTypeMirror> typeVarMapping =
		    atypes.findTypeArguments(tree);
		List<AnnotatedTypeMirror> typeargs = new LinkedList<AnnotatedTypeMirror>();

		if (!typeVarMapping.isEmpty()) {
		    for ( AnnotatedTypeVariable tv : constructor.getTypeVariables()) {
		        typeargs.add(typeVarMapping.get(tv));
		    }

		    constructor = constructor.substitute(typeVarMapping);
		}

		// System.out.println("adapted constructor: " + constructor);

		return Pair.of(constructor, typeargs);
	}

	/**
	 * Adapt the types of methods.
	 *
	 * @param tree the method invocation tree.
	 * @return the modified method invocation.
	 */
	@Override
	public Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> methodFromUse(MethodInvocationTree tree) {
		assert tree != null;

		Pair<AnnotatedExecutableType, List<AnnotatedTypeMirror>> mfuPair = super.methodFromUse(tree);
		AnnotatedExecutableType method = mfuPair.first;
		Map<AnnotatedTypeMirror, AnnotatedTypeMirror> mappings = new HashMap<AnnotatedTypeMirror, AnnotatedTypeMirror>();

		// Set the receiver
		AnnotatedTypeMirror receiverType = null;
		ExpressionTree exprTree = tree.getMethodSelect();
		if (exprTree.getKind() == Kind.MEMBER_SELECT) {
			MemberSelectTree memberSelectTree = (MemberSelectTree) exprTree;
			receiverType = getAnnotatedType(memberSelectTree.getExpression());
		} else {
			receiverType = getSelfType(tree);
		}
		assert receiverType != null;

		// Modify parameters
		for (AnnotatedTypeMirror parameterType : method.getParameterTypes()) {
			AnnotatedTypeMirror combined;
			try {
				combined = combineTypeWithType(receiverType, parameterType);
			} catch (ContextInTopAccessException e) {
				e.report(tree);
				return mfuPair;
			}
			mappings.put(parameterType, combined);
		}

		// Modify return type
		AnnotatedTypeMirror returnType = method.getReturnType();
		if (returnType.getKind() != TypeKind.VOID) {
			AnnotatedTypeMirror combinedType;
			try {
				combinedType = combineTypeWithType(receiverType, returnType);
			} catch (ContextInTopAccessException e) {
				e.report(tree);
				return mfuPair;
			}
			mappings.put(returnType, combinedType);
		}

		// TODO: upper bounds, throws?

		method = method.substitute(mappings);

		Map<AnnotatedTypeVariable, AnnotatedTypeMirror> typeVarMapping =
            atypes.findTypeArguments(tree);
        List<AnnotatedTypeMirror> typeargs = new LinkedList<AnnotatedTypeMirror>();

        if (!typeVarMapping.isEmpty()) {
            for ( AnnotatedTypeVariable tv : method.getTypeVariables()) {
                typeargs.add(typeVarMapping.get(tv));
            }

            method = method.substitute(typeVarMapping);
        }

		// System.out.println("adapted method: " + method);

		return Pair.of(method, typeargs);
	}

	@Override
    protected TreeAnnotator createTreeAnnotator(PrecisionChecker checker) {
        return new PrecisionTreeAnnotator(checker, this);
    }

	private class PrecisionTreeAnnotator extends TreeAnnotator {

		public PrecisionTreeAnnotator(BaseTypeChecker checker,
				AnnotatedTypeFactory typeFactory) {
			super(checker, typeFactory);
		}

		/**
		 * Propagate information from the lhs and rhs of binary operations.
		 */
	    @Override
	    public Void visitBinary(BinaryTree node, AnnotatedTypeMirror type) {
	    	super.visitBinary(node, type);

	    	AnnotatedTypeMirror lhs = PrecisionAnnotatedTypeFactory.this.getAnnotatedType(node.getLeftOperand());
	    	AnnotatedTypeMirror rhs = PrecisionAnnotatedTypeFactory.this.getAnnotatedType(node.getRightOperand());

	    	// Pointer equality is always precise.
	    	if ((lhs.getKind() == TypeKind.DECLARED || lhs.getKind() == TypeKind.NULL) &&
	    			(rhs.getKind() == TypeKind.DECLARED || rhs.getKind() == TypeKind.NULL)&&
	    			(node.getKind() == Kind.EQUAL_TO || node.getKind() == Kind.NOT_EQUAL_TO)) {
	    		type.clearAnnotations();
	    		type.addAnnotation(checker.PRECISE);
	    		return null;
	    	}

	    	if ( lhs.getAnnotations().contains(checker.TOP) ||
	    			rhs.getAnnotations().contains(checker.TOP) ) {
	    		type.clearAnnotations();
	    		type.addAnnotation(checker.TOP);
	    	} else if ( lhs.getAnnotations().contains(checker.APPROX) ||
	    			rhs.getAnnotations().contains(checker.APPROX) ) {
	    		type.clearAnnotations();
	    		type.addAnnotation(checker.APPROX);
	    	} else if ( lhs.getAnnotations().contains(checker.CONTEXT) ||
	    			rhs.getAnnotations().contains(checker.CONTEXT) ) {
	    		type.clearAnnotations();
	    		type.addAnnotation(checker.CONTEXT);
	    	}

	    	return null;
	    }

	    @Override
	    public Void visitClass(ClassTree node, AnnotatedTypeMirror type) {
	    	for(TypeParameterTree tpt : node.getTypeParameters()) {
	    		if (tpt.getBounds().isEmpty()) {
	    			// No bounds.
	    			continue;
	    		}
	    		visitTypeParameter(tpt, fromTypeTree(tpt.getBounds().get(0)));
	    	}
	    	return super.visitClass(node, type);
	    }

	    @Override
	    public Void visitTypeParameter(TypeParameterTree node, AnnotatedTypeMirror type) {
	    	if (!type.isAnnotated()) {
	    		type.addAnnotation(checker.TOP);
	    	}
	    	return super.visitTypeParameter(node, type);
	    }

	}
}
