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
import io.vertx.core.*;
import io.vertx.core.eventbus.impl.clustered.ClusterNodeInfo;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;
import io.vertx.core.spi.cluster.ClusterManager;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.misc.RedissonPromise;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * batch.atomic return's value must using Codec to Object. (always return String type)
 *
 * @see org.redisson.RedissonSetMultimapValues
 */
class RedisAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {

	private final RSetMultimap<K, V> map;
	private final Vertx vertx;
	private final ClusterManager clusterManager;
	private final RedissonClient redisson;
	private final LoadingCache<K, AtomicInteger> iterators = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build(new CacheLoader<K, AtomicInteger>() {
				@Override
				public AtomicInteger load(K key) {
					return new AtomicInteger();
				}
			});

	RedisAsyncMultiMap(Vertx vertx, ClusterManager clusterManager, RedissonClient redisson, String name) {
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
		map.getAllAsync(k).onComplete((values, t) -> {
			context.runOnContext(i -> {
				if (t != null) {
					asyncResultHandler.handle(Future.failedFuture(t));
				} else {
					List<V> valueList = values.stream()
							.sorted(Comparator.comparing(v -> {
								if (v instanceof ClusterNodeInfo) {
									return ((ClusterNodeInfo) v).nodeId;
								} else {
									return v.toString();
								}
							}))
							.filter(v -> {
								if (v instanceof ClusterNodeInfo) {
									return clusterManager.getNodes().contains(((ClusterNodeInfo) v).nodeId);
								} else {
									return true;
								}
							}).collect(Collectors.toList());
					if (!valueList.isEmpty()) {
						try {
							Collections.rotate(valueList, iterators.get(k).getAndIncrement() % valueList.size());
						} catch (ExecutionException e) {
							throw new RuntimeException(e);
						}
					}
					asyncResultHandler.handle(Future.succeededFuture(new ChoosableList(valueList)));
				}
			});

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
//						RLock lock = map.getLock(key);
//						int id = (int) System.currentTimeMillis();
						return /*lock.lockAsync(4000, TimeUnit.MILLISECONDS, id)
								.thenCompose(v3 -> {
									return */map.getAllAsync(key) /*;
								})*/.thenCompose(values -> {
									return RedissonPromise.allOf(values.stream().filter(p)
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
}
