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
import com.google.common.collect.Sets;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.vertx.core.Future;
import io.vertx.core.*;
import io.vertx.core.eventbus.impl.clustered.ClusterNodeInfo;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.misc.RedissonPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

/**
 * A Redis-backed cluster manager. Uses Redisson for cluster primitives.
 * <p>
 * Node membership is maintained by heartbeat to Redis, i.e. redis keys are set frequently by this cluster manager, and
 * they are evicted by redis.
 * <p>
 * Limitations:
 * <ul>
 *   <li>Does not currently share a Redis instance gracefully with other users of Redis, Redisson or other Vertx
 *   clusters. Redisson has some support for prefixing/namespacing its use of Redis resources but it is not used
 *   here.</li>
 *   <li>Does not gracefully handle long network interruptions.</li>
 *   <li>There is a pin risk that with many, many vertx instances and transient network issues, high availability
 *   will accidentally deploy too many instances of a verticle due to a transient difference in the nodes list
 *   between two clustered vertx instances. If you need high availability singletons, use locks or semaphores.</li>
 * </ul>
 *
 * @author <a href="mailto:ben@hiddenswitch.com">Benjamin Berman</a>
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class RedisClusterManager implements ClusterManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisClusterManager.class);

	/**
	 * @see io.vertx.core.impl.VertxImpl
	 */
	private static final String CLUSTER_MAP_NAME = "__vertx:haInfo";

	/**
	 * @see io.vertx.core.eventbus.impl.clustered.ClusteredEventBus
	 */
	private static final String SUBS_MAP_NAME = "__vertx:subs";
	private static final long DEFAULT_SHUTDOWN_QUIET_PERIOD = 2000L;
	private static final long DEFAULT_SHUTDOWN_TIMEOUT = 8000L;
	private static final String VERTX_NODELIST_PREFIX = "__vertx:nodelist:";

	private final Factory factory;
	private final int minNodes;
	private final int timeToLiveMillis = 1000;
	private final int refreshRateMillis = 200;
	private final long shutdownQuietPeriod = DEFAULT_SHUTDOWN_QUIET_PERIOD;
	private final long shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;
	private final int coefficientOfTimeout = 2;
	private final int creditsPerAppearance = timeToLiveMillis / refreshRateMillis * coefficientOfTimeout;

	private Vertx vertx;
	private final RedissonClient redisson;
	private final long baseId;
	private final AtomicInteger threadIds = new AtomicInteger();
	private final ConcurrentMap<String, AsyncMap<?, ?>> asyncMaps = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, AsyncMultiMap<?, ?>> asyncMultiMaps = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, Map<?, ?>> maps = new ConcurrentHashMap<>();
	private final Set<RedisLock> locks = Sets.newConcurrentHashSet();
	private NodeListener nodeListener;
	private AsyncMultiMap<String, ClusterNodeInfo> subs;
	private Map<String, String> haInfo;
	private final Set<String> nodes = new ConcurrentSkipListSet<>();
	private long timerId = -1;
	private RBucket<String> thisNodeBucket;
	private final AtomicBoolean isActive = new AtomicBoolean();
	private final Multiset<String> nodeCredits = LinkedHashMultiset.create();
	private Handler<AsyncResult<Void>> joinHandler;
	private Object nodeListenerLock = new Object();
	private String nodeId;
	// Keeps track if this cluster manager is right now asking the HA manager to verify its verticles have not been
	// redeployed elsewhere due to transient networking issues.
	private volatile boolean checkingSelfFailover;
	private boolean exitGracefully = true;

	public RedisClusterManager(String singleServerRedisUrl) {
		this(singleServerRedisUrl, 1);
	}

	public RedisClusterManager(String singleServerRedisUrl, int minNodes) {
		Config config = new Config();
		config.useSingleServer().setAddress(singleServerRedisUrl);
		config.setLockWatchdogTimeout(2000);
		this.redisson = Redisson.create(config);
		this.baseId = UUID.fromString(redisson.getId()).getLeastSignificantBits() & ~0xFFFF;
		this.factory = Factory.createDefaultFactory();
		this.minNodes = minNodes;
	}

	public RedisClusterManager(Config config) {
		this.redisson = Redisson.create(config);
		this.baseId = UUID.fromString(redisson.getId()).getLeastSignificantBits() & ~0xFFFF;
		this.factory = Factory.createDefaultFactory();
		this.minNodes = 1;
	}

	/**
	 * The amount of time a node will not be heard from in order to be considered dead.
	 *
	 * @return
	 */
	public long getNodeTimeout() {
		return timeToLiveMillis * coefficientOfTimeout;
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
						key -> factory.createAsyncMultiMap(vertx, redisson, name, this));
				future.complete(asyncMultiMap);
			}

		}, resultHandler);
	}

	@Override
	public <K, V> void getAsyncMap(String name, Handler<AsyncResult<AsyncMap<K, V>>> resultHandler) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(resultHandler);
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
			RLock lock = redisson.getLock("__vertx:locks:" + name);
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
								resultHandler.handle(Future.failedFuture(t == null ? new TimeoutException(name + " (" + finalTimeout + ")") : t));
							} else {
								LOGGER.debug("getLockWithTimeout: {} acquired by verticle {}/{} with threadId {}", name, getNodeID(), context.deploymentID(), threadId);
								RedisLock result = new RedisLock(lock, threadId);
								locks.add(result);
								resultHandler.handle(Future.succeededFuture(result));
							}
						});
						return null;
					});
		} catch (Exception e) {
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
			resultHandler.handle(Future.failedFuture(e));
		}
	}

	@Override
	public String getNodeID() {
		return nodeId;
	}

	@Override
	public List<String> getNodes() {
		return new ArrayList<>(nodes);
	}

	/**
	 * (2)
	 * </p>
	 * HAManager
	 */
	@Override
	public void nodeListener(NodeListener nodeListener) {
		Objects.requireNonNull(nodeListener);
		this.nodeListener = nodeListener;
	}

	@Override
	public void join(Handler<AsyncResult<Void>> joinHandler) {
		// Join will be called when minCount nodes are observed in the nodes list by updateNodes
		this.joinHandler = joinHandler;
		// Gracefully fails in many situations
		if (redisson.isShutdown() || redisson.isShuttingDown()) {
			joinHandler.handle(Future.failedFuture(new VertxException("redisson shut down")));
			return;
		}
		if (isActive()) {
			joinHandler.handle(Future.failedFuture(new VertxException("already active")));
			return;
		}
		if (timerId != -1) {
			joinHandler.handle(Future.failedFuture(new VertxException("already joining")));
			return;
		}

		// The node list will be updated at most once, by a dedicated ordering primitive.
		ExecutorService runner =
				new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
						new SynchronousQueue<>(),
						new DefaultThreadFactory("redis-clustermanager-calls-RedisClusterManager#updateNodes"),
						new ThreadPoolExecutor.DiscardPolicy());

		// Conceptually, nodes are simply assigned an order, to guarantee that the HAManager sees the same order of nodes
		// in all places. The order number, an atomic long, is suffixed with a unique node ID corresponding to the redisson
		// connection ID, which is itself just a random UUID. Other RedisClusterManagers discover other nodes by polling
		// (scanning keys from Redis prefixed with VERTX_NODELIST_PREFIX). They sort the nodes, then call nodeAdded /
		// nodeLeft at the right time. The keys are set with a time to live, so if they're not refreshed in time (i.e., a
		// non-graceful shutdown of a node), Redis evicts the keys, thereby clearing the node list on all the nodes when
		// the next check. The updateNodes function gives greater than 1 "credits" for every appearance of a key; then,
		// every time it checks for nodes, it reduces the credits accumulated for each node by 1. Thus it only really
		// considers a node to have left if the number of credits reaches zero. This crediting system is necessary to work
		// around the fact that redis can only scan a limited number of keys at once, so it smooths out issues related to
		// redis's idiosyncratic way of querying lists of keys that have TTL set on them.
		redisson.getAtomicLong("__vertx:nodeOrder")
				.getAndIncrementAsync()
				.thenCompose(orderNumber -> {
					this.nodeId = String.format("%09d:%s", orderNumber, redisson.getId());
					String name = VERTX_NODELIST_PREFIX + nodeId;
					thisNodeBucket = redisson.<String>getBucket(name, new StringCodec());
					LOGGER.trace("join: bucket name is being set {}", name);
					return thisNodeBucket.trySetAsync("ok", timeToLiveMillis, TimeUnit.MILLISECONDS);
				})
				.thenCompose(suceeded -> {
					if (suceeded) {
						return RedissonPromise.newSucceededFuture(true);
					} else {
						return RedissonPromise.newFailedFuture(new RuntimeException("node already exists with this order number??"));
					}
				})
				.thenAccept(suceeded -> {
					timerId = vertx.setPeriodic(refreshRateMillis, ignored -> {
						if (redisson.isShuttingDown() || redisson.isShutdown()) {
							return;
						}

						if (!redisson.isShuttingDown() && !redisson.isShutdown()) {
							thisNodeBucket
									.getAndSetAsync("ok", timeToLiveMillis, TimeUnit.MILLISECONDS)
									.onComplete((r, t) -> {
										if (t != null) {
											vertx.cancelTimer(this.timerId);
											LOGGER.error("{} join refresh errored", nodeId, t);
											return;
										}
										if (r == null) {
											LOGGER.warn("{} join refresh timed out previously due to expiration. This indicates that there " +
													"a transient networking issue between this cluster manager and Redis that lasted longer than " +
													"{} milliseconds. Other nodes will assume this node died if the amount of time it appeared " +
													"exceeds {} milliseconds, and a failover may have occurred. Because of this, the cluster " +
													"manager will now check for failover.", nodeId, timeToLiveMillis, getNodeTimeout());
											if (!checkingSelfFailover) {
												checkingSelfFailover = true;
												nodeListener.nodeAdded(getNodeID());
												checkingSelfFailover = false;
											}
										}
									});

							// Node monitor
							runner.submit(this::updateNodes);
						}

					});
				})
				.exceptionally(t -> {
					joinHandler.handle(Future.failedFuture(t));
					return null;
				});


	}

	private void updateNodes() {
		List<String> someNodes = redisson.getKeys().getKeysStreamByPattern(VERTX_NODELIST_PREFIX + "*", 8)
				.map(key -> key.replace(VERTX_NODELIST_PREFIX, ""))
				.collect(toList());

		// Set credits to max for all the nodes we saw
		for (String someNode : someNodes) {
			nodeCredits.setCount(someNode, creditsPerAppearance);
		}

		// Remove 1 credit for each existing node
		Multisets.removeOccurrences(nodeCredits, nodes);

		List<String> nodesUnseen = new ArrayList<>(nodes);
		List<String> nodesNow = new ArrayList<>(nodeCredits.elementSet());
		List<String> nodesAdded = new ArrayList<>();
		List<String> nodesRemoved = new ArrayList<>();

		synchronized (nodeListenerLock) {
			for (String nodeNow : nodesNow) {
				if (!nodesUnseen.remove(nodeNow)) {
					nodes.add(nodeNow);
					nodesAdded.add(nodeNow);
				}
			}

			for (String nodeUnseen : nodesUnseen) {
				nodes.remove(nodeUnseen);
				nodesRemoved.add(nodeUnseen);
			}

			Collections.sort(nodesAdded);
			Collections.sort(nodesRemoved);

			registerListeners(nodesAdded);

			for (String nodeAdded : nodesAdded) {
				if (isActive.get() && nodeListener != null) {
					// It is okay to notify that I myself have been added here
					nodeListener.nodeAdded(nodeAdded);
				}
			}

			for (String nodeRemoved : nodesRemoved) {
				if (isActive.get() && nodeListener != null) {
					nodeListener.nodeLeft(nodeRemoved);
				}
			}

			if (nodes.size() >= minNodes && isActive.compareAndSet(false, true)) {
				// We can start. Probably needs a timeout to start anyway
				joinHandler.handle(Future.succeededFuture());
			}
		}
	}

	private void registerListeners(List<String> nodesAdded) {
		for (String nodeAdded : nodesAdded) {
			if (Objects.equals(nodeAdded, nodeId)) {
				continue;
			}
			String name = VERTX_NODELIST_PREFIX + nodeAdded;
			LOGGER.debug("registerListeners: {} listening for {}", nodeId, name);
			RBucket<String> bucket = redisson.getBucket(name, new StringCodec());
			AtomicInteger deletedListener = new AtomicInteger();
			AtomicInteger expiredListener = new AtomicInteger();
			deletedListener.set(bucket.addListener(new DeletedObjectListener() {
				@Override
				public void onDeleted(String name) {
					LOGGER.trace("onDeleted {}", name);
					bucket.removeListenerAsync(deletedListener.get());
					bucket.removeListenerAsync(expiredListener.get());
					immediatelyRemoveNode(nodeAdded);
				}
			}));

			expiredListener.set(bucket.addListener(new ExpiredObjectListener() {
				@Override
				public void onExpired(String name) {
					LOGGER.trace("onExpired {}", name);
					bucket.removeListenerAsync(deletedListener.get());
					bucket.removeListenerAsync(expiredListener.get());
					immediatelyRemoveNode(nodeAdded);
				}
			}));
		}
	}

	private void immediatelyRemoveNode(String nodeRemoved) {
		nodeCredits.setCount(nodeRemoved, 0);
		if (nodes.remove(nodeRemoved) && nodeListener != null) {
			nodeListener.nodeLeft(nodeRemoved);
		}
	}

	@Override
	public void leave(Handler<AsyncResult<Void>> resultHandler) {
		LOGGER.trace("leave: called {}", nodeId);
		if (redisson.isShuttingDown() || redisson.isShutdown()) {
			LOGGER.debug("leave: {} already shut down", nodeId);
			resultHandler.handle(Future.succeededFuture());
			return;
		}

		vertx.cancelTimer(timerId);
		timerId = -1;
		isActive.set(false);

		vertx.executeBlocking(outerFut -> {
			RFuture<Boolean> booleanRFuture;
			if (isExitGracefully()) {
				booleanRFuture = thisNodeBucket.deleteAsync();
			} else {
				booleanRFuture = RedissonPromise.newSucceededFuture(false);
			}
			booleanRFuture
					.thenCompose(deleted -> {
						if (!deleted) {
							LOGGER.debug("leave: failed to delete {}", thisNodeBucket.getName());
						}

						if (isExitGracefully()) {
							return RedissonPromise.allOf(locks.stream().map(lock -> lock.unlockAsync().toCompletableFuture()).toArray(CompletableFuture[]::new));
						} else {
							return RedissonPromise.newSucceededFuture(null);
						}
					})
					.handle((v, t) -> {
						if (t != null) {
							outerFut.fail(t);
							return null;
						}

						vertx.executeBlocking(fut -> {
							try {
								if (isExitGracefully()) {
									redisson.shutdown(shutdownQuietPeriod, shutdownTimeout, TimeUnit.MILLISECONDS);
								} else {
									redisson.shutdown(0, 0, TimeUnit.MILLISECONDS);
								}
								fut.complete();
							} catch (Throwable t2) {
								fut.fail(t2);
								return;
							}
							fut.complete();
						}, false, outerFut);
						return null;
					});

		}, false, resultHandler);
	}

	@Override
	public boolean isActive() {
		return isActive.get();
	}

	@Override
	public String toString() {
		return super.toString() + "{nodeID=" + getNodeID() + "}";
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

		/**
		 * Provides a way for the framework to unlock the redis lock
		 *
		 * @return
		 */
		private RFuture<Void> unlockAsync() {
			if (!released) {
				released = true;
				return redisLock.unlockAsync(threadId);
			}
			return RedissonPromise.newSucceededFuture(null);
		}

		@Override
		public void release() {
			if (!released) {
				released = true;
				redisLock
						.unlockAsync(threadId)
						.onComplete((r, t) -> {
							if (t != null) {
								LOGGER.error("RedisLock release {}: {} failed unlocking due to {}", getNodeID(), redisLock.getName(), threadId, t);
								vertx.exceptionHandler().handle(t);
							} else {
								LOGGER.trace("RedisLock release: {} succeeded by {}", redisLock.getName(), threadId);
							}
						});

			} else {
				LOGGER.trace("RedisLock release {}: {} already released", getNodeID(), redisLock.getName());
			}
		}
	}

	/**
	 * Specifies whether this cluster manager should exit gracefully when {@link #leave(Handler)} is called.
	 * <p>
	 * When not exiting gracefully, locks are not closed and the node membership entry is not removed.
	 *
	 * @return
	 */
	public boolean isExitGracefully() {
		return exitGracefully;
	}

	public RedisClusterManager setExitGracefully(boolean exitGracefully) {
		this.exitGracefully = exitGracefully;
		return this;
	}

	/**
	 * Based on the heartbeat of the specified node ID, is it failing?
	 *
	 * @param nodeId
	 * @return
	 */
	public boolean isFailing(String nodeId) {
		return nodeCredits.count(nodeId) <= creditsPerAppearance / 2;
	}
}
