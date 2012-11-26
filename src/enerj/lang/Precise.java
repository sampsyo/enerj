package enerj.lang;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * Type qualifier for precise values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TypeQualifier
@DefaultQualifierInHierarchy
@SubtypeOf({})
public @interface Precise {
}
