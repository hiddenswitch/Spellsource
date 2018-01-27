package com.hiddenswitch.spellsource.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcOptions {
	/**
	 * What is the maximum amount of time to wait for a response across the event bus for this method call to reply?
	 *
	 * @return The delay in milliseconds.
	 */
	long sendTimeoutMS() default 8000L;

	/**
	 * What kind of serialization should this method use?
	 *
	 * @return {@link Serialization#JAVA} to use the Java runtime serialization, or {@link Serialization#JSON} to use
	 * the JSON serialization that is provided by Vertx (typically Jackson).
	 */
	Serialization serialization() default Serialization.JAVA;

	enum Serialization {
		JAVA,
		JSON
	}
}
