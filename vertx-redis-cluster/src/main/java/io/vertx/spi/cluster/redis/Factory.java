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
package io.vertx.spi.cluster.redis;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.impl.clustered.ClusterNodeInfo;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.redis.impl.DefaultFactory;
import org.redisson.api.RedissonClient;

import java.util.Map;

/**
 *
 */
public interface Factory {

	<K, V> AsyncMap<K, V> createAsyncMap(Vertx vertx, RedissonClient redisson, String name);

	<K, V> AsyncMultiMap<K, V> createAsyncMultiMap(Vertx vertx, RedissonClient redisson, String name, ClusterManager clusterManager);

	<K, V> Map<K, V> createMap(Vertx vertx, RedissonClient redisson, String name);

	AsyncMultiMap<String, ClusterNodeInfo> createAsyncMultiMapSubs(Vertx vertx, ClusterManager clusterManager,
	                                                               RedissonClient redisson, String name);

	Map<String, String> createMapHaInfo(Vertx vertx, ClusterManager clusterManager, RedissonClient redisson,
	                                    String name);

	static Factory createDefaultFactory() {
		return new DefaultFactory();
	}

}
