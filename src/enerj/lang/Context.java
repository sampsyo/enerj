package enerj.lang;

import java.lang.annotation.*;
import checkers.quals.*;

/**
 * Type qualifier for polymorphic types that depend on the
 * instantiation of the surrounding class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@TypeQualifier
@SubtypeOf({})
public @interface Context {
}
