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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.vertx.core.Future;
import io.vertx.core.*;
import io.vertx.core.eventbus.impl.clustered.ClusterNodeInfo;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import io.vertx.spi.cluster.redis.RedisClusterManager;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.misc.RedissonPromise;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class RedisAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

	private final RSetMultimap<K, V> map;
	private final Vertx vertx;
	private final RedisClusterManager clusterManager;
	private final RedissonClient redisson;
	private final Map<K, Deque<HandlerTuple<V>>> handlers = new ConcurrentHashMap<>();

	// Helps maintain iterator state
	private final LoadingCache<K, AtomicInteger> iterators = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build(new CacheLoader<K, AtomicInteger>() {
				@Override
				public AtomicInteger load(K key) {
					return new AtomicInteger();
				}
			});

	RedisAsyncMultiMap(Vertx vertx, RedisClusterManager clusterManager, RedissonClient redisson, String name) {
		this.vertx = vertx;
		this.clusterManager = clusterManager;
		this.redisson = redisson;
		this.map = redisson.getSetMultimap(name, new KeyValueCodec(
				JsonJacksonCodec.INSTANCE.getValueEncoder(), JsonJacksonCodec.INSTANCE.getValueDecoder(),
				StringCodec.INSTANCE.getMapKeyEncoder(), StringCodec.INSTANCE.getMapKeyDecoder(),
				JsonJacksonCodec.INSTANCE.getValueEncoder(), JsonJacksonCodec.INSTANCE.getValueDecoder()));
	}

	@Override
	public void add(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
		if (redisson.isShutdown()) {
			// TODO: Should we gracefully allow adding here?¬
			completionHandler.handle(Future.failedFuture(new VertxException("redisson shut down")));
			return;
		}
		Context context = vertx.getOrCreateContext();
		map.putAsync(k, v).onComplete((r, t) -> context.runOnContext(i -> completionHandler.handle(Future.succeededFuture())));
	}

	@Override
	public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> asyncResultHandler) {
		if (redisson.isShutdown()) {
			asyncResultHandler.handle(Future.failedFuture(new VertxException("redisson shut down")));
			return;
		}
		Context context = vertx.getOrCreateContext();
		enqueue(context, asyncResultHandler, k);
		// Always retrieve the freshest subs list
		map.getAllAsync(k).onComplete((res, t) -> {
			context.runOnContext(i -> {
				HandlerTuple<V> dequeue = dequeue(k);
				Handler<AsyncResult<ChoosableIterable<V>>> handler = dequeue.handler;
				if (context != dequeue.context) {
					handler = x -> dequeue.context.runOnContext(v -> dequeue.handler.handle(x));
				}
				if (t != null) {
					handler.handle(Future.failedFuture(t));
				} else {
					// This approach to generating a ChooseableIterable implements round robin in a very straightforward way -
					// it rotates a list by an amount specified in an integer stored in the multimap. The list is always sorted
					// on the ClusterNodeInfo.nodeId first, which are authored to have an order that is the same across all nodes.
					// Changes to the subs list that occur while this is iterating will naturally not propagate to the
					// ChooseableIterable but that is not part of the API anyhow. Additionally this approach does not trick the
					// event bus into doing an ordered send.
					List<V> roundRobinned = res.stream()
							.sorted(Comparator.comparing(v -> {
								if (v instanceof ClusterNodeInfo) {
									return ((ClusterNodeInfo) v).nodeId;
								} else {
									return v.toString();
								}
							}))
							.filter(v -> {
								if (v instanceof ClusterNodeInfo) {
									// We will make a gentle affordance to check if the cluster node is flakey
									String nodeId = ((ClusterNodeInfo) v).nodeId;
									return clusterManager.isHealthy(nodeId);
								} else {
									return true;
								}
							}).collect(Collectors.toList());
					if (!roundRobinned.isEmpty()) {
						try {
							Collections.rotate(roundRobinned, iterators.get(k).getAndIncrement() % roundRobinned.size());
						} catch (ExecutionException e) {
							handler.handle(Future.failedFuture(e));
						}
					}
					handler.handle(Future.succeededFuture(new ChoosableList<>(roundRobinned)));
				}
			});
		});
	}


	private HandlerTuple<V> dequeue(K k) {
		return handlers.get(k).removeFirst();
	}

	private void enqueue(Context context, Handler<AsyncResult<ChoosableIterable<V>>> asyncResultHandler, K k) {
		handlers.compute(k, (k1, handlerTuples) -> {
			if (handlerTuples == null) {
				handlerTuples = new ArrayDeque<>();
			}
			handlerTuples.addLast(new HandlerTuple<>(context, asyncResultHandler));
			return handlerTuples;
		});
	}

	@Override
	public void remove(K k, V v, Handler<AsyncResult<Boolean>> completionHandler) {
		if (redisson.isShutdown()) {
			completionHandler.handle(Future.failedFuture(new VertxException("redisson shut down")));
			return;
		}
		Context context = vertx.getOrCreateContext();
		map.removeAsync(k, v).onComplete((r, t) -> context.runOnContext(i -> completionHandler.handle(t == null ? Future.succeededFuture(r) : Future.failedFuture(t))));
	}

	@Override
	public void removeAllForValue(V v, Handler<AsyncResult<Void>> completionHandler) {
		if (redisson.isShutdown()) {
			completionHandler.handle(Future.failedFuture(new VertxException("redisson shut down")));
			return;
		}
		Context context = vertx.getOrCreateContext();

		map.readAllKeySetAsync().thenCompose(keys -> {
			return RedissonPromise.allOf(
					keys.stream().map(key -> {
						return map.removeAsync(key, v).toCompletableFuture();
					}).toArray(CompletableFuture[]::new)
			);
		}).handle((v2, t) -> {
			context.runOnContext(i -> completionHandler.handle(t == null ? Future.succeededFuture() : Future.failedFuture(t)));
			return null;
		});
	}

	@Override
	public void removeAllMatching(Predicate<V> p, Handler<AsyncResult<Void>> completionHandler) {
		if (redisson.isShutdown()) {
			completionHandler.handle(Future.failedFuture(new VertxException("redisson shut down")));
			return;
		}
		Context context = vertx.getOrCreateContext();
		(map.readAllKeySetAsync().thenCompose(keys -> {
			return RedissonPromise.allOf(
					keys.stream().map(key -> {
						// It does not appear that redis will require locking across the whole map to achieve this. The context of
						// removeAllMatching is to clean up down nodes, so there aren't going to be nodeIds that should be removed
						// "added back in" while this is executing.

//						RLock lock = map.getLock(key);
//						int id = (int) System.currentTimeMillis();
						return /*lock.lockAsync(4000, TimeUnit.MILLISECONDS, id)
								.thenCompose(v3 -> {
									return */map.getAllAsync(key) /*;
								})*/.thenCompose(values -> {
							return RedissonPromise.allOf(
									values.stream()
											.filter(p)
											.map(value -> map.removeAsync(key, value).toCompletableFuture())
											.toArray(CompletableFuture[]::new));
						})/*.thenCompose(ignored -> {
									return lock.unlockAsync(id);
								})*/
								.toCompletableFuture();
					}).toArray(CompletableFuture[]::new)
			);
		})).handle((v2, t) -> {
			context.runOnContext(i -> completionHandler.handle(t == null ? Future.succeededFuture() : Future.failedFuture(t)));
			return null;
		});
	}

	private static class ChoosableList<V> implements ChoosableIterable<V> {
		List<V> items;
		boolean chose;

		public ChoosableList(List<V> items) {
			this.items = items;
		}

		@Override
		public boolean isEmpty() {
			return items.isEmpty();
		}

		@Override
		public V choose() {
			// We're assuming you only choose once.
			if (chose) {
				throw new UnsupportedOperationException();
			}
			chose = true;
			if (items.isEmpty()) {
				return null;
			}
			return items.get(0);
		}

		@Override
		public Iterator<V> iterator() {
			return items.iterator();
		}
	}

	private static class HandlerTuple<V> {
		private final Context context;
		private final Handler<AsyncResult<ChoosableIterable<V>>> handler;

		public HandlerTuple(Context context, Handler<AsyncResult<ChoosableIterable<V>>> handler) {
			this.context = context;
			this.handler = handler;
		}
	}
}
