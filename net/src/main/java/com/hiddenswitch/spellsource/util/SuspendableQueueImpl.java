package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.VertxException;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.Serializable;

import static io.vertx.ext.sync.Sync.awaitResult;

class SuspendableQueueImpl<V> implements SuspendableQueue<V> {
	private final String name;
	private final SuspendableMap<String, Node<V>> map;
	private final int capacity;
	private final Counter counter;
	private final SuspendableCondition notEmpty;
	private final SuspendableCondition notFull;

	SuspendableQueueImpl(String name, Counter counter, SuspendableMap<String, Node<V>> map, int capacity, SuspendableCondition notEmpty, SuspendableCondition notFull) {
		this.counter = counter;
		this.name = name;
		this.map = map;
		this.capacity = capacity;
		this.notEmpty = notEmpty;
		this.notFull = notFull;
	}

	@Override
	@Suspendable
	public boolean trySend(V item) {
		if (item == null) {
			throw new NullPointerException("item");
		}
		if (awaitResult(counter::get) == (long) capacity) {
			return false;
		}
		long c = -1;
		Node<V> node = new Node<V>(item);
		Lock putLock = lock("__putLock", Long.MAX_VALUE);
		map.put(node.id, node);
		try {
			if (awaitResult(counter::get) < capacity) {
				enqueue(node);
				c = awaitResult(h -> counter.getAndAdd(1L, h));
				if (c + 1 < capacity) {
					notFull.signal();
				}
			}
		} finally {
			putLock.release();
		}
		if (c == 0) {
			signalNotEmpty();
		}
		return c >= 0;
	}

	@Suspendable
	private void signalNotEmpty() {
		Lock takeLock = lock("__takeLock", Integer.MAX_VALUE);
		try {
			notEmpty.signal();
		} finally {
			takeLock.release();
		}
	}

	@Suspendable
	private void enqueue(Node<V> node) {
		Node<V> last = map.get("__last");
		last.next = node.id;
		map.put("__last", node);
		map.put(last.id, last);
	}

	@Suspendable
	public V receive(final long timeout) throws InterruptedException {
		V x = null;
		long c = -1L;
		long startTime = System.currentTimeMillis();
		long millis = timeout;
		Lock lock;
		try {
			lock = lock("__takeLock", timeout);
		} catch (VertxException timedOut) {
			return null;
		}

		millis -= System.currentTimeMillis() - startTime;
		try {
			while (awaitResult(counter::get) == 0L) {
				if (millis <= 0) {
					return null;
				}
				millis = notEmpty.awaitMillis(millis);
			}

			x = dequeue();
			c = awaitResult(h -> counter.getAndAdd(-1, h));
			if (c > 1) {
				notEmpty.signal();
			}
		} finally {
			lock.release();
		}
		if (c == capacity) {
			signalNotFull();
		}
		return x;
	}

	@Suspendable
	private void signalNotFull() {
		Lock putLock = lock("__putLock", Long.MAX_VALUE);
		try {
			notFull.signal();
		} finally {
			putLock.release();
		}
	}

	private Lock lock(String s, long maxValue) {
		return SharedData.lock(name + s, maxValue);
	}

	@Suspendable
	private V dequeue() {
		Node<V> h = map.remove("__head");
		map.remove(h.id);
		if (h.item != null) {
			throw new AssertionError("head.item != null");
		}
		Node<V> first = map.get(h.next);
		V x = first.item;
		first.item = null;
		map.put("__head", first);
		map.put(first.id, first);
		return x;
	}

	void init() throws SuspendExecution {
		SuspendableQueueImpl.Node<V> nullNode = new SuspendableQueueImpl.Node<>(null);

		map.put(nullNode.id, nullNode);
		map.put("__last", nullNode);
		map.put("__head", nullNode);
	}

	static class Node<V> implements Serializable {
		public String id;
		public String next;
		public V item;

		Node() {
		}

		public Node(V item) {
			this.id = RandomStringUtils.randomAlphanumeric(32);
			this.item = item;
		}
	}
}
