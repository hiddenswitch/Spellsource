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
	private static final String CLUSTER_MAP_NAME = "__vertx:haInfo";

	/**
	 * @see io.vertx.core.eventbus.impl.clustered.ClusteredEventBus
	 */
	private static final String SUBS_MAP_NAME = "__vertx:subs";
	private static final long DEFAULT_SHUTDOWN_QUIET_PERIOD = 2000L;
	private static final long DEFAULT_SHUTDOWN_TIMEOUT = 8800L;
	private static final String VERTX_NODELIST_PREFIX = "__vertx:nodelist:";

	private final Factory factory;
	private final int minNodes;
	private final int timeToLiveMillis = 1000;
	private final int refreshRateMillis = 200;
	private long shutdownQuietPeriod = DEFAULT_SHUTDOWN_QUIET_PERIOD;
	private long shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;
	private final int creditsPerAppearance = timeToLiveMillis / refreshRateMillis * 2;


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
	private final Set<String> nodes = new ConcurrentSkipListSet<>();
	private List<Integer> listeners = new ArrayList<Integer>();
	private long timerId = -1;
	private RBucket<String> thisNodeBucket;
	private final AtomicBoolean isActive = new AtomicBoolean();
	private final Multiset<String> nodeCredits = LinkedHashMultiset.create();
	private Handler<AsyncResult<Void>> joinHandler;
	private Object nodeListenerLock = new Object();
	private String nodeId;


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
		this.minNodes = 1;
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
								resultHandler.handle(Future.failedFuture(!res ? new TimeoutException(name + " (" + finalTimeout + ")") : t));
							} else {
								log.debug("getLockWithTimeout: {} acquired by verticle {}/{} with threadId {}", name, getNodeID(), context.deploymentID(), threadId);
								resultHandler.handle(Future.succeededFuture(new RedisLock(lock, threadId)));
							}
						});
						return null;
					});
		} catch (Exception e) {
			log.info("nodeId: " + getNodeID() + ", name: " + name + ", timeout: " + timeout, e);
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
			log.info("nodeId: " + getNodeID() + ", name: " + name, e);
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

		ExecutorService runner =
				new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
						new SynchronousQueue<>(), new ThreadPoolExecutor.DiscardPolicy());

		// get my order ID
		redisson.getAtomicLong("__vertx:nodeOrder")
				.getAndIncrementAsync()
				.thenCompose(orderNumber -> {
					this.nodeId = String.format("%09d:%s", orderNumber, redisson.getId());
					String name = VERTX_NODELIST_PREFIX + nodeId;
					log.debug("join: bucket name {}", name);
					thisNodeBucket = redisson.<String>getBucket(name, new StringCodec());
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
							thisNodeBucket.setIfExistsAsync("ok", timeToLiveMillis, TimeUnit.MILLISECONDS)
									.exceptionally(t -> {
										log.error("{} previously timed out and node lefts were fired!", nodeId);
										return null;
									});

							// Node monitor
							runner.submit(this::updateNodes);
						}

					});
				})
				.exceptionally(t -> {
					resultHandler.handle(Future.failedFuture(t));
					return null;
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
			log.debug("registerListeners: {} listening for {}", nodeId, name);
			RBucket<Object> bucket = redisson.getBucket(name);
			AtomicInteger deletedListener = new AtomicInteger();
			AtomicInteger expiredListener = new AtomicInteger();
			deletedListener.set(bucket.addListener(new DeletedObjectListener() {
				@Override
				public void onDeleted(String name) {
					log.debug("onDeleted {}", name);
					bucket.removeListenerAsync(deletedListener.get());
					bucket.removeListenerAsync(expiredListener.get());
					immediatelyRemoveNode(nodeAdded);
				}
			}));

			expiredListener.set(bucket.addListener(new ExpiredObjectListener() {
				@Override
				public void onExpired(String name) {
					log.debug("onExpired {}", name);
					bucket.removeListenerAsync(deletedListener.get());
					bucket.removeListenerAsync(expiredListener.get());
					immediatelyRemoveNode(nodeAdded);
				}
			}));
		}
	}

	private void immediatelyRemoveNode(String nodeRemoved) {
//		return;
		nodeCredits.setCount(nodeRemoved, 0);
		if (nodes.remove(nodeRemoved) && nodeListener != null) {
			nodeListener.nodeLeft(nodeRemoved);
		}
	}

	/**
	 *
	 */
	@Override
	public void leave(Handler<AsyncResult<Void>> resultHandler) {
		log.debug("leave: called {}", nodeId);
		if (redisson.isShuttingDown() || redisson.isShutdown()) {
			log.debug("leave: {} already shut down", nodeId);
			resultHandler.handle(Future.succeededFuture());
			return;
		}

		vertx.cancelTimer(timerId);
		timerId = -1;
		isActive.set(false);

		vertx.executeBlocking(fut -> {
			try {
				redisson.shutdown(shutdownQuietPeriod, shutdownTimeout, TimeUnit.MILLISECONDS);
			} catch (Throwable t) {
				fut.fail(t);
				return;
			}
			fut.complete();
		}, false, Promise.promise());
		thisNodeBucket.unlinkAsync().onComplete((r, t) -> {
			if (!r) {
				log.warn("leave: failed to unlink {}", thisNodeBucket.getName());
			}
//			context.runOnContext(v -> {
			if (t == null) {
				log.debug("leave: deleted {}", thisNodeBucket.getName());
				resultHandler.handle(Future.succeededFuture());
			} else {
				log.error("leave: failed to delete {}", thisNodeBucket.getName(), t);
				resultHandler.handle(Future.failedFuture(t));
			}
//			});
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
