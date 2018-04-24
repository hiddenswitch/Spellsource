package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Counter;

import static io.vertx.ext.sync.Sync.awaitResult;

public interface SuspendableQueue<V> {
	static <V> SuspendableQueue<V> create(String name, int capacity) throws SuspendExecution {
		Vertx vertx = Vertx.currentContext().owner();
		System.setProperty("vertx.hazelcast.async-api", "true");
		Counter counter = awaitResult(h -> vertx.sharedData().getCounter(name + "__counter", h));
		SuspendableMap<String, SuspendableLinkedQueue.Node<V>> map = SharedData.getClusterWideMap(name + "__nodes");
		SuspendableCondition notEmpty = SuspendableCondition.create(name + "__notEmpty");
		SuspendableCondition notFull = SuspendableCondition.create(name + "__notFull");
		SuspendableLinkedQueue<V> suspendableQueue = new SuspendableLinkedQueue<>(name, counter, map, capacity, notEmpty, notFull);
		suspendableQueue.init();
		return suspendableQueue;
	}

	@Suspendable
	boolean trySend(V item);

	V poll(long timeout) throws InterruptedException, SuspendExecution;

	@Suspendable
	default boolean offer(V item) {
		return trySend(item);
	}
}