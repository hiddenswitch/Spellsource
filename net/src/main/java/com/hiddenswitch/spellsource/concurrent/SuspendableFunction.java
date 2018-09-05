package com.hiddenswitch.spellsource.concurrent;

import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.fibers.Suspendable;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

@FunctionalInterface
public interface SuspendableFunction<T, R> extends Serializable {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the function argument
	 * @return the function result
	 */
	@Suspendable
	R apply(T t) throws InterruptedException, SuspendExecution, InvocationTargetException, IllegalAccessException;

	/**
	 * Returns a function that always returns its input argument.
	 *
	 * @param <T> the type of the input and output objects to the function
	 * @return a function that always returns its input argument
	 */
	static <T> Function<T, T> identity() {
		return t -> t;
	}
}
