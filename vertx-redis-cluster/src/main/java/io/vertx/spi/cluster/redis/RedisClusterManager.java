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

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import io.vertx.core.Future;
import io.vertx.core.*;
import io.vertx.core.eventbus.impl.clustered.ClusterNodeInfo;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * https://github.com/redisson/redisson/wiki/11.-Redis-commands-mapping
 *
 * @author <a href="mailto:leo.tu.taipei@gmail.com">Leo Tu</a>
 */
public class RedisClusterManager implements ClusterManager {
	private static final Logger log = LoggerFactory.getLogger(RedisClusterManager.class);

	/**
	 * @see io.vertx.core.impl.VertxImpl
	 */
	private static final String CLUSTER_MAP_NAME = "__vertx.haInfo";

	/**
	 * @see io.vertx.core.eventbus.impl.clustered.ClusteredEventBus
	 */
	private static final String SUBS_MAP_NAME = "__vertx.subs";
	private static final long DEFAULT_SHUTDOWN_QUIET_PERIOD = 4000L;
	private static final long DEFAULT_SHUTDOWN_TIMEOUT = 16000L;
	private static final String VERTX_NODELIST_PREFIX = "__vertx.nodelist.";

	private final Factory factory;
	private int minNodes;
	private final int timeToLiveMillis = 1000;
	private final int refreshRateMillis = 200;
	private long shutdownQuietPeriod = DEFAULT_SHUTDOWN_QUIET_PERIOD;
	private long shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;
	private final int creditsPerAppearance = timeToLiveMillis / refreshRateMillis * 4;


	private Vertx vertx;
	private final RedissonClient redisson;
	private final long baseId;
	private final AtomicInteger threadIds = new AtomicInteger();
	private final ConcurrentMap<String, AsyncMap<?, ?>> asyncMaps = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, AsyncMultiMap<?, ?>> asyncMultiMaps = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Map<?, ?>> maps = new ConcurrentHashMap<>();
	private NodeListener nodeListener;
	private AsyncMultiMap<String, ClusterNodeInfo> subs;
	private Map<String, String> haInfo;
	private List<String> nodes;
	private long timerId = -1;
	private RBucket<String> thisNodeBucket;
	private final AtomicBoolean isActive = new AtomicBoolean();
	private final Multiset<String> nodeCredits = LinkedHashMultiset.create();
	private Handler<AsyncResult<Void>> joinHandler;
	private Object nodeListenerLock = new Object();


	public RedisClusterManager(String singleServerRedisUrl) {
		this(singleServerRedisUrl, 1);
	}

	public RedisClusterManager(String singleServerRedisUrl, int minNodes) {
		Config config = new Config();
		config.useSingleServer().setAddress(singleServerRedisUrl);
		config.setLockWatchdogTimeout(1000);
		this.redisson = Redisson.create(config);
		this.baseId = UUID.fromString(redisson.getId()).getLeastSignificantBits() & ~0xFFFF;
		this.factory = Factory.createDefaultFactory();
		this.minNodes = minNodes;
	}

	public RedisClusterManager(Config config) {
		this.redisson = Redisson.create(config);
		this.baseId = UUID.fromString(redisson.getId()).getLeastSignificantBits() & ~0xFFFF;
		this.factory = Factory.createDefaultFactory();
	}

	/**
	 *
	 */
	@Override
	public void setVertx(Vertx vertx) {
		this.vertx = vertx;
	}

