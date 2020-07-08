/*
 * Copyright (c) 2019 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.spi.cluster.redis.impl;

import io.vertx.core.*;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.TaskQueue;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

/**
 * batch.atomic return's value must using Codec to Object. (always return String type)
 *
 * @author <a href="mailto:leo.tu.taipei@gmail.com">Leo Tu</a>
 * @see org.redisson.RedissonSetMultimapValues
 */
class RedisAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {
	private static final Logger log = LoggerFactory.getLogger(RedisAsyncMultiMap.class);

	//	protected final ConcurrentMap<K, ChoosableSet<V>> choosables;
	protected final RedissonClient redisson;
	protected final Vertx vertx;
	protected final RSetMultimap<K, V> multiMap;
	protected final String name;
	protected final TaskQueue taskQueue;
	protected final ConcurrentMap<K, ChoosableSet<V>> choosables;


	public RedisAsyncMultiMap(Vertx vertx, RedissonClient redisson, String name, Codec codec) {
		Objects.requireNonNull(redisson, "redisson");
		Objects.requireNonNull(name, "name");
		this.vertx = vertx;
		this.redisson = redisson;
		this.name = name;
		this.multiMap = createMultimap(this.redisson, this.name, codec);
		this.choosables = new ConcurrentHashMap<>();
		this.taskQueue = new TaskQueue();
	}

	/**
	 * Here you can customize(override method) a "Codec"
	 *
	 * @see org.redisson.codec.JsonJacksonCodec
	 */
	protected RSetMultimap<K, V> createMultimap(RedissonClient redisson, String name, Codec codec) {
		if (codec == null) {
			return redisson.getSetMultimap(name);
		} else {
			return redisson.getSetMultimap(name, codec);
		}
	}

