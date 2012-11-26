package enerj;

import java.util.Set;

import javax.lang.model.element.AnnotationMirror;

import checkers.basetype.BaseTypeVisitor;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;

import com.sun.source.tree.*;


public class PrecisionVisitor extends BaseTypeVisitor<PrecisionChecker> {
	public PrecisionVisitor(PrecisionChecker checker, CompilationUnitTree root) {
        super(checker, root);
    }

    /**
     * Make sure that @Approx annotated object types are approximable.
     */
    protected class ApproximabilityValidator extends BaseTypeVisitor<PrecisionChecker>.TypeValidator {
        @Override
        public Void visitDeclared(AnnotatedDeclaredType type, Tree node) {
        	// Is the class not approximatable but has an @Approx annotation?
        	if ((type.hasEffectiveAnnotation(checker.APPROX) ||
        	        type.hasEffectiveAnnotation(checker.CONTEXT)) &&
        	        !PrecisionChecker.isApproximable(type.getUnderlyingType())) {
        	    checker.report(Result.failure("type.invalid.unapproximable",
        	                                  type.getUnderlyingType().toString()), node);
        	}

            return super.visitDeclared(type, node);
        }
    }

    @Override
    public ApproximabilityValidator createTypeValidator() {
        return new ApproximabilityValidator();
    }


    /**
     * Validate a new object creation.
     *
     * @param node the new object creation.
     * @param p not used.
     */
    @Override
    public Void visitNewClass(NewClassTree node, Void p) {
        assert node != null;

        // Check for @Top as top-level modifier.
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node);
        if ( type.hasEffectiveAnnotation(checker.TOP) ) {
            // System.out.println("new got: " + type.toString());
            checker.report(Result.failure("new.top.forbidden"), node);
        }

        return super.visitNewClass(node, p);
    }

    @Override
    protected boolean checkConstructorInvocation(AnnotatedDeclaredType dt,
            AnnotatedExecutableType constructor, Tree src) {
    	// Ignore the default annotation on constructors
    	return true;
    }

    @Override
    protected boolean checkMethodInvocability(AnnotatedExecutableType method,
            MethodInvocationTree node) {
    	// Ignore the default annotation on methods
    	return true;
    }

    @Override
    public Void visitBinary(BinaryTree node, Void p) {
    	super.visitBinary(node, p);

    	// AnnotatedTypeMirror lhs = atypeFactory.getAnnotatedType(node.getLeftOperand());
    	// AnnotatedTypeMirror rhs = atypeFactory.getAnnotatedType(node.getRightOperand());

    	// TODO: is this the correct check? do we want subtypes or not?
    	// Disabling this check for now. We should allow binary operations on incompat.
    	// types (e.g., approx + precise). The resulting expression just becomes "tainted"
    	// approximate. --ALDS
    	/*
    	if (! (checker.getQualifierHierarchy().isSubtype(lhs.getAnnotations(), rhs.getAnnotations()) ||
    			checker.getQualifierHierarchy().isSubtype(rhs.getAnnotations(), lhs.getAnnotations()) ) ) {
    		checker.report(Result.failure("binary.type.incompatible", lhs, rhs), node);
    	}
    	*/

    	return null;
    }

	/**
     * Do not allow approximate data as the type of a conditional.
     * TODO: ensure that approximation gets propagated in comparisons!
     */
    private void conditionCheck(ExpressionTree condtree) {
    	AnnotatedTypeMirror cond = atypeFactory.getAnnotatedType(condtree);
    	Set<AnnotationMirror> condanns = cond.getAnnotations();

    	if (condanns.size() > 0 &&
    		!condanns.contains(checker.PRECISE) ) {
    		checker.report(Result.failure("condition.type.incompatible", cond), condtree);
    	}
    }

    @Override
    public Void visitIf(IfTree node, Void p) {
    	super.visitIf(node, p);
    	conditionCheck(node.getCondition());
    	return null;
    }

    @Override
    public Void visitDoWhileLoop(DoWhileLoopTree node, Void p) {
    	super.visitDoWhileLoop(node, p);
        conditionCheck(node.getCondition());
        return null;
    }

    @Override
    public Void visitWhileLoop(WhileLoopTree node, Void p) {
    	super.visitWhileLoop(node, p);
        conditionCheck(node.getCondition());
        return null;
    }

    @Override
    public Void visitForLoop(ForLoopTree node, Void p) {
    	super.visitForLoop(node, p);
    	if (node.getCondition() == null) {
    		return null;
    	}
        conditionCheck(node.getCondition());
        return null;
    }

    /* TODO: should we forbid iterating over approx arrays? Hmm, no.
    @Override
    public Void visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
        return super.visitEnhancedForLoop(node, p);
    } */

    @Override
    public Void visitConditionalExpression(ConditionalExpressionTree node, Void p) {
    	super.visitConditionalExpression(node, p);
        conditionCheck(node.getCondition());
        return null;
    }

    @Override
    public Void visitAssert(AssertTree node, Void p) {
    	super.visitAssert(node, p);
        conditionCheck(node.getCondition());
        return null;
    }

    // Ensure that array indices are precise.
    @Override
    public Void visitArrayAccess(ArrayAccessTree node, Void p) {
        super.visitArrayAccess(node, p);
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(
            node.getIndex());
        if ( !type.hasEffectiveAnnotation(checker.PRECISE) ) {
            checker.report(Result.failure("array.index.approx"),
                           node.getIndex());
        }
        return null;
    }

    // Moved here in Checker Framework 1.3.0.
    public boolean isValidUse(AnnotatedDeclaredType declarationType,
            AnnotatedDeclaredType useType) {
        return true;
    }
}
