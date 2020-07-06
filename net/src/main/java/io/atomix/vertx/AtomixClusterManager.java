/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package io.atomix.vertx;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.atomix.cluster.ClusterMembershipEvent;
import io.atomix.cluster.ClusterMembershipEventListener;
import io.atomix.core.Atomix;
import io.atomix.core.counter.AtomicCounter;
import io.atomix.core.lock.AtomicLock;
import io.atomix.primitive.Consistency;
import io.atomix.primitive.Replication;
import io.atomix.primitive.protocol.ProxyProtocol;
import io.atomix.protocols.backup.MultiPrimaryProtocol;
import io.atomix.utils.concurrent.SingleThreadContext;
import io.atomix.utils.concurrent.ThreadContext;
import io.atomix.utils.serializer.Namespace;
import io.atomix.utils.serializer.Namespaces;
import io.atomix.utils.serializer.Serializer;
import io.vertx.core.*;
import io.vertx.core.net.impl.ServerID;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Vert.x Atomix cluster manager.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class AtomixClusterManager implements ClusterManager {
	public static final int DEFAULT_CACHE_SIZE = 64 * 1024 * 1024;
	public static final int DEFAULT_LOCK_CACHE_SIZE = 10000;
	public static final int DEFAULT_COUNTER_CACHE_SIZE = 10000;
	private final Atomix atomix;
	private final ThreadContext context;
	private NodeListener listener;
	private final AtomicBoolean active = new AtomicBoolean();
	private Vertx vertx;
	private final ClusterMembershipEventListener clusterListener = this::handleClusterEvent;
	private final LoadingCache<String, CompletableFuture<AtomicLock>> lockCache = CacheBuilder.newBuilder()
			.maximumSize(DEFAULT_LOCK_CACHE_SIZE)
			.build(new CacheLoader<>() {
				@Override
				public CompletableFuture<AtomicLock> load(String key) throws Exception {
					return atomix.atomicLockBuilder(key)
							.buildAsync();
				}
			});
	private final LoadingCache<String, CompletableFuture<AtomicCounter>> counterCache = CacheBuilder.newBuilder()
			.maximumSize(DEFAULT_COUNTER_CACHE_SIZE)
			.build(new CacheLoader<>() {
				@Override
				public CompletableFuture<AtomicCounter> load(String key) throws Exception {
					return atomix.atomicCounterBuilder(key)
							.buildAsync();
				}
			});

	public AtomixClusterManager() {
		this(Atomix.builder(Atomix.config()).build());
	}

	public AtomixClusterManager(String configFile) {
		this(new File(configFile));
	}

	public AtomixClusterManager(File configFile) {
		this(new Atomix(configFile));
	}

	public AtomixClusterManager(Atomix atomix) {
		this.atomix = checkNotNull(atomix, "atomix cannot be null");
		this.context = new SingleThreadContext("atomix-vertx-%d");
	}

	/**
	 * Returns the underlying Atomix instance.
	 *
	 * @return The underlying Atomix instance.
	 */
	public Atomix atomix() {
		return atomix;
	}

	@Override
	public void setVertx(Vertx vertx) {
		this.vertx = vertx;
	}

	/**
	 * Creates a new Vert.x compatible serializer.
	 */
	public Serializer createSerializer() {
		return Serializer.using(Namespace.builder()
				.setRegistrationRequired(false)
				.register(Namespaces.BASIC)
				.register(ServerID.class)
				.register(new ClusterSerializableSerializer<>(), ClusterSerializable.class)
				.build());
	}

	@Override
	public <K, V> void getAsyncMultiMap(String name, Handler<AsyncResult<AsyncMultiMap<K, V>>> handler) {
		atomix.<K, V>atomicMultimapBuilder(name)
				.withSerializer(createSerializer())
				.buildAsync()
				.whenComplete(VertxFutures.convertHandler(
						handler, map -> new AtomixAsyncMultiMap<>(vertx, map.async()), vertx.getOrCreateContext()));
	}

	@Override
	public <K, V> void getAsyncMap(String name, Handler<AsyncResult<AsyncMap<K, V>>> handler) {
		atomix.<K, V>atomicMapBuilder(name)
				.withSerializer(createSerializer())
				.buildAsync()
				.whenComplete(VertxFutures.convertHandler(
						handler, map -> new AtomixAsyncMap<>(vertx, map.async()), vertx.getOrCreateContext()));
	}

	@Override
	public <K, V> Map<K, V> getSyncMap(String name) {
		return new AtomixMap<>(vertx, atomix.<K, V>atomicMapBuilder(name)
//				.withProtocol(getProtocol())
				.withSerializer(createSerializer())
				.build());
	}

	@Override
	public void getLockWithTimeout(String name, long timeout, Handler<AsyncResult<Lock>> handler) {
		Context context = vertx.getOrCreateContext();
		lockCache.getUnchecked(name).whenComplete((lock, error) -> {
			if (error == null) {
				var fut = (timeout == Long.MAX_VALUE || timeout == -1) ? lock.async().lock().thenApply(Optional::of) : lock.async().tryLock(Duration.ofMillis(timeout));
				fut.whenComplete((lockResult, lockError) -> {
					if (lockError == null) {
						if (lockResult.isPresent()) {
							context.runOnContext(v -> Future.<Lock>succeededFuture(new AtomixLock(vertx, lock)).setHandler(handler));
						} else {
							context.runOnContext(v -> Future.<Lock>failedFuture(new VertxException("Timed out waiting to get lock " + name)).setHandler(handler));
						}
					} else {
						context.runOnContext(v -> Future.<Lock>failedFuture(lockError).setHandler(handler));
					}
				});
			} else {
				context.runOnContext(v -> Future.<Lock>failedFuture(error).setHandler(handler));
			}
		});
	}

	@Override
	public void getCounter(String name, Handler<AsyncResult<Counter>> handler) {
		Context context = vertx.getOrCreateContext();
		counterCache.getUnchecked(name).whenComplete(VertxFutures.convertHandler(
				handler, counter -> new AtomixCounter(vertx, counter.async()).setCloseHandler(v -> {
					counterCache.invalidate(name);
				}), context));
	}

	@Override
	public String getNodeID() {
		return atomix.getMembershipService().getLocalMember().id().id();
	}

	@Override
	public List<String> getNodes() {
		return atomix.getMembershipService().getMembers()
				.stream()
				.map(member -> member.id().id())
				.collect(Collectors.toList());
	}

	@Override
	public void nodeListener(NodeListener nodeListener) {
		this.listener = nodeListener;
	}

	@Override
	public synchronized void join(Handler<AsyncResult<Void>> handler) {
		Context context = vertx.getOrCreateContext();
		if (active.compareAndSet(false, true)) {
			atomix.start().whenComplete((result, error) -> {
				if (error == null) {
					atomix.getMembershipService().addListener(clusterListener);
					context.runOnContext(v -> Future.<Void>succeededFuture().setHandler(handler));
				} else {
					context.runOnContext(v -> Future.<Void>failedFuture(error).setHandler(handler));
				}
			});
		} else {
			context.runOnContext(v -> Future.<Void>succeededFuture().setHandler(handler));
		}
	}

	/**
	 * Handles a cluster event.
	 */
	private void handleClusterEvent(ClusterMembershipEvent event) {
		NodeListener nodeListener = this.listener;
		if (nodeListener != null) {
			context.execute(() -> {
				if (active.get()) {
					switch (event.type()) {
						case MEMBER_ADDED:
							nodeListener.nodeAdded(event.subject().id().id());
							break;
						case MEMBER_REMOVED:
							nodeListener.nodeLeft(event.subject().id().id());
							break;
						default:
							break;
					}
				}
			});
		}
	}

	@Override
	public synchronized void leave(Handler<AsyncResult<Void>> handler) {
		Context context = vertx.getOrCreateContext();
		if (active.compareAndSet(true, false)) {
			atomix.stop().whenComplete((leaveResult, leaveError) -> {
				if (leaveError == null) {
					context.runOnContext(v -> Future.<Void>succeededFuture().setHandler(handler));
				} else {
					context.runOnContext(v -> Future.<Void>failedFuture(leaveError).setHandler(handler));
				}
			});
		} else {
			context.runOnContext(v -> Future.<Void>succeededFuture().setHandler(handler));
		}
	}

	@Override
	public boolean isActive() {
		return active.get();
	}
}
