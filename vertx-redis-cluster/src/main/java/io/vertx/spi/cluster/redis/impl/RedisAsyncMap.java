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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.RedisStrictCommand;
import org.redisson.client.protocol.convertor.LongReplayConvertor;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

/**
 * @author <a href="mailto:leo.tu.taipei@gmail.com">Leo Tu</a>
 */
class RedisAsyncMap<K, V> implements AsyncMap<K, V> { // extends MapTTL<K, V>
	// private static final Logger log = LoggerFactory.getLogger(RedisAsyncMap.class);

	protected final RedisStrictCommand<Long> ZSCORE_LONG = new RedisStrictCommand<Long>("ZSCORE",
			new LongReplayConvertor()); // RedisCommands.ZSCORE

	protected final Vertx vertx;
	protected final RedissonClient redisson;
	protected final RMapCache<K, V> map;
	protected final String name;

	public RedisAsyncMap(Vertx vertx, RedissonClient redisson, String name, Codec codec) {
		// super(vertx, redisson, name);
		Objects.requireNonNull(redisson, "redisson");
		Objects.requireNonNull(name, "name");
		this.vertx = vertx;
		this.redisson = redisson;
		this.name = name;
		this.map = createRMapCache(this.redisson, this.name, codec);
		// super.setMap(this.map); // XXX
	}

	/**
	 * Here you can customize(override method) a "Codec"
	 *
	 * @see org.redisson.codec.JsonJacksonCodec
	 * @see org.redisson.codec.FstCodec
	 */
	protected RMapCache<K, V> createRMapCache(RedissonClient redisson, String name, Codec codec) {
		if (codec == null) {
			return redisson.getMapCache(name); // redisson.getMapCache(name, new RedisMapCodec());
		} else {
			return redisson.getMapCache(name, codec);
		}
	}

	@Override
	public void get(K k, Handler<AsyncResult<V>> resultHandler) {
		Objects.requireNonNull(k);
		Objects.requireNonNull(resultHandler);
		Context context = vertx.getOrCreateContext();
		map.getAsync(k).whenComplete((v, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(v)))
		);
	}

	@Override
	public void put(K k, V v, Handler<AsyncResult<Void>> completionHandler) {
		Objects.requireNonNull(k);
		Objects.requireNonNull(v);
		Objects.requireNonNull(completionHandler);
		Context context = vertx.getOrCreateContext();
		map.fastPutAsync(k, v).whenComplete((added, e) -> context.runOnContext(vd ->
				completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture()))
		);
	}

	@Override
	public void put(K k, V v, long ttl, Handler<AsyncResult<Void>> completionHandler) {
		Objects.requireNonNull(k);
		Objects.requireNonNull(v);
		Objects.requireNonNull(completionHandler);
		Context context = vertx.getOrCreateContext();
		map.fastPutAsync(k, v, ttl, TimeUnit.MILLISECONDS).whenComplete((added, e) -> context.runOnContext(vd ->
				completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture()))
		);
	}

	/**
	 * @return Previous value if key already exists else null
	 */
	@Override
	public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> completionHandler) {
		Context context = vertx.getOrCreateContext();
		map.putIfAbsentAsync(k, v).whenComplete((previousValue, e) -> context.runOnContext(vd ->
				completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(previousValue)))
		);
	}

	/**
	 * @return Previous value if key already exists else null
	 */
	@Override
	public void putIfAbsent(K k, V v, long ttl, Handler<AsyncResult<V>> completionHandler) {
		Context context = vertx.getOrCreateContext();
		map.putIfAbsentAsync(k, v, ttl, TimeUnit.MILLISECONDS)
				.whenComplete((previousValue, e) -> context.runOnContext(vd ->
						completionHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(previousValue)))
				);
	}

	@Override
	public void remove(K k, Handler<AsyncResult<V>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.removeAsync(k).whenComplete((previousValue, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(previousValue)))
		);
	}

	@Override
	public void removeIfPresent(K k, V v, Handler<AsyncResult<Boolean>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.removeAsync(k, v).whenComplete((removed, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(removed)))
		);
	}

	/**
	 * @return previous (old) value
	 */
	@Override
	public void replace(K k, V v, Handler<AsyncResult<V>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.replaceAsync(k, v).whenComplete((previousValue, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(previousValue)))
		);
	}

	@Override
	public void replaceIfPresent(K k, V oldValue, V newValue, Handler<AsyncResult<Boolean>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.replaceAsync(k, oldValue, newValue).whenComplete((replaced, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(replaced)))
		);
	}

	@Override
	public void clear(Handler<AsyncResult<Void>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.deleteAsync().whenComplete((deleted, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture()))
		);
	}

	@Override
	public void size(Handler<AsyncResult<Integer>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.sizeAsync().whenComplete((v, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(v)))
		);
	}

	@Override
	public void keys(Handler<AsyncResult<Set<K>>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.readAllKeySetAsync().whenComplete((v, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(v)))
		);
	}

	@Override
	public void values(Handler<AsyncResult<List<V>>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.readAllValuesAsync().whenComplete((v, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e)
						: Future.succeededFuture((v instanceof List) ? (List<V>) v : new ArrayList<>(v))))
		);
	}

	@Override
	public void entries(Handler<AsyncResult<Map<K, V>>> resultHandler) {
		Context context = vertx.getOrCreateContext();
		map.readAllMapAsync().whenComplete((v, e) -> context.runOnContext(vd ->
				resultHandler.handle(e != null ? Future.failedFuture(e) : Future.succeededFuture(v)))
		);
	}

	@Override
	public String toString() {
		return super.toString() + "{name=" + name + "}";
	}
}
