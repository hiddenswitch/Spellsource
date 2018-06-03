package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.VertxException;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import static io.vertx.ext.sync.Sync.awaitResult;

class SuspendableLinkedQueue<V> implements SuspendableQueue<V> {
	private static Logger logger = LoggerFactory.getLogger(SuspendableLinkedQueue.class);
	private final String name;
	private final SuspendableMap<String, Node<V>> map;
	private final int capacity;
	private final Counter counter;
	private final SuspendableCondition notEmpty;
	private final SuspendableCondition notFull;

	SuspendableLinkedQueue(String name, Counter counter, SuspendableMap<String, Node<V>> map, int capacity, SuspendableCondition notEmpty, SuspendableCondition notFull) {
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
		logger.trace("trySend {} {}: Enter", name, item);
		if (item == null) {
			throw new NullPointerException("item");
		}
		long c = -1L;
		Lock putLock = lock("__putLock", Long.MAX_VALUE);
		try {
			Long counter1 = awaitResult(h -> counter.get(h));
			if (counter1 == capacity) {
				logger.trace("trySend {} {}: At capacity", name, item);
				return false;
			}
			Node<V> node = new Node<V>(item);

			logger.trace("trySend {} {}: Node put", name, item);
			long expected = counter1;
			while (expected < capacity) {
				long finalExpected = expected;
				Boolean incremented = awaitResult(h -> counter.compareAndSet(finalExpected, finalExpected + 1, h));
				if (!incremented) {
					expected += 1;
					continue;
				}

				map.put(node.id, node);
				enqueue(node);
				logger.trace("trySend {} {}: Enqueued", name, item);
				c = finalExpected;
				if (c + 1 < capacity) {
					notFull.signal();
				}
				break;
			}
		} finally {
			putLock.release();
		}
		if (c == 0) {
			signalNotEmpty();
			logger.trace("trySend {} {}: Signaled not empty", name, item);
		}
		return c >= 0;
	}

	@Suspendable
	private void signalNotEmpty() {
//		Lock takeLock = lock("__takeLock", Integer.MAX_VALUE);
//		try {
			notEmpty.signal();
//		} finally {
//			takeLock.release();
//		}
	}

	@Suspendable
	private void enqueue(Node<V> node) {
		Node<V> last = map.get("__last");
		last.next = node.id;
		map.put("__last", node);
		// This was the head element, so update it too
		if (last.item == null) {
			map.put("__head", last);
		}
		map.put(last.id, last);
	}

	@Suspendable
	public V poll(long timeout) throws InterruptedException, SuspendExecution {
		logger.trace("poll {}: Enter", name);
		V x = null;
		long c = -1L;
		long startTime = System.currentTimeMillis();
		long millis = timeout;
		Lock takeLock;
		try {
			takeLock = lock("__takeLock", timeout);
			logger.trace("receive {}: Taking lock", name);
		} catch (VertxException timedOut) {
			logger.trace("receive {}: Lock timed out", name);
			return null;
		}

		millis -= System.currentTimeMillis() - startTime;
		try {
			Long c1;
			while (true) {
				c1 = awaitResult(h -> counter.get(h));
				if (c1 == 0L) {
					if (millis <= 0) {
						return null;
					}
					logger.trace("receive {}: awaiting not empty", name);
					millis = notEmpty.awaitMillis(millis);
				} else {
					break;
				}
			}

			x = dequeue();
			logger.trace("receive {}: dequeued {}", name, x);
			c = awaitResult(h -> counter.getAndAdd(-1L, h));
			logger.trace("receive {}: subtracted counter to {}", name, c - 1);
			if (c > 1) {
				logger.trace("receive {}: signaling not empty", name);
				notEmpty.signal();
			}
		} finally {
			takeLock.release();
			logger.trace("receive {}: releasing lock", name);
		}
		if (c == capacity) {
			signalNotFull();
			logger.trace("receive {}: signaled not full", name);
		}
		return x;
	}

	@Suspendable
	private void signalNotFull() {
//		Lock putLock = lock("__putLock", Long.MAX_VALUE);
//		try {
			notFull.signal();
//		} finally {
//			putLock.release();
//		}
	}

	@Suspendable
	private Lock lock(String varName, long timeout) {
		return SharedData.lock(name + varName, timeout);
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
		// If there is no next element, this is also the last element
		if (first.next == null) {
			map.put("__last", first);
		}
		return x;
	}

	void init() throws SuspendExecution {
		SuspendableLinkedQueue.Node<V> nullNode = new SuspendableLinkedQueue.Node<>(null);

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
