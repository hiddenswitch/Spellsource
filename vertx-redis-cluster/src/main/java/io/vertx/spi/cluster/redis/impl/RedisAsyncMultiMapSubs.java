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
import java.util.function.Predicate;

import org.redisson.api.BatchOptions;
import org.redisson.api.BatchOptions.ExecutionMode;
import org.redisson.api.RBatch;
import org.redisson.api.RSetMultimap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.impl.clustered.ClusterNodeInfo;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;

/**
 * SUBS_MAP_NAME = "__vertx.subs"
 * <p/>
 * When last node disconnected will still keep it's subs address. (Don't remove last node subs, "__vertx.subs" are not
 * empty !)
 *
 * @author <a href="mailto:leo.tu.taipei@gmail.com">Leo Tu</a>
 * @see io.vertx.core.net.impl.ServerID
 * @see org.redisson.codec.JsonJacksonCodec
 * @see io.vertx.core.eventbus.impl.clustered.ClusterNodeInfo
 */
class RedisAsyncMultiMapSubs extends RedisAsyncMultiMap<String, ClusterNodeInfo> {
	private static final Logger log = LoggerFactory.getLogger(RedisAsyncMultiMapSubs.class);

	@SuppressWarnings("unused")
	private final ClusterManager clusterManager;

	public RedisAsyncMultiMapSubs(Vertx vertx, ClusterManager clusterManager, RedissonClient redisson, String name) {
		super(vertx, redisson, name, null);
		this.clusterManager = clusterManager;
	}

	/**
	 * @see org.redisson.client.codec.StringCodec
	 * @see org.redisson.codec.JsonJacksonCodec
	 */
	@Override
	protected RSetMultimap<String, ClusterNodeInfo> createMultimap(RedissonClient redisson, String name, Codec codec) {
		RSetMultimap<String, ClusterNodeInfo> mmap = redisson.getSetMultimap(name, new KeyValueCodec(//
				JsonJacksonCodec.INSTANCE.getValueEncoder(), JsonJacksonCodec.INSTANCE.getValueDecoder(), //
				StringCodec.INSTANCE.getMapKeyEncoder(), StringCodec.INSTANCE.getMapKeyDecoder(), //
				JsonJacksonCodec.INSTANCE.getValueEncoder(), JsonJacksonCodec.INSTANCE.getValueDecoder()));
		return mmap;
	}

	@Override
	public void removeAllForValue(ClusterNodeInfo v, Handler<AsyncResult<Void>> completionHandler) {
		removeAllMatching(value -> value == v || value.equals(v), completionHandler);
	}

	/**
	 * Remove values which satisfies the given predicate in all keys.
	 *
	 * @see io.vertx.core.eventbus.impl.clustered.ClusteredEventBus#setClusterViewChangedHandler
	 */
	@Override
	public void removeAllMatching(Predicate<ClusterNodeInfo> p, Handler<AsyncResult<Void>> completionHandler) {
		Context context = vertx.getOrCreateContext();
		batchRemoveAllMatching(p, ar -> {
			if (ar.failed()) {
				context.runOnContext(vd -> completionHandler.handle(Future.failedFuture(ar.cause())));
			} else {
				context.runOnContext(vd -> completionHandler.handle(Future.succeededFuture(ar.result())));
			}
		});
	}

	private void batchRemoveAllMatching(Predicate<ClusterNodeInfo> p, Handler<AsyncResult<Void>> completionHandler) {
		List<Map.Entry<String, ClusterNodeInfo>> deletedList = new ArrayList<>();
		multiMap.entries().forEach(entry -> {
			ClusterNodeInfo value = entry.getValue();
			if (p.test(value)) { // XXX: "!members.contains(ci.nodeId)"
				deletedList.add(entry);
			}
		});

		if (!deletedList.isEmpty()) {
			RBatch batch = redisson.createBatch(BatchOptions.defaults().executionMode(ExecutionMode.REDIS_WRITE_ATOMIC).skipResult());
			deletedList.forEach(entry -> {
				multiMap.removeAsync(entry.getKey(), entry.getValue());
			});

			batch.executeAsync().whenCompleteAsync((result, e) -> {
				if (e != null) {
					log.warn("error: {}", e.toString());
					completionHandler.handle(Future.failedFuture(e));
				} else { // XXX: skipResult() ==> result.class=<null>, result=null
					completionHandler.handle(Future.succeededFuture());
				}
			});
		} else {
			completionHandler.handle(Future.succeededFuture());
		}
	}

}
