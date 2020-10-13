package com.hiddenswitch.framework.impl;

import com.google.common.collect.MapMaker;
import io.vertx.core.Vertx;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class WeakVertxMap<T> {
	private final Function<Vertx, T> constructor;
	private final AtomicReference<T> reference = new AtomicReference<>();
	private final ConcurrentMap<Vertx, T> map = new MapMaker().weakKeys().concurrencyLevel(Runtime.getRuntime().availableProcessors()).initialCapacity(Runtime.getRuntime().availableProcessors()).makeMap();

	public WeakVertxMap(@NotNull Function<Vertx, T> constructor) {
		this.constructor = constructor;
	}

	public T get() {
		if (Vertx.currentContext() == null) {
			return reference.updateAndGet(existing -> {
				if (existing == null) {
					return constructor.apply(null);
				}
				return existing;
			});
		} else {
			return map.computeIfAbsent(Vertx.currentContext().owner(), constructor);
		}
	}
}
