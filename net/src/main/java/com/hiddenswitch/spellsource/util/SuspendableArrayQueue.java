package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static com.hiddenswitch.spellsource.util.QuickJson.array;

class SuspendableArrayQueue<V> implements SuspendableQueue<V> {

	private final String name;
	private final int capacity;

	SuspendableArrayQueue(String name, int capacity) {
		this.name = name;
		this.capacity = capacity;
	}

	SuspendableArrayQueue(String name) {
		this(name, -1);
	}

	@Override
	@Suspendable
	public boolean trySend(@NotNull V item, boolean createQueue) {
		Lock lock = lock();
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
			return true;
		} finally {
			lock.release();
		}
	}

	@NotNull
	private SuspendableCondition notEmpty() {
		return SuspendableCondition.getOrCreate("SuspendableArrayQueue::arrayQueues[" + name + "]__notEmpty");
	}

	@Suspendable
	private Lock lock() {
		return SharedData.lock("SuspendableArrayQueue::arrayQueues[" + name + "]__lock", 1000L);
	}

	@Override
	@Suspendable
	public V poll(long timeout) throws InterruptedException, SuspendExecution {
		Lock lock = lock();

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
			V x;

			try {
				if (header.unbounded) {
					x = (V) header.items[0];
					if (header.items.length == 1) {
						header.items = new Object[0];
					} else {
						header.items = Arrays.copyOfRange(header.items, 1, header.items.length);
					}
				} else {
					x = (V) header.items[header.takeIndex];
					header.items[header.takeIndex] = null;
					header.takeIndex++;
					if (header.takeIndex == header.items.length) {
						header.takeIndex = 0;
					}
				}

				header.count--;
				arrayQueues.put(name, header);
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
			return x;
		} finally {
			lock.release();
		}
	}

	@NotNull
	private SuspendableCondition notFull() {
		return SuspendableCondition.getOrCreate("SuspendableArrayQueue::arrayQueues[" + name + "]__notFull");
	}

	@NotNull
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

	static class SuspendableArrayQueueHeader implements Serializable, ClusterSerializable {
		// TODO: The header probably has to store the hash values of the objects to prevent duplicates from being enqueued
		Object[] items;
		int takeIndex;
		int putIndex;
		int count;
		boolean unbounded = false;

		SuspendableArrayQueueHeader() {
			this(-1);
		}

		SuspendableArrayQueueHeader(int capacity) {
			if (capacity <= 0) {
				unbounded = true;
				capacity = 0;
			}

			items = new Object[capacity];
		}

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
	}
}
