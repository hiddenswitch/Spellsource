package net.demilich.metastone.tests.util;

import java.util.concurrent.atomic.AtomicBoolean;

public class OverrideHandle<T> {
	public T object;
	public AtomicBoolean stopped = new AtomicBoolean(false);

	public void set(T object) {
		this.object = object;
	}

	public T get() {
		return object;
	}

	public void stop() {
		stopped.set(true);
	}
}