	/**
	 * EventBus been created !
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <K, V> void getAsyncMultiMap(String name, Handler<AsyncResult<AsyncMultiMap<K, V>>> resultHandler) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(resultHandler);
		vertx.executeBlocking(future -> {
			if (name.equals(SUBS_MAP_NAME)) {
				synchronized (this) {
					if (subs == null) {
						subs = factory.createAsyncMultiMapSubs(vertx, this, redisson, name);
					}
					future.complete((AsyncMultiMap<K, V>) subs);
				}
			} else {
				AsyncMultiMap<K, V> asyncMultiMap = (AsyncMultiMap<K, V>) asyncMultiMaps.computeIfAbsent(name,
						key -> factory.createAsyncMultiMap(vertx, redisson, name));
				future.complete(asyncMultiMap);
			}

		}, resultHandler);
	}

	@Override
	public <K, V> void getAsyncMap(String name, Handler<AsyncResult<AsyncMap<K, V>>> resultHandler) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(resultHandler);
		if (name.equals(CLUSTER_MAP_NAME)) {
			log.error("name cannot be '{}'", name);
			resultHandler.handle(Future.failedFuture(new IllegalArgumentException("name cannot be '" + name + "'")));
			return;
		}
		vertx.executeBlocking(future -> {
			@SuppressWarnings("unchecked")
			AsyncMap<K, V> asyncMap = (AsyncMap<K, V>) asyncMaps.computeIfAbsent(name,
					key -> factory.createAsyncMap(vertx, redisson, name));
			future.complete(asyncMap);
		}, resultHandler);
	}

	/**
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <K, V> Map<K, V> getSyncMap(String name) {
		Objects.requireNonNull(name);
		if (name.equals(CLUSTER_MAP_NAME)) {
			synchronized (this) {
				if (haInfo == null) {
					haInfo = factory.createMapHaInfo(vertx, this, redisson, name);
				}
				return (Map<K, V>) haInfo;
			}
		} else {
			Map<K, V> map = (Map<K, V>) maps.computeIfAbsent(name, key -> factory.createMap(vertx, redisson, name));
			return map;
		}
	}

	@Override
	public void getLockWithTimeout(String name, long timeout, Handler<AsyncResult<Lock>> resultHandler) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(resultHandler);
		if (timeout < 0) {
			throw new AssertionError("negative timeout");
		}
		try {
			RLock lock = redisson.getLock(name);
			Context context = vertx.getOrCreateContext();
			long threadId = baseId + (threadIds.getAndIncrement() & 0xFFFF);
			CompletionStage<Boolean> future;
			timeout = Math.min(Integer.MAX_VALUE, timeout);
			if (timeout >= Integer.MAX_VALUE) {
				future = lock.lockAsync(threadId).thenApply(res -> true);
			} else {
				future = lock.tryLockAsync(timeout, -1, TimeUnit.MILLISECONDS, threadId);
			}
			long finalTimeout = timeout;
			future
					.handleAsync((res, t) -> {
						context.runOnContext(v -> {
							if (t != null || !res) {
								resultHandler.handle(Future.failedFuture(!res ? new TimeoutException(name + " (" + finalTimeout + ")") : t));
							} else {
								log.debug("getLockWithTimeout: {} acquired by verticle {}/{} with threadId {}", name, getNodeID(), context.deploymentID(), threadId);
								resultHandler.handle(Future.succeededFuture(new RedisLock(lock, threadId)));
							}
						});
						return null;
					});
		} catch (Exception e) {
			log.info("nodeId: " + getNodeId() + ", name: " + name + ", timeout: " + timeout, e);
			resultHandler.handle(Future.failedFuture(e));
		}
	}

	@Override
	public void getCounter(String name, Handler<AsyncResult<Counter>> resultHandler) {
		Objects.requireNonNull(name);
		try {
			RAtomicLong counter = redisson.getAtomicLong(name);
			resultHandler.handle(Future.succeededFuture(new RedisCounter(counter)));
		} catch (Exception e) {
			log.info("nodeId: " + getNodeId() + ", name: " + name, e);
			resultHandler.handle(Future.failedFuture(e));
		}
	}

	@Override
	public String getNodeID() {
		return getNodeId();
	}

	@Override
	public List<String> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	/**
	 * (2)
	 * </p>
	 * HAManager
	 */
	@Override
	public void nodeListener(NodeListener nodeListener) {
		Objects.requireNonNull(nodeListener);
		synchronized (nodeListenerLock) {
			boolean first = this.nodeListener == null;
			this.nodeListener = nodeListener;
			if (first) {
				for (String node : nodes) {
					this.nodeListener.nodeAdded(node);
				}
			}
		}
	}

