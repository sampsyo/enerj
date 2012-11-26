package enerj.lang;

import java.lang.annotation.*;

import com.sun.source.tree.TypeParameterTree;

import checkers.quals.*;

/**
 * The top of the type hierarchy.
 * 
 * TODO: usable for programmers or not? For reference types maybe?
 */
@Retention(RetentionPolicy.RUNTIME)
//@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TypeQualifier
@SubtypeOf({})
@ImplicitFor(treeClasses={TypeParameterTree.class})
public @interface Top {
}
