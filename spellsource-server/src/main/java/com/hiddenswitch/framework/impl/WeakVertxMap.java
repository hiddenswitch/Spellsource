package com.hiddenswitch.framework.impl;

import io.vertx.core.Vertx;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class WeakVertxMap<T> {
	private final Function<Vertx, T> constructor;
	private final AtomicReference<T> reference = new AtomicReference<>();
	private final Map<Vertx, T> map = Collections.synchronizedMap(new WeakHashMap<>());

	public WeakVertxMap(@NotNull Function<Vertx, T> constructor) {
		this.constructor = constructor;
	}

	public synchronized T get() {
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
