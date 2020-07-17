package com.hiddenswitch.spellsource.net;

import io.vertx.core.Future;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.redis.RedisClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the clustering and in-memory state management of Spellsource game servers
 */
public interface Cluster {
	Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

	static Future<ClusterManager> create(int clusterPort, String... nodes) {
		return Future.succeededFuture(new RedisClusterManager(Configuration.getRedisUrl(), Math.max(1, nodes.length)));
	}
}
