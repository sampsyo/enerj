package enerj.lang;

import java.lang.annotation.*;

import checkers.quals.*;

/**
 * Type qualifier for approximate values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TypeQualifier
@SubtypeOf({})
public @interface Approx {
}
