package com.hiddenswitch.spellsource.concurrent.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Timeout;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import com.hiddenswitch.spellsource.concurrent.SuspendableQueue;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class LocalQueue<V> implements SuspendableQueue<V> {
	private static Map<String, LocalQueue> QUEUES = new ConcurrentHashMap<>();
	private int capacity;
	private Channel<V> channel;
	private String name;

	@SuppressWarnings("unchecked")
	@Suspendable
	public static <E> LocalQueue<E> get(String name, int capacity) {
		return (LocalQueue<E>) QUEUES.getOrDefault(Vertx.currentContext().owner().toString() + "/" + name, new LocalQueue<>(capacity, name));
	}

	protected LocalQueue(int capacity, String name) {
		this.capacity = capacity;
		this.name = name;
	}

	public static boolean containsKey(String name) {
		return QUEUES.containsKey(Vertx.currentContext().owner().toString() + "/" + name);
	}

	@SuppressWarnings("unchecked")
	public static <E> LocalQueue<E> getOrCreateLocalQueue(String name, int capacity) {
		return (LocalQueue<E>) QUEUES.computeIfAbsent(Vertx.currentContext().owner().toString() + "/" + name, s -> {
			LocalQueue<Object> queue = new LocalQueue<>(capacity, name);
			queue.channel = Channels.newChannel(capacity, Channels.OverflowPolicy.BLOCK, false, false);
			return queue;
		});
	}

	@Override
	@Suspendable
	public boolean offer(@NotNull V item, boolean createQueue) {
		@SuppressWarnings("unchecked")
		LocalQueue<V> queue = (LocalQueue<V>) QUEUES.computeIfAbsent(getKey(), s -> {
			if (createQueue) {
				this.channel = Channels.newChannel(capacity, Channels.OverflowPolicy.BLOCK, false, false);
				return this;
			} else {
				return null;
			}
		});

		if (queue == null) {
			return false;
		}

		try {
			channel.send(item);
			return true;
		} catch (SuspendExecution | InterruptedException | VertxException ex) {
			return false;
		}
	}

	@Override
	@Suspendable
	public V poll(long timeout) throws InterruptedException, SuspendExecution {
		QUEUES.computeIfAbsent(getKey(), s -> {
			this.channel = Channels.newChannel(capacity, Channels.OverflowPolicy.BLOCK, false, false);
			return this;
		});
		return channel.receive(new Timeout(timeout, TimeUnit.MILLISECONDS));
	}

	@Override
	public V take() throws InterruptedException, SuspendExecution {
		QUEUES.computeIfAbsent(getKey(), s -> {
			this.channel = Channels.newChannel(capacity, Channels.OverflowPolicy.BLOCK, false, false);
			return this;
		});
		return channel.receive();
	}

	@Override
	@Suspendable
	public boolean offer(V item) {
		return offer(item, true);
	}

	@Override
	@Suspendable
	public void destroy() {
		QUEUES.remove(getKey());
		this.channel.close();
	}

	@NotNull
	public String getKey() {
		return Vertx.currentContext().owner().toString() + "/" + name;
	}
}
