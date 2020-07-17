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
import io.vertx.core.shareddata.AsyncMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 */
class RedisAsyncMap<K, V> implements AsyncMap<K, V> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisAsyncMap.class);

	protected final Vertx vertx;
	protected final RedissonClient redisson;
	protected final RMapCache<K, V> map;
	protected final String name;

	public RedisAsyncMap(Vertx vertx, RedissonClient redisson, String name) {
		Objects.requireNonNull(redisson, "redisson");
		Objects.requireNonNull(name, "name");
		this.vertx = vertx;
		this.redisson = redisson;
		this.name = name;
		this.map = redisson.getMapCache(this.name, new RedisMapCodec());
	}

	@Override
	public void get(K k, Handler<AsyncResult<V>> resultHandler) {
		Objects.requireNonNull(k);
		Objects.requireNonNull(resultHandler);
		Context context = vertx.getOrCreateContext();
		LOGGER.trace("started get {}: {}", map.getName(), k);
		map.getAsync(k).onComplete((v, e) -> context.runOnContext(vd -> {
			LOGGER.trace("get {}: {} {}", map.getName(), k, v);
			resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(v));
		}));
	}

	@Override
	public void put(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
		LOGGER.trace("put {}: {} {}", map.getName(), k, v);
		Objects.requireNonNull(k);
		Objects.requireNonNull(v);
		Objects.requireNonNull(completionHandler);
		Context context = vertx.getOrCreateContext();
		map.fastPutAsync(k, v).onComplete((added, e) -> context.runOnContext(vd ->
				completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture())));
	}

	@Override
	public void put(K k, V v, long ttl, Handler<AsyncResult<Void>> completionHandler) {
		Objects.requireNonNull(k);
		Objects.requireNonNull(v);
		Objects.requireNonNull(completionHandler);
		Context context = vertx.getOrCreateContext();
		map.fastPutAsync(k, v, ttl, TimeUnit.MILLISECONDS).onComplete((added, e) -> context.runOnContext(vd ->
				completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture())));
	}

	/**
	 * @return Previous value if key already exists else null
	 */
	@Override
	public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> completionHandler) {
		Context context = vertx.getOrCreateContext();
		LOGGER.trace("putIfAbsent {}: {} {}", map.getName(), k, v);
		map.putIfAbsentAsync(k, v).onComplete((previousValue, e) -> context.runOnContext(vd ->
				completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(previousValue)))
		);
	}


	@Override
	public void putIfAbsent(K k, V v, long ttl, Handler<AsyncResult<V>> completionHandler) {
		Context context = vertx.getOrCreateContext();
		map.putIfAbsentAsync(k, v, ttl, TimeUnit.MILLISECONDS)
				.onComplete((previousValue, e) -> context.runOnContext(vd ->
						completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(previousValue)))
				);
	}

	@Override
	public void remove(K k, Handler<AsyncResult<V>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.removeAsync(k).onComplete((previousValue, e) -> context.runOnContext(vd -> {
			LOGGER.trace("remove {}: {} previously {}", map.getName(), k, previousValue);
			resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(previousValue));
		}));
	}

	@Override
	public void removeIfPresent(K k, V v, Handler<AsyncResult<Boolean>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.removeAsync(k, v).onComplete((removed, e) -> context.runOnContext(vd -> {
			LOGGER.trace("remove {}: {} {} did remove {}", map.getName(), k, v, removed);
			resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(removed));
		}));
	}

	/**
	 * @return previous (old) value
	 */
	@Override
	public void replace(K k, V v, Handler<AsyncResult<V>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.replaceAsync(k, v).onComplete((previousValue, e) -> context.runOnContext(vd -> {
			LOGGER.trace("replace {}: {} {} replaced {}", map.getName(), k, v, previousValue);
			resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(previousValue));
		}));
	}

	@Override
	public void replaceIfPresent(K k, V oldValue, V newValue, Handler<AsyncResult<Boolean>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.replaceAsync(k, oldValue, newValue).onComplete((replaced, e) -> context.runOnContext(vd -> {
			LOGGER.trace("replaceIfPresent {}: k={} oldValue={} newValue={} replaced {}", map.getName(), k, oldValue, newValue, replaced);
			resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(replaced));
		}));
	}

	@Override
	public void clear(Handler<AsyncResult<Void>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.deleteAsync().onComplete((deleted, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture()))
		);
	}

	@Override
	public void size(Handler<AsyncResult<Integer>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.sizeAsync().onComplete((v, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(v)))
		);
	}

	@Override
	public void keys(Handler<AsyncResult<Set<K>>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.readAllKeySetAsync().onComplete((v, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(v)))
		);
	}

	@Override
	public void values(Handler<AsyncResult<List<V>>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.readAllValuesAsync().onComplete((v, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e)
						: Future.succeededFuture((v instanceof List) ? (List<V>) v : new ArrayList<>(v))))
		);
	}

	@Override
	public void entries(Handler<AsyncResult<Map<K, V>>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.readAllMapAsync().onComplete((v, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(v)))
		);
	}

	@Override
	public String toString() {
		return super.toString() + "{name=" + name + "}";
	}
}
