package com.hiddenswitch.spellsource.concurrent.impl;

import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.fibers.Suspendable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hiddenswitch.spellsource.concurrent.SuspendableCondition;
import com.hiddenswitch.spellsource.concurrent.SuspendableLock;
import com.hiddenswitch.spellsource.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.concurrent.SuspendableQueue;
import io.vertx.core.VertxException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class SuspendableArrayQueue<V> implements SuspendableQueue<V> {
	private static Logger logger = LoggerFactory.getLogger(SuspendableArrayQueue.class);
	private final String name;
	private final int capacity;

	public SuspendableArrayQueue(String name, int capacity) {
		this.name = name;
		this.capacity = capacity;
	}

	public SuspendableArrayQueue(String name) {
		this(name, -1);
	}

	@Override
	@Suspendable
	public boolean offer(@NotNull V item, boolean createQueue) {
		SuspendableLock lock = lock();
		try {
			try {
				SuspendableMap<String, SuspendableArrayQueueHeader> arrayQueues = getArrayQueues();
				SuspendableArrayQueueHeader header = arrayQueues.get(name);
				if (header == null) {
					if (!createQueue) {
						return false;
					}

					header = new SuspendableArrayQueueHeader(capacity);
					arrayQueues.put(name, header);
				}

				if (header.unbounded) {
					header.items = Arrays.copyOf(header.items, header.items.length + 1);
					header.items[header.items.length - 1] = item;
					header.count++;
				} else {
					if (header.count == header.items.length) {
						return false;
					} else {
						header.items[header.putIndex] = item;
						header.putIndex++;
						if (header.putIndex == header.items.length) {
							header.putIndex = 0;
						}
						header.count++;
					}
				}

				arrayQueues.put(name, header);
				logger.trace("offer {}: Set to {}", name, header);
			} catch (VertxException ex) {
				// Sender interrupted!
				if (ex.getCause() instanceof InterruptedException
						// The cluster went down
						|| ex.getCause() instanceof TimeoutException) {
					return false;
				}
				// Not recoverable
				throw ex;
			}

			// Could be interrupted here, which would be a big problem, since it's already mutated
			// Something else has to have the responsibility of triggering the condition
			SuspendableCondition notEmpty = notEmpty();
			notEmpty.signal();
			logger.trace("offer {}: Signaled not empty", name);
			return true;
		} finally {
			lock.release();
		}
	}

	@NotNull
	@Suspendable
	private SuspendableCondition notEmpty() {
		return SuspendableCondition.getOrCreate("SuspendableArrayQueue::arrayQueues[" + name + "]__notEmpty");
	}

	@Suspendable
	private SuspendableLock lock() {
		return SuspendableLock.lock("SuspendableArrayQueue::arrayQueues[" + name + "]__lock");
	}

	@Override
	@Suspendable
	public V poll(long timeout) throws InterruptedException, SuspendExecution {
		SuspendableLock lock = lock();

		try {
			SuspendableCondition notEmpty = notEmpty();
			SuspendableMap<String, SuspendableArrayQueueHeader> arrayQueues = getArrayQueues();
			SuspendableArrayQueueHeader header = arrayQueues.get(name);

			if (header == null) {
				header = new SuspendableArrayQueueHeader(capacity);
				arrayQueues.put(name, header);
			}

			while (header.count == 0) {
				lock.release();
				timeout = notEmpty.awaitMillis(timeout);

				if (!arrayQueues.containsKey(name)) {
					// It was destroyed while waiting
					return null;
				}

				// This will also be zero if it was interrupted
				if (timeout <= 0) {
					return null;
				}

				lock = lock();
				header = arrayQueues.get(name);
			}
			return dequeue(arrayQueues, header);
		} finally {
			lock.release();
		}
	}

	@Suspendable
	private V dequeue(SuspendableMap<String, SuspendableArrayQueueHeader> arrayQueues, SuspendableArrayQueueHeader header) {
		V x;

		try {
			if (header.unbounded) {
				x = castItem(header.items[0]);
				if (header.items.length == 1) {
					header.items = new Object[0];
				} else {
					header.items = Arrays.copyOfRange(header.items, 1, header.items.length);
				}
			} else {
				x = castItem(header.items[header.takeIndex]);
				header.items[header.takeIndex] = null;
				header.takeIndex++;
				if (header.takeIndex == header.items.length) {
					header.takeIndex = 0;
				}
			}

			header.count--;
			arrayQueues.put(name, header);
			logger.trace("dequeue {}: Header is now {}", name, header);
		} catch (VertxException ex) {
			// The request was normally interrupted / canceled
			if (ex.getCause() instanceof InterruptedException
					// The request timed out (the cluster is down)
					|| ex.getCause() instanceof TimeoutException) {
				return null;
			}
			// Not recoverable
			throw ex;
		}

		SuspendableCondition notFull = notFull();
		notFull.signal();
		logger.trace("dequeue {}: Signalled not full", name);
		return x;
	}

	@SuppressWarnings("unchecked")
	private V castItem(Object item) {
		V x;
		if (item instanceof JsonObject) {
			x = Json.mapper.convertValue(((JsonObject) item).getMap(), new TypeReference<V>() {
			});
		} else {
			x = (V) item;
		}
		return x;
	}

	@Override
	@Suspendable
	public V take() throws InterruptedException {
		SuspendableLock lock = lock();
		try {
			SuspendableMap<String, SuspendableArrayQueueHeader> arrayQueues = getArrayQueues();
			SuspendableArrayQueueHeader header = arrayQueues.get(name);
			SuspendableCondition notEmpty = notEmpty();
			if (header == null) {
				header = new SuspendableArrayQueueHeader(capacity);
				arrayQueues.put(name, header);
			}
			while (header.count == 0) {
				logger.trace("take {}: Header count was 0", name);
				lock.release();
				if (!notEmpty.await()) {
					// Interrupted
					throw new InterruptedException();
				}

				lock = lock();
				header = arrayQueues.get(name);
			}
			return dequeue(getArrayQueues(), header);
		} finally {
			lock.release();
		}
	}

	@NotNull
	@Suspendable
	private SuspendableCondition notFull() {
		return SuspendableCondition.getOrCreate("SuspendableArrayQueue::arrayQueues[" + name + "]__notFull");
	}

	@NotNull
	@Suspendable
	private SuspendableMap<String, SuspendableArrayQueueHeader> getArrayQueues() {
		return SuspendableMap.getOrCreate("SuspendableArrayQueue::arrayQueues");
	}

	@Override
	@Suspendable
	public void destroy() {
		getArrayQueues().remove(name);
		notFull().signalAll();
		notEmpty().signalAll();
	}

	static class SuspendableArrayQueueHeader implements Serializable /*, ClusterSerializable*/ {
		// TODO: The header probably has to store the hash values of the objects to prevent duplicates from being enqueued
		Object[] items;
		int takeIndex;
		int putIndex;
		public int count;
		boolean unbounded = false;

		public SuspendableArrayQueueHeader() {
			this(-1);
		}

		SuspendableArrayQueueHeader(int capacity) {
			if (capacity <= 0) {
				unbounded = true;
				capacity = 0;
			}

			items = new Object[capacity];
		}

		/*
		@Override
		public void writeToBuffer(Buffer buffer) {
			// Assume it has a JSON representation
			new JsonObject()
					.put("takeIndex", takeIndex)
					.put("putIndex", putIndex)
					.put("count", count)
					.put("items", array(items))
					.put("unbounded", unbounded)
					.writeToBuffer(buffer);
		}

		@Override
		public int readFromBuffer(int pos, Buffer buffer) {
			JsonObject header = new JsonObject();
			int newPos = header.readFromBuffer(pos, buffer);
			items = header.getJsonArray("items").stream().toArray();
			takeIndex = header.getInteger("takeIndex");
			putIndex = header.getInteger("putIndex");
			count = header.getInteger("count");
			unbounded = header.getBoolean("unbounded");
			return newPos;
		}
		*/

		@Override
		public String toString() {
			return new ReflectionToStringBuilder(this)
					.build();
		}
	}
}
