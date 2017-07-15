package com.hiddenswitch.proto3.net.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcOptions {
	/**
	 * What is the maximum amount of time to wait for a response across the event bus for this method call to reply?
	 *
	 * @return The delay in milliseconds.
	 */
	long sendTimeoutMS() default 8000L;

	Serialization serialization() default Serialization.JAVA;

	enum Serialization {
		JAVA,
		JSON
	}
}