	/**
	 * (1)
	 * <p/>
	 * createHaManager
	 */
	@Override
	public void join(Handler<AsyncResult<Void>> resultHandler) {
		this.joinHandler = resultHandler;
		if (redisson.isShutdown() || redisson.isShuttingDown()) {
			resultHandler.handle(Future.failedFuture(new VertxException("redisson shut down")));
			return;
		}
		if (isActive()) {
			resultHandler.handle(Future.failedFuture(new VertxException("already active")));
			return;
		}
		if (timerId != -1) {
			resultHandler.handle(Future.failedFuture(new VertxException("already joining")));
			return;
		}

		thisNodeBucket = redisson.<String>getBucket(VERTX_NODELIST_PREFIX + getNodeID(), new StringCodec());
		// Refresh my key entry
		nodes = new CopyOnWriteArrayList<>();
		// Refresh node list
		ExecutorService runner =
				new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
						new SynchronousQueue<>(), new ThreadPoolExecutor.DiscardPolicy());

		Runnable bindUpdateNodes = () -> {
			updateNodes();
		};

		timerId = vertx.setPeriodic(refreshRateMillis, ignored -> {
			if (redisson.isShuttingDown() || redisson.isShutdown()) {
				return;
			}

			thisNodeBucket.setAsync("ok", timeToLiveMillis, TimeUnit.MILLISECONDS);

			// Node monitor
			runner.submit(bindUpdateNodes);
		});
	}

	private void updateNodes() {
		List<String> someNodes = redisson.getKeys().getKeysStreamByPattern(VERTX_NODELIST_PREFIX + "*", 8)
				.map(key -> key.replace(VERTX_NODELIST_PREFIX, ""))
				.collect(Collectors.toList());

		// Set credits to max for all the nodes we saw
		for (String someNode : someNodes) {
			nodeCredits.setCount(someNode, creditsPerAppearance);
		}

		// Remove 1 credit for each existing node
		Multisets.removeOccurrences(nodeCredits, nodes);

		List<String> nodesUnseen = new ArrayList<>(nodes);
		List<String> nodesNow = new ArrayList<>(nodeCredits.elementSet());

		synchronized (nodeListenerLock) {
			for (String nodeNow : nodesNow) {
				if (!nodesUnseen.remove(nodeNow)) {
					nodes.add(nodeNow);

					if (nodes.size() >= minNodes) {
						// No longer constrained for starting
						minNodes = Integer.MIN_VALUE;
						if (isActive.compareAndSet(false, true)) {
							vertx.getOrCreateContext().runOnContext(v -> {
								joinHandler.handle(Future.succeededFuture());
							});
						} else if (nodeListener != null) {
							nodeListener.nodeAdded(nodeNow);
						}
					}
				}
			}

			for (String nodeUnseen : nodesUnseen) {
				nodes.remove(nodeUnseen);
				if (nodeListener != null) {
					nodeListener.nodeLeft(nodeUnseen);
				}
			}
		}
	}

	/**
	 *
	 */
	@Override
	public void leave(Handler<AsyncResult<Void>> resultHandler) {
		if (redisson.isShuttingDown() || redisson.isShutdown()) {
			resultHandler.handle(Future.succeededFuture());
			return;
		}

		ContextInternal context = (ContextInternal) vertx.getOrCreateContext();
		vertx.cancelTimer(timerId);
		timerId = -1;

		thisNodeBucket.deleteAsync().onComplete((r, t) -> {
			resultHandler.handle(Future.succeededFuture());
			redisson.shutdown(shutdownQuietPeriod, shutdownTimeout, TimeUnit.MILLISECONDS);
//			context.executeBlocking(fut -> {
//
//
//				fut.complete();
//			}, taskQueue, resultHandler);
			context.runOnContext(v -> {
				if (t == null) {
					resultHandler.handle(Future.succeededFuture());
				} else {
					resultHandler.handle(Future.failedFuture(t));
				}
			});
		});
	}

	@Override
	public boolean isActive() {
		return isActive.get();
	}

	@Override
	public String toString() {
		return super.toString() + "{nodeID=" + getNodeID() + "}";
	}

	public String getNodeId() {
		return redisson.getId();
	}

	/**
	 * Lock implement
	 */
	private class RedisCounter implements Counter {
		private final RAtomicLong counter;

		public RedisCounter(RAtomicLong counter) {
			this.counter = counter;
		}

		@Override
		public void get(Handler<AsyncResult<Long>> resultHandler) {
			Objects.requireNonNull(resultHandler);
			Context context = vertx.getOrCreateContext();
			counter.getAsync().whenComplete((v, e) -> context.runOnContext(vd ->
					resultHandler.handle((e != null || v == null) ? Future.failedFuture(e) : Future.succeededFuture(v)))
			);
		}

		@Override
		public void incrementAndGet(Handler<AsyncResult<Long>> resultHandler) {
			Objects.requireNonNull(resultHandler);
			Context context = vertx.getOrCreateContext();
			counter.incrementAndGetAsync().whenComplete((v, e) -> context.runOnContext(vd ->
					resultHandler.handle((e != null || v == null) ? Future.failedFuture(e) : Future.succeededFuture(v)))
			);
		}

		@Override
		public void getAndIncrement(Handler<AsyncResult<Long>> resultHandler) {
			Objects.requireNonNull(resultHandler);
			Context context = vertx.getOrCreateContext();
			counter.getAndIncrementAsync().whenComplete((v, e) -> context.runOnContext(vd ->
					resultHandler.handle((e != null || v == null) ? Future.failedFuture(e) : Future.succeededFuture(v)))
			);
		}

		@Override
		public void decrementAndGet(Handler<AsyncResult<Long>> resultHandler) {
			Objects.requireNonNull(resultHandler);
			Context context = vertx.getOrCreateContext();
			counter.decrementAndGetAsync().whenComplete((v, e) -> context.runOnContext(vd ->
					resultHandler.handle((e != null || v == null) ? Future.failedFuture(e) : Future.succeededFuture(v)))
			);
		}

		@Override
		public void addAndGet(long value, Handler<AsyncResult<Long>> resultHandler) {
			Objects.requireNonNull(resultHandler);
			Context context = vertx.getOrCreateContext();
			counter.addAndGetAsync(value).whenComplete((v, e) -> context.runOnContext(vd ->
					resultHandler.handle((e != null || v == null) ? Future.failedFuture(e) : Future.succeededFuture(v)))
			);
		}

		@Override
		public void getAndAdd(long value, Handler<AsyncResult<Long>> resultHandler) {
			Objects.requireNonNull(resultHandler);
			Context context = vertx.getOrCreateContext();
			counter.getAndAddAsync(value).whenComplete((v, e) -> context.runOnContext(vd ->
					resultHandler.handle((e != null || v == null) ? Future.failedFuture(e) : Future.succeededFuture(v)))
			);
		}

		@Override
		public void compareAndSet(long expected, long value, Handler<AsyncResult<Boolean>> resultHandler) {
			Objects.requireNonNull(resultHandler);
			Context context = vertx.getOrCreateContext();
			counter.compareAndSetAsync(expected, value).whenComplete((v, e) -> context.runOnContext(vd ->
					resultHandler.handle((e != null || v == null) ? Future.failedFuture(e) : Future.succeededFuture(v)))
			);
		}
	}

	/**
	 * Lock implement
	 */
	private class RedisLock implements Lock {
		private final RLock redisLock;
		private final long threadId;
		private volatile boolean released;


		public RedisLock(RLock redisLock, long threadId) {
			Objects.requireNonNull(redisLock);
			this.redisLock = redisLock;
			this.threadId = threadId;
		}

		@Override
		public void release() {
			if (!released) {
				released = true;

				redisLock.unlockAsync(threadId).onComplete((r, t) -> {
					if (t != null) {
						log.error("RedisLock release: {} failed unlocking by {}", redisLock.getName(), threadId, t);
						vertx.exceptionHandler().handle(t);
					} else {
						log.debug("RedisLock release: {} succeeded by {}", redisLock.getName(), threadId);
					}
				});

			}
		}
	}


}
