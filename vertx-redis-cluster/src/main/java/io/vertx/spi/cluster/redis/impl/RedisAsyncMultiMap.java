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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import org.redisson.api.BatchOptions;
import org.redisson.api.BatchOptions.ExecutionMode;
import org.redisson.api.RBatch;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;

/**
 * batch.atomic return's value must using Codec to Object. (always return String type)
 *
 * @author <a href="mailto:leo.tu.taipei@gmail.com">Leo Tu</a>
 * @see org.redisson.RedissonSetMultimapValues
 */
class RedisAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {
	private static final Logger log = LoggerFactory.getLogger(RedisAsyncMultiMap.class);

	protected final ConcurrentMap<K, ChoosableSet<V>> choosableSetPtr = new ConcurrentHashMap<>();
	protected final RedissonClient redisson;
	protected final Vertx vertx;
	protected final RSetMultimap<K, V> multiMap;
	protected final String name;

	public RedisAsyncMultiMap(Vertx vertx, RedissonClient redisson, String name, Codec codec) {
		Objects.requireNonNull(redisson, "redisson");
		Objects.requireNonNull(name, "name");
		this.vertx = vertx;
		this.redisson = redisson;
		this.name = name;
		this.multiMap = createMultimap(this.redisson, this.name, codec);
	}

	/**
	 * Here you can customize(override method) a "Codec"
	 *
	 * @see org.redisson.codec.JsonJacksonCodec
	 */
	protected RSetMultimap<K, V> createMultimap(RedissonClient redisson, String name, Codec codec) {
		if (codec == null) {
			return redisson.getSetMultimap(name); // redisson.getSetMultimapCache(name);
		} else {
			return redisson.getSetMultimap(name, codec);
		}
	}

	@Override
	public void add(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
		Context context = vertx.getOrCreateContext();
		multiMap.putAsync(k, v).whenComplete((added, e) -> context.runOnContext(vd -> //
				completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture())) //
		);
	}

	/**
	 * @see io.vertx.core.eventbus.impl.clustered.ClusteredEventBus#sendOrPub(SendContextImpl<T>)
	 */
	@Override
	public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		multiMap.getAllAsync(k).whenComplete((v, e) -> { // v class is java.util.LinkedHashSet
			if (e != null) {
				context.runOnContext(vd -> resultHandler.handle(Future.failedFuture(e)));
			} else {
				context.runOnContext(vd -> resultHandler.handle(Future.succeededFuture(getCurrentRef(k, v))));
			}
		});

	}

	@Override
	public void remove(K k, V v, Handler<AsyncResult<Boolean>> completionHandler) {
		Context context = vertx.getOrCreateContext();
		multiMap.removeAsync(k, v).whenComplete((removed, e) -> context.runOnContext(vd -> //
				completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(removed))) //
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
		multiMap.readAllKeySetAsync().whenComplete((keys, e) -> {
			if (e != null) {
				log.warn("error: {}", e.toString());
				completionHandler.handle(Future.failedFuture(e));
			} else {
				if (keys.isEmpty()) {
					completionHandler.handle(Future.succeededFuture());
					return;
				}
				Map<K, Future> keyFutures = new HashMap<>(keys.size());
				keys.forEach(key -> {
					keyFutures.put(key, Future.future());
				});
				for (K key : keys) {
					Future keyFuture = keyFutures.get(key);
					multiMap.getAllAsync(key).whenComplete((values, e2) -> {
						if (e2 != null) {
							keyFuture.fail(e2);
						} else {
							if (values.isEmpty()) {
								keyFuture.complete();
							} else {
								List<V> deletedList = new ArrayList<>();
								values.forEach(value -> {
									if (p.test(value)) { // XXX
										deletedList.add(value);
									}
								});

								if (deletedList.isEmpty()) {
									keyFuture.complete();
								} else {
									RBatch batch = redisson.createBatch(BatchOptions.defaults()
											.executionMode(ExecutionMode.REDIS_WRITE_ATOMIC).skipResult());
									deletedList.forEach(value -> {
										multiMap.removeAsync(key, value);
									});
									batch.executeAsync().whenCompleteAsync((result, e3) -> {
										if (e != null) {
											log.warn("key: {}, error: {}", key, e3.toString());
											keyFuture.fail(e3);
										} else { // XXX: skipResult() ==> result.class=<null>, result=null
											keyFuture.complete();
										}
									});
								}
							}
						}
					});
				}
				//
				CompositeFuture.join(new ArrayList<>(keyFutures.values())).setHandler(ar -> completionHandler
						.handle(ar.failed() ? Future.failedFuture(ar.cause()) : Future.succeededFuture()));
			}
		});
	}

	private ChoosableSet<V> getCurrentRef(K k, Collection<V> v) {
		ChoosableSet<V> current = choosableSetPtr.get(k);
		ChoosableSet<V> newSet = new ChoosableSet<>(vertx, v);
		if (current == null) {
			ChoosableSet<V> previous = choosableSetPtr.putIfAbsent(k, newSet);
			if (previous != null) {
				if (!previous.equals(newSet)) {
					choosableSetPtr.put(k, newSet);
					// log.debug("Using newSet: {}", newSet);
					current = newSet;
				} else {
					// log.debug("Using previous: {}", previous);
					current = previous;
				}
			} else {
				current = newSet;
				// log.debug("Using newSet: {}", newSet);
			}
		} else {
			if (!current.equals(newSet)) {
				choosableSetPtr.put(k, newSet);
				// log.debug("Using newSet: {}, old: {}", newSet, current);
				current = newSet;
			} else {
				if (current.isEmpty() || newSet.isEmpty()) {
					log.debug("Using current: {}, newSet: {}", current, newSet);
				}
			}
		}
		return current;
	}

	@Override
	public String toString() {
		return super.toString() + "{name=" + name + "}";
	}

}
