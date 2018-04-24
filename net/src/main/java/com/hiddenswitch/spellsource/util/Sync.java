package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableAction1;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Created by bberman on 2/15/17.
 */
public class Sync {
	@Suspendable
	public static <T> Handler<T> suspendableHandler(SuspendableAction1<T> handler) {
		FiberScheduler scheduler = io.vertx.ext.sync.Sync.getContextScheduler();
		return p -> new Fiber<Void>(scheduler, () -> handler.call(p)).start();
	}

	@Suspendable
	public static <R> R invoke(Supplier<R> func0) {
		return awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func0.get());
		}, false, h));
	}

	@Suspendable
	public static <T> void invoke0(Consumer<T> func1, T arg1) {
		Void res = awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			func1.accept(arg1);
			done.complete();
		}, false, h));
	}

	@Suspendable
	public static void invoke0(NoArgs func0) {
		Void res = awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			func0.apply();
			done.complete();
		}, false, h));
	}

	@Suspendable
	public static <T, R> R invoke(Function<T, R> func1, T arg1) {
		return awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func1.apply(arg1));
		}, false, h));
	}

	@Suspendable
	public static <T1, T2, R> R invoke(BiFunction<T1, T2, R> func2, T1 arg1, T2 arg2) {
		return awaitResult(h -> Vertx.currentContext().executeBlocking(done -> {
			done.complete(func2.apply(arg1, arg2));
		}, false, h));
	}
}
