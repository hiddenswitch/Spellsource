package io.vertx.spi.cluster.etcd;

import static io.vertx.spi.cluster.etcd.impl.Codec.fromByteString;
import static io.vertx.spi.cluster.etcd.impl.Codec.toByteString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.etcd.jetcd.api.Event;
import io.etcd.jetcd.api.KVGrpc;
import io.etcd.jetcd.api.KeyValue;
import io.etcd.jetcd.api.LeaseGrantRequest;
import io.etcd.jetcd.api.LeaseGrantResponse;
import io.etcd.jetcd.api.LeaseGrpc;
import io.etcd.jetcd.api.LeaseKeepAliveRequest;
import io.etcd.jetcd.api.LeaseRevokeRequest;
import io.etcd.jetcd.api.LeaseRevokeResponse;
import io.etcd.jetcd.api.PutRequest;
import io.etcd.jetcd.api.PutResponse;
import io.etcd.jetcd.api.WatchCancelRequest;
import io.etcd.jetcd.api.WatchCreateRequest;
import io.etcd.jetcd.api.WatchGrpc;
import io.etcd.jetcd.api.WatchRequest;
import io.etcd.jetcd.api.WatchResponse;
import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.vertx.core.*;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;
import io.vertx.grpc.GrpcBidiExchange;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.spi.cluster.etcd.impl.CounterImpl;
import io.vertx.spi.cluster.etcd.impl.EtcdAsyncMapImpl;
import io.vertx.spi.cluster.etcd.impl.EtcdAsyncMultiMapImpl;
import io.vertx.spi.cluster.etcd.impl.EtcdSyncMapImpl;
import io.vertx.spi.cluster.etcd.impl.KeyPath;
import io.vertx.spi.cluster.etcd.impl.LockImpl;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class EtcdClusterManager implements ClusterManager {

	private static final long KEEP_ALIVE_TIME = 5l;
	private String host;
	private int port;
	private String prefix;
	private Vertx vertx;
	private ManagedChannel managedChannel;
	private ManagedChannel rawChannel;
	private NodeListener nodeListener;
	private KVGrpc.KVVertxStub kvStub;
	private LeaseGrpc.LeaseVertxStub leaseStub;
	private WatchGrpc.WatchVertxStub watchStub;
	private KeyPath nodePath;
	private ConcurrentHashMap<ByteString, String> nodeCache = new ConcurrentHashMap<>();
	private Long keepAliveTimerId;
	private Long sharedLease;
	private Long watchId;
	private volatile GrpcBidiExchange<WatchResponse, WatchRequest> watchExchange;
	private volatile boolean active;
	private volatile String nodeId;

	public EtcdClusterManager(String host, int port) {
		this(host, port, "vertx");
	}

	public EtcdClusterManager(String host, int port, String prefix) {
		this.host = host;
		this.port = port;
		this.prefix = prefix;
		this.nodePath = KeyPath.path(prefix + "/cluster/nodes");
	}

	@Override
	public void setVertx(Vertx vertx) {
		this.vertx = vertx;
	}

	@Override
	public <K, V> void getAsyncMultiMap(String name, Handler<AsyncResult<AsyncMultiMap<K, V>>> handler) {
		vertx.runOnContext((ignore) -> {
			long lease = name.equals("__vertx.subs") ? sharedLease : 0;
			EtcdAsyncMultiMapImpl<K, V> map = new EtcdAsyncMultiMapImpl<>(
					KeyPath.path(prefix + "/multimaps/" + name), lease, managedChannel);
			map.init(handler);
		});
	}

	@Override
	public <K, V> void getAsyncMap(String name, Handler<AsyncResult<AsyncMap<K, V>>> handler) {
		vertx.runOnContext((ignore) ->
				handler.handle(Future.succeededFuture(
						new EtcdAsyncMapImpl<>(KeyPath.path(prefix + "/maps/" + name), managedChannel)))
		);
	}

	@Override
	public <K, V> Map<K, V> getSyncMap(String name) {
		return new EtcdSyncMapImpl<>(KeyPath.path(prefix + "/maps/" + name), sharedLease, rawChannel);
	}

	@Override
	public void getLockWithTimeout(String name, long timeout, Handler<AsyncResult<Lock>> handler) {
		vertx.runOnContext((ignore) -> {
			LockImpl lock = new LockImpl(name, timeout, sharedLease, managedChannel, vertx);
			lock.aquire(handler);
		});
	}

	@Override
	public void getCounter(String name, Handler<AsyncResult<Counter>> handler) {
		vertx.runOnContext((ignore) ->
				handler.handle(Future.succeededFuture(
						new CounterImpl(prefix + "/counters/" + name, sharedLease, managedChannel)))
		);
	}

	@Override
	public String getNodeID() {
		return nodeId;
	}

	@Override
	public List<String> getNodes() {
		return new ArrayList<>(nodeCache.values());
	}

	@Override
	public void nodeListener(NodeListener listener) {
		this.nodeListener = listener;
	}

	@Override
	public void join(Handler<AsyncResult<Void>> handler) {
		vertx.runOnContext(v -> {
			nodeId = UUID.randomUUID().toString();
			managedChannel = VertxChannelBuilder.forAddress(vertx, host, port)
					.usePlaintext()
					.build();
			rawChannel = NettyChannelBuilder.forAddress(host, port)
					.usePlaintext()
					.build();
			leaseStub = LeaseGrpc.newVertxStub(managedChannel);
			kvStub = KVGrpc.newVertxStub(managedChannel);
			watchStub = WatchGrpc.newVertxStub(managedChannel);
			startKeepAlive()
					.compose(lease -> Future.<PutResponse>future(fut ->
							kvStub.put(PutRequest.newBuilder()
									.setLease(sharedLease)
									.setKey(nodePath.getKey(nodeId))
									.setValue(toByteString(nodeId))
									.build(), fut)))
					.compose(res -> {
						long rev = res.getHeader().getRevision();
						nodeCache.put(nodePath.getKey(nodeId), nodeId);
						return startWatch(rev);
					})
					.<Void>map(ignore -> {
						active = true;
						return null;
					})
					.onComplete(handler);
		});
	}

	@Override
	public void leave(Handler<AsyncResult<Void>> handler) {
		vertx.runOnContext(v ->
				stopWatch()
						.compose(ignore -> this.stopKeepAlive())
						.compose(ignore -> {
							Promise<Void> promise = Promise.promise();
							vertx.executeBlocking((blcFuture) -> {
								try {
									managedChannel.shutdownNow();
									managedChannel.awaitTermination(10, TimeUnit.SECONDS);
									rawChannel.shutdownNow();
									rawChannel.awaitTermination(10, TimeUnit.SECONDS);
									active = false;
									blcFuture.tryComplete();
								} catch (InterruptedException e) {
									blcFuture.tryFail(e);
								}
							}, promise);
							return promise.future();
						})
						.onComplete(handler)
		);
	}

	@Override
	public boolean isActive() {
		return active;
	}

	private Future<Void> startKeepAlive() {
		return Future.<LeaseGrantResponse>future(fut ->
				leaseStub.leaseGrant(
						LeaseGrantRequest.newBuilder()
								.setTTL(KEEP_ALIVE_TIME)
								.build(), fut))
				.map(res -> {
					sharedLease = res.getID();
					leaseStub.leaseKeepAlive(exchange ->
							keepAliveTimerId = vertx.setPeriodic(KEEP_ALIVE_TIME / 2, (ignore) -> {
								exchange.write(LeaseKeepAliveRequest.newBuilder()
										.setID(sharedLease)
										.build());
							}));
					return null;
				});
	}

	private Future<Void> stopKeepAlive() {
		if (keepAliveTimerId != null) {
			vertx.cancelTimer(keepAliveTimerId);
		}
		if (sharedLease == null) {
			return Future.succeededFuture();
		}
		return Future.<LeaseRevokeResponse>future(fut ->
				leaseStub.leaseRevoke(
						LeaseRevokeRequest.newBuilder()
								.setID(sharedLease)
								.build(), fut))
				.mapEmpty();
	}

	private Future<Void> startWatch(long rev) {
		Promise<Void> promise = Promise.promise();
		watchStub.watch(exchange -> {
			watchExchange = exchange;
			watchExchange
					.write(WatchRequest.newBuilder()
							.setCreateRequest(WatchCreateRequest.newBuilder()
									.setStartRevision(rev)
									.setKey(nodePath.rangeBegin())
									.setPrevKv(true)
									.setRangeEnd(nodePath.rangeEnd())
							)
							.build())
					.handler(watchRes -> {
						if (watchRes.getCreated()) {
							watchId = watchRes.getWatchId();
							promise.tryComplete();
						} else {
							this.handleEvents(watchRes.getEventsList());
						}
					})
					.exceptionHandler(promise::tryFail);
		});
		return promise.future();
	}

	private Future<Void> stopWatch() {
		if (watchId == null) {
			return Future.succeededFuture();
		}
		Promise<Void> promise = Promise.promise();
		watchExchange.write(
				WatchRequest.newBuilder()
						.setCancelRequest(
								WatchCancelRequest.newBuilder()
										.setWatchId(watchId)
						)
						.build())
				.handler(watchRes -> {
					if (watchRes.getCanceled()) {
						watchId = watchRes.getWatchId();
						promise.tryComplete();
					} else {
						this.handleEvents(watchRes.getEventsList());
					}
				})
				.exceptionHandler(promise::tryFail);
		return promise.future();
	}

	private void handleEvents(List<Event> events) {
		events.forEach(event -> {
			KeyValue kv = event.getKv();

			if (Objects.equals(kv.getKey(), nodePath.getKey(this.nodeId))) {
				return;
			}
			if (event.getType() == Event.EventType.PUT) {
				String nodeId = fromByteString(kv.getValue());
				nodeCache.put(kv.getKey(), nodeId);
				if (nodeListener != null) {
					nodeListener.nodeAdded(nodeId);
				}
			} else if (event.getType() == Event.EventType.DELETE) {
				nodeCache.remove(kv.getKey());
				if (nodeListener != null) {
					nodeListener.nodeLeft(nodeId);
				}
			}
		});
	}
}
