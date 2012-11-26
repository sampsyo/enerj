package enerj.instrument;

import enerj.PrecisionChecker;
import enerj.lang.Approx;
import enerj.lang.Context;

import com.sun.source.util.TreePath;
import javax.annotation.processing.ProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import java.util.Set;
import java.util.HashSet;

import checkers.runtime.instrument.ReferencingTranslator;
import checkers.types.AnnotatedTypeMirror;

// Tightly couples with the SimulationTranslator, this pass replaces all local
// variable references (and method parameters) with reference objects. This
// allows us to simulate pass-by-reference for instrumentation of local variable
// reads and writes.
public class PrecisionReferencingTranslator extends ReferencingTranslator<PrecisionChecker> {
    public PrecisionReferencingTranslator(PrecisionChecker checker,
                                 ProcessingEnvironment env,
                                 TreePath p) {
        super(checker, env, p);

        // Use our references (which include an "approx" flag) instead of the
        // provided reference class. Should change this eventually (FIXME).
        REFERENCE_CLASS = enerj.rt.Reference.class.getName();
    }

    // An *extremely hacky* way to make a few more trees behave approximately
    // than are those that annotated by the atypeFactory.
    protected static Set<JCTree> approxTrees = new HashSet<JCTree>();

    protected boolean isApprox(JCTree tree) {
        AnnotatedTypeMirror treeType = atypeFactory.getAnnotatedType(tree);
        if (treeType.hasAnnotation(Approx.class)) {
        	return true;
        } else if (treeType.hasAnnotation(Context.class)) {
        	return true; // TODO! Look up precision from runtime index.
        } else if (approxTrees.contains(tree)) {
        	return true;
        } else {
        	return false;
        }
    }

    @Override
    public JCTree.JCExpression createNewInitializer(JCTree.JCVariableDecl tree, JCTree.JCExpression boxedOldType,
            JCTree.JCExpression newType, JCTree.JCExpression init, boolean primitive) {
        // Was the old variable approximate?
        boolean approx = isApprox(tree);

        JCTree.JCExpression newInit = maker.NewClass(
            null,
            List.of(boxedOldType),
            newType,
            List.<JCTree.JCExpression>of(
                init,
                boolExp(approx),
                boolExp(primitive)
            ),
            null
        );

        return newInit;
    }
}
