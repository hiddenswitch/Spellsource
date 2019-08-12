package com.hiddenswitch.spellsource.concurrent.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.concurrent.SuspendableQueue;
import com.hiddenswitch.spellsource.util.Sync;
import io.atomix.core.queue.AsyncDistributedQueue;
import io.atomix.vertx.AtomixClusterManager;
import org.jetbrains.annotations.NotNull;

public class SuspendableAtomixLoopingPollQueue<V> implements SuspendableQueue<V> {
	private String name;
	private long pollingFrequencyMillis = 200L;

	public SuspendableAtomixLoopingPollQueue(String name) {
		this.name = name;
	}

	@Override
	@Suspendable
	public boolean offer(@NotNull V item) {
		AsyncDistributedQueue<V> queue;
		queue = getOrCreateQueue();

		if (queue == null) {
			return false;
		}

		return Sync.get(queue.offer(item));
	}

	@Suspendable
	private AsyncDistributedQueue<V> getOrCreateQueue() {
		AtomixClusterManager clusterManager = AtomixHelpers.getClusterManager();
		return Sync.get(clusterManager.atomix().<V>queueBuilder(name)
				.withProtocol(clusterManager.getProtocol())
				.withSerializer(clusterManager.createSerializer())
				.buildAsync()).async();
	}

	@Override
	@Suspendable
	public V poll(long timeoutMillis) throws InterruptedException, SuspendExecution {
		AsyncDistributedQueue<V> queue = getOrCreateQueue();

		if (queue == null) {
			return null;
		}

		// Check once immediately
		V v = Sync.get(queue.poll());
		if (v != null) {
			return v;
		}

		while (timeoutMillis > 0) {
			v = Sync.get(queue.poll());
			if (v == null) {
				long then = System.currentTimeMillis();
				Strand.sleep(Math.min(timeoutMillis, pollingFrequencyMillis));
				timeoutMillis -= System.currentTimeMillis() - then;
				continue;
			}
			return v;
		}
		return null;

	}

	@Override
	@Suspendable
	public void destroy() {
		AsyncDistributedQueue<V> queue = getOrCreateQueue();
		if (queue == null) {
			return;
		}
		Sync.get(queue.close());
	}

	@Override
	public void close() {
	}
}

