package com.hiddenswitch.diagnostics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or constructor for compile-time OpenTracing span weaving so that
 * the Java callstack and the distributed OpenTracing span hierarchy stay in 1:1 correspondence.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Traced {
	String value() default "";
}
