package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.TimeoutException;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static io.vertx.ext.sync.Sync.awaitResult;

class SuspendableLinkedQueue<V> implements SuspendableQueue<V> {
	private static final String TAKE_LOCK = "__takeLock";
	private static final String INITED = "__inited";
	private static final String HEAD = "__head";
	private static final String LAST = "__last";
	private static final String PUT_LOCK = "__putLock";
	private static final String HEADER = "__header";
	private static Logger logger = LoggerFactory.getLogger(SuspendableLinkedQueue.class);
	private final String name;
	private final SuspendableMap<String, Node<V>> map;
	private final int capacity;
	private final Counter counter;
	private final SuspendableCondition notEmpty;
	private final SuspendableCondition notFull;

	private SuspendableLinkedQueue(String name, Counter counter, SuspendableMap<String, Node<V>> map, int capacity, SuspendableCondition notEmpty, SuspendableCondition notFull) {
		this.counter = counter;
		this.name = name;
		this.map = map;
		this.capacity = capacity;
		this.notEmpty = notEmpty;
		this.notFull = notFull;
	}

	static <V> SuspendableLinkedQueue<V> getOrCreate(String name, int capacity) throws SuspendExecution {
		Vertx vertx = Vertx.currentContext().owner();
		System.setProperty("vertx.hazelcast.async-api", "true");
		Counter counter = awaitResult(h -> vertx.sharedData().getCounter(name + "__counter", h));
		SuspendableMap<String, Node<V>> map = SuspendableMap.getOrCreate(name + "__nodes");
		SuspendableCondition notEmpty = SuspendableCondition.getOrCreate(name + "__notEmpty");
		SuspendableCondition notFull = SuspendableCondition.getOrCreate(name + "__notFull");
		SuspendableLinkedQueue<V> suspendableQueue = new SuspendableLinkedQueue<>(name, counter, map, capacity, notEmpty, notFull);
		suspendableQueue.initIfNeeded();
		return suspendableQueue;
	}

	static <V> SuspendableQueue<V> getOrCreate(String name) throws SuspendExecution {
		return getOrCreate(name, Integer.MAX_VALUE);
	}

	@Override
	@Suspendable
	public boolean offer(@NotNull V item, boolean createQueue) {
		logger.trace("trySend {} {}: Enter", name, item);
		long c = -1L;
		Lock putLock = null;
		try {
			putLock = lock(PUT_LOCK);
			Node<V> node = new Node<V>(item);
			logger.trace("trySend {} {}: Node put", name, item);

			if (invoke(counter::get) < capacity) {
				map.put(node.id, node);
				enqueue(node);
				c = invoke(counter::getAndIncrement);
				logger.trace("trySend {} {}: Enqueued", name, item);
				if (c + 1 < capacity) {
					notFull.signal();
				}
			}
		} finally {
			if (putLock != null) {
				putLock.release();
			}
		}
		if (c == 0) {
			signalNotEmpty();
			logger.trace("trySend {} {}: Signaled not empty", name, item);
		}
		return c >= 0;
	}

	@Suspendable
	private Lock lock(String varName) {
		return SharedData.lock(name + varName);
	}

	@Suspendable
	private void signalNotEmpty() {
		Lock takeLock = lock(TAKE_LOCK);
		try {
			notEmpty.signal();
		} finally {
			takeLock.release();
		}
	}

	@Suspendable
	private void enqueue(Node<V> node) {
		Node<V> last = map.get(LAST);
		last.next = node.id;
		map.put(LAST, node);
		// This was the head element, so update it too
		if (last.item == null) {
			map.put(HEAD, last);
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
			takeLock = lock(TAKE_LOCK, timeout);
			logger.trace("receive {}: Taking lock", name);
		} catch (VertxException timedOut) {
			if (timedOut.getCause() instanceof TimeoutException) {
				logger.trace("receive {}: Lock timed out", name);
				return null;
			} else {
				throw timedOut;
			}
		}

		millis -= System.currentTimeMillis() - startTime;
		try {
			Long c1;
			while (true) {
				c1 = invoke(counter::get);
				if (c1 == -1L) {
					// Destroyed
					return null;
				}

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
			c = invoke(counter::getAndAdd, -1L);
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

	@Override
	public V take() throws InterruptedException, SuspendExecution {
		V x;
		Long c = -1L;
		Lock takeLock = lock(TAKE_LOCK);
		try {
			while (invoke(counter::get) == 0) {
				notEmpty.awaitMillis(Long.MAX_VALUE);
			}
			x = dequeue();
			c = invoke(counter::getAndAdd, -1L);
			if (c > 1) {
				notEmpty.signal();
			}
		} finally {
			takeLock.release();
		}

		if (c == capacity) {
			signalNotFull();
		}

		return x;
	}

	@Suspendable
	private void signalNotFull() {
		Lock putLock = lock(PUT_LOCK);
		try {
			notFull.signal();
		} finally {
			putLock.release();
		}
	}

	@Suspendable
	private Lock lock(String varName, long timeout) {
		return SharedData.lock(name + varName, timeout);
	}

	@Suspendable
	private V dequeue() {
		Node<V> h = map.remove(HEAD);
		map.remove(h.id);
		if (h.item != null) {
			throw new AssertionError("head.item != null");
		}
		Node<V> first = map.get(h.next);
		V x = first.item;
		first.item = null;
		map.put(HEAD, first);
		map.put(first.id, first);
		// If there is no next element, this is also the last element
		if (first.next == null) {
			map.put(LAST, first);
		}
		return x;
	}

	void initIfNeeded() throws SuspendExecution {
		Lock lock = lock(HEADER);
		try {
			SuspendableLinkedQueue.Node<V> nullNode = new SuspendableLinkedQueue.Node<>(null);
			if (map.putIfAbsent(INITED, nullNode) == null) {
				invoke(counter::compareAndSet, invoke(counter::get), 0L);
				map.put(nullNode.id, nullNode);
				map.put(LAST, nullNode);
				map.put(HEAD, nullNode);
			}
		} finally {
			lock.release();
		}
	}

	@Override
	@Suspendable
	public void destroy() {
		Lock lock = lock(HEADER);
		try {
			map.clear();
			invoke(counter::compareAndSet, invoke(counter::get), -1L);
			signalNotFull();
			signalNotEmpty();
		} finally {
			lock.release();
		}

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
