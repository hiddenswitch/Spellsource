package io.vertx.spi.cluster.etcd.impl;

import static io.vertx.spi.cluster.etcd.impl.Codec.fromByteString;
import static io.vertx.spi.cluster.etcd.impl.Codec.toByteString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.etcd.jetcd.api.Compare;
import io.etcd.jetcd.api.DeleteRangeRequest;
import io.etcd.jetcd.api.DeleteRangeResponse;
import io.etcd.jetcd.api.Event;
import io.etcd.jetcd.api.KVGrpc;
import io.etcd.jetcd.api.KeyValue;
import io.etcd.jetcd.api.PutRequest;
import io.etcd.jetcd.api.PutResponse;
import io.etcd.jetcd.api.RangeRequest;
import io.etcd.jetcd.api.RangeResponse;
import io.etcd.jetcd.api.RequestOp;
import io.etcd.jetcd.api.TxnRequest;
import io.etcd.jetcd.api.TxnResponse;
import io.etcd.jetcd.api.WatchCreateRequest;
import io.etcd.jetcd.api.WatchGrpc;
import io.etcd.jetcd.api.WatchRequest;

import io.grpc.ManagedChannel;
import io.vertx.core.*;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class EtcdAsyncMultiMapImpl<K, V> implements AsyncMultiMap<K, V> {

	private KVGrpc.KVVertxStub kvStub;
	private WatchGrpc.WatchVertxStub watchStub;
	private KeyPath keyPath;
	private long lease;
	private ConcurrentHashMap<K, Set<V>> cacheMap = new ConcurrentHashMap<>();
	private ConcurrentMap<K, KeyValueIterable> iterableMap = new ConcurrentHashMap<>();

	public EtcdAsyncMultiMapImpl(KeyPath keyPath, long lease, ManagedChannel channel) {
		this.lease = lease;
		this.keyPath = keyPath;
		this.kvStub = KVGrpc.newVertxStub(channel);
		this.watchStub = WatchGrpc.newVertxStub(channel);
	}

	private static Future<Void> awaitAdd(Void ignored1) {
		// TODO:
		Promise<Void> promise = Promise.promise();
		Vertx.currentContext().owner().setTimer(2000, ignored2 -> promise.complete());
		return promise.future();
	}

	@Override
	public void add(K k, V v, Handler<AsyncResult<Void>> handler) {
		Future.<PutResponse>future(fut ->
				kvStub.put(
						PutRequest.newBuilder()
								.setKey(keyPath.getKey(k, v))
								.setValue(toByteString(v))
								.setLease(lease)
								.build(), fut))
				.<Void>map(res -> {
					getItems(k).add(v);
					return null;
				})
				.compose(EtcdAsyncMultiMapImpl::awaitAdd)
				.onComplete(handler);
	}

	@Override
	public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> handler) {
		handler.handle(
				Future.succeededFuture(
						iterableMap.computeIfAbsent(k, KeyValueIterable::new))
		);
	}

	@Override
	public void remove(K k, V v, Handler<AsyncResult<Boolean>> handler) {
		Future.<DeleteRangeResponse>future(fut ->
				kvStub.deleteRange(
						DeleteRangeRequest.newBuilder()
								.setKey(keyPath.getKey(k, v))
								.build(), fut))
				.map(res -> {
					getItems(k).remove(v);
					return res.getDeleted() > 0;
				})
				.onComplete(handler);
	}

	@Override
	public void removeAllForValue(V v, Handler<AsyncResult<Void>> handler) {
		removeAllMatching((value) -> value.equals(v), handler);
	}

	@Override
	public void removeAllMatching(Predicate<V> p, Handler<AsyncResult<Void>> handler) {
		Future.<RangeResponse>future(fut ->
				kvStub.range(
						RangeRequest.newBuilder()
								.setKey(keyPath.rangeBegin())
								.setRangeEnd(keyPath.rangeEnd())
								.build(), fut))
				.compose(res -> Future.<TxnResponse>future(fut -> {
					TxnRequest.Builder txn = TxnRequest.newBuilder();
					res.getKvsList().stream()
							.filter(kv -> p.test(fromByteString(kv.getValue())))
							.forEach(kv ->
									txn.addCompare(
											Compare.newBuilder()
													.setModRevision(kv.getModRevision())
													.setTarget(Compare.CompareTarget.MOD)
													.setResult(Compare.CompareResult.EQUAL)
													.setKey(kv.getKey()))
											.addSuccess(
													RequestOp.newBuilder()
															.setRequestDeleteRange(
																	DeleteRangeRequest.newBuilder()
																			.setKey(kv.getKey())))
							);
					kvStub.txn(txn.build(), fut);
				}))
				.compose(res -> {
					if (res.getSucceeded()) {
						cacheMap.values()
								.forEach(set -> set.removeIf(p));
						return Future.succeededFuture();
					}
					return Future.<AsyncResult<Void>>future(fut -> removeAllMatching(p, handler));
				})
				.<Void>mapEmpty()
				.onComplete(handler);
	}

	public void init(Handler<AsyncResult<AsyncMultiMap<K, V>>> handler) {
		Future.<RangeResponse>future(fut ->
				kvStub.range(
						RangeRequest.newBuilder()
								.setKey(keyPath.rangeBegin())
								.setRangeEnd(keyPath.rangeEnd())
								.build(), fut))
				.map(res -> {
					cacheMap = res.getKvsList().stream()
							.collect(groupingBy(
									entry -> keyPath.getRawKey(entry.getKey()),
									ConcurrentHashMap::new,
									Collectors.mapping(
											entry -> fromByteString(entry.getValue()),
											toSet()
									)));
					return res.getHeader().getRevision();
				})
				.compose(rev -> {
					Promise<Void> promise = Promise.promise();
					watchStub.watch(exchange ->
							exchange
									.write(WatchRequest.newBuilder()
											.setCreateRequest(WatchCreateRequest.newBuilder()
													.setStartRevision(rev)
													.setKey(keyPath.rangeBegin())
													.setRangeEnd(keyPath.rangeEnd()))
											.build()
									)
									.handler(watchRes -> {
										if (watchRes.getCreated()) {
											promise.complete();
										} else {
											watchRes.getEventsList().forEach(this::handleEvent);
										}
									})
									.exceptionHandler(promise::tryFail)
					);
					return promise.future();
				})
				.<AsyncMultiMap<K, V>>map(this)
				.onComplete(handler);
	}

	private void handleEvent(Event event) {
		KeyValue kv = event.getKv();
		switch (event.getType()) {
			case DELETE:
				V val = fromByteString(kv.getValue());
				cacheMap.computeIfPresent(
						keyPath.getRawKey(kv.getKey()),
						(k, set) -> set.stream()
								.filter(v -> !Objects.equals(v, val))
								.collect(toSet())
				);
				break;
			case PUT:
				cacheMap.computeIfAbsent(
						keyPath.getRawKey(kv.getKey()),
						(k) -> new HashSet<>())
						.add(fromByteString(kv.getValue()));
				break;
		}
	}

	private Set<V> getItems(K key) {
		return cacheMap
				.computeIfAbsent(key, (ignore) -> new HashSet<>());
	}

	private class KeyValueIterable implements ChoosableIterable<V> {

		private K key;

		private AtomicInteger counter = new AtomicInteger(0);

		KeyValueIterable(K key) {
			this.key = key;
		}

		@Override
		public boolean isEmpty() {
			return !cacheMap.containsKey(key)
					|| cacheMap.get(key).isEmpty();
		}

		@Override
		public V choose() {
			List<V> items = getList();
			if (items.isEmpty()) {
				return null;
			}
			return items.get(Math.abs(counter.getAndIncrement()) % items.size());
		}

		@Override
		@Nonnull
		public Iterator<V> iterator() {
			return getList().iterator();
		}

		private List<V> getList() {
			return new ArrayList<>(getItems(key));
		}
	}
}