	@Override
	public void add(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
		log.debug("add: {} added k={}, v={} on node={}", name, k, v, redisson.getId());
		Context context = vertx.getOrCreateContext();
		multiMap.putAsync(k, v).whenComplete((added, e) -> context.runOnContext(vd ->
				completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture()))
		);
	}

	/**
	 * @see io.vertx.core.eventbus.impl.clustered.ClusteredEventBus#sendOrPub(SendContextImpl<T>)
	 */
	@Override
	public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> resultHandler) {
		ContextInternal context = (ContextInternal) vertx.getOrCreateContext();
		@SuppressWarnings("unchecked")
		Queue<GetRequest<K, V>> getRequests = (Queue<GetRequest<K, V>>) context.contextData().computeIfAbsent(this, ctx -> new ArrayDeque<>());
		synchronized (getRequests) {
			getRequests.add(new GetRequest<>(k, resultHandler));
			if (getRequests.size() == 1) {
				dequeueGet(context, getRequests);
			}
		}
	}

	@Override
	public void remove(K k, V v, Handler<AsyncResult<Boolean>> completionHandler) {
		Context context = vertx.getOrCreateContext();
		if (choosables.containsKey(k)) {
			choosables.get(k).remove(v);
		}
		multiMap.removeAsync(k, v).whenComplete((removed, e) -> context.runOnContext(vd ->
				completionHandler.handle((e != null || removed==null) ? Future.failedFuture(e) : Future.succeededFuture(removed)))
		);
	}

	@Override
	public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
		removeAllMatching(value -> value == v || value.equals(v), completionHandler);
	}

	/**
	 * Remove values which satisfies the given predicate in all keys.
	 */
	@Override
	public void removeAllMatching(Predicate<V> p, Handler<AsyncResult<Void>> completionHandler) {
		Context context = vertx.getOrCreateContext();
		batchRemoveAllMatching(p, ar -> {
			if (ar.failed()) {
				context.runOnContext(vd -> completionHandler.handle(Future.failedFuture(ar.cause())));
			} else {
				context.runOnContext(vd -> completionHandler.handle(Future.succeededFuture(ar.result())));
			}
		});
	}

	@SuppressWarnings({"rawtypes", "deprecation"})
	private void batchRemoveAllMatching(Predicate<V> p, Handler<AsyncResult<Void>> completionHandler) {
		Context context = vertx.getOrCreateContext();
		context.executeBlocking(fut -> {
			try {
				Iterator<Map.Entry<K, V>> iterator = multiMap.entries().iterator();
				while (iterator.hasNext()) {
					Map.Entry<K, V> next = iterator.next();
					if (p.test(next.getValue())) {
						iterator.remove();
					}
				}
				fut.complete();
			} catch (Throwable t) {
				fut.fail(t);
			}
		}, true, completionHandler);
	}

	@Override
	public String toString() {
		return super.toString() + "{name=" + name + "}";
	}


	/**
	 * @author <a href="http://tfox.org">Tim Fox</a>
	 */
	protected static class ChoosableSet<T> implements ChoosableIterable<T> {

		private volatile boolean initialised;
		private final Set<T> ids;
		private volatile Iterator<T> iter;

		public ChoosableSet(int initialSize) {
			ids = new ConcurrentHashSet<>(initialSize);
		}

		public boolean isInitialised() {
			return initialised;
		}

		public void setInitialised() {
			this.initialised = true;
		}

		public void add(T elem) {
			ids.add(elem);
		}

		public void remove(T elem) {
			ids.remove(elem);
		}

		public void merge(ChoosableSet<T> toMerge) {
			ids.addAll(toMerge.ids);
		}

		public boolean isEmpty() {
			return ids.isEmpty();
		}

		@Override
		public Iterator<T> iterator() {
			return ids.iterator();
		}

		public synchronized T choose() {
			if (!ids.isEmpty()) {
				if (iter == null || !iter.hasNext()) {
					iter = ids.iterator();
				}
				try {
					return iter.next();
				} catch (NoSuchElementException e) {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	private static class GetRequest<K, V> {
		final K key;
		final Handler<AsyncResult<ChoosableIterable<V>>> handler;

		GetRequest(K key, Handler<AsyncResult<ChoosableIterable<V>>> handler) {
			this.key = key;
			this.handler = handler;
		}
	}


	private void dequeueGet(ContextInternal context, Queue<GetRequest<K, V>> getRequests) {
		GetRequest<K, V> getRequest;
		for (; ; ) {
			getRequest = getRequests.peek();
			ChoosableSet<V> entries = choosables.get(getRequest.key);
			if (entries != null && entries.isInitialised()) {
				Handler<AsyncResult<ChoosableIterable<V>>> handler = getRequest.handler;
				context.runOnContext(v -> {
					handler.handle(Future.succeededFuture(entries));
				});
				getRequests.remove();
				if (getRequests.isEmpty()) {
					return;
				}
			} else {
				break;
			}
		}
		K key = getRequest.key;
		Handler<AsyncResult<ChoosableIterable<V>>> handler = getRequest.handler;

		context.<ChoosableIterable<V>>executeBlocking(fut -> {
			Set<V> entries = multiMap.getAll(key);
			ChoosableSet<V> sids;
			if (entries != null) {
				sids = new ChoosableSet<>(entries.size());
				for (V hid : entries) {
					sids.add(hid);
				}
			} else {
				sids = new ChoosableSet<>(0);
			}
			ChoosableSet<V> prev = (sids.isEmpty()) ? null : choosables.putIfAbsent(key, sids);
//			if (prev != null) {
//				prev.merge(sids);
//				sids = prev;
//			}
			sids.setInitialised();
			fut.complete(sids);
		}, taskQueue, res -> {
			synchronized (getRequests) {
				context.runOnContext(v -> {
					handler.handle(res);
				});
				getRequests.remove();
				if (!getRequests.isEmpty()) {
					dequeueGet(context, getRequests);
				}
			}
		});
	}
}
