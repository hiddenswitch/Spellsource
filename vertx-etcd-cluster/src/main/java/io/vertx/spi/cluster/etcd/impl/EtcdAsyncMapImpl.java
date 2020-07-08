package io.vertx.spi.cluster.etcd.impl;

import static io.vertx.spi.cluster.etcd.impl.Codec.fromByteString;
import static io.vertx.spi.cluster.etcd.impl.Codec.toByteString;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.etcd.jetcd.api.Compare;
import io.etcd.jetcd.api.DeleteRangeRequest;
import io.etcd.jetcd.api.DeleteRangeResponse;
import io.etcd.jetcd.api.KVGrpc;
import io.etcd.jetcd.api.PutRequest;
import io.etcd.jetcd.api.PutResponse;
import io.etcd.jetcd.api.RangeRequest;
import io.etcd.jetcd.api.RangeResponse;
import io.etcd.jetcd.api.RequestOp;
import io.etcd.jetcd.api.TxnRequest;
import io.etcd.jetcd.api.TxnResponse;

import io.grpc.ManagedChannel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class EtcdAsyncMapImpl<K, V> implements AsyncMap<K, V> {

	private KVGrpc.KVVertxStub kvStub;

	private KeyPath keyPath;

	public EtcdAsyncMapImpl(KeyPath keyPath, ManagedChannel channel) {
		this.keyPath = keyPath;
		this.kvStub = KVGrpc.newVertxStub(channel);
	}

	@Override
	public void get(K k, Handler<AsyncResult<V>> handler) {
		Future.<RangeResponse>future(fut ->
				kvStub.range(
						RangeRequest.newBuilder()
								.setKey(keyPath.getKey(k))
								.build(), fut))
				.map(res ->
						res.getKvsList().stream()
								.findFirst()
								.<V>map(kv -> fromByteString(kv.getValue()))
								.orElse(null)
				)
				.onComplete(handler);
	}

	@Override
	public void keys(Handler<AsyncResult<Set<K>>> handler) {
		Future.<RangeResponse>future(fut ->
				kvStub.range(
						RangeRequest.newBuilder()
								.setKey(keyPath.rangeBegin())
								.setRangeEnd(keyPath.rangeEnd())
								.build(), fut))
				.map(res ->
						res.getKvsList().stream()
								.<K>map(kv -> keyPath.getRawKey(kv.getKey()))
								.filter(Objects::nonNull)
								.collect(toSet())
				)
				.onComplete(handler);
	}

	@Override
	public void values(Handler<AsyncResult<List<V>>> handler) {
		Future.<RangeResponse>future(fut ->
				kvStub.range(
						RangeRequest.newBuilder()
								.setKey(keyPath.rangeBegin())
								.setRangeEnd(keyPath.rangeEnd())
								.build(), fut))
				.map(res ->
						res.getKvsList().stream()
								.<V>map(kv -> fromByteString(kv.getValue()))
								.filter(Objects::nonNull)
								.collect(toList())
				)
				.onComplete(handler);
	}

	@Override
	public void entries(Handler<AsyncResult<Map<K, V>>> handler) {
		Future.<RangeResponse>future(fut ->
				kvStub.range(
						RangeRequest.newBuilder()
								.setKey(keyPath.rangeBegin())
								.setRangeEnd(keyPath.rangeEnd())
								.build(), fut))
				.<Map<K, V>>map(res ->
						res.getKvsList().stream()
								.collect(toMap(
										kv -> keyPath.getRawKey(kv.getKey()),
										kv -> fromByteString(kv.getValue())
								))
				)
				.onComplete(handler);
	}

	@Override
	public void put(K k, V v, Handler<AsyncResult<Void>> handler) {
		Future.<PutResponse>future(fut ->
				kvStub.put(
						PutRequest.newBuilder()
								.setKey(keyPath.getKey(k))
								.setValue(toByteString(v))
								.build(), fut))
				.<Void>mapEmpty()
				.onComplete(handler);
	}

	@Override
	public void put(K k, V v, long ttl, Handler<AsyncResult<Void>> handler) {
		//TODO submit to ttl scheduler
		Future.<PutResponse>future(fut ->
				kvStub.put(
						PutRequest.newBuilder()
								.setKey(keyPath.getKey(k))
								.setValue(toByteString(v))
								.build(), fut))
				.<Void>mapEmpty()
				.onComplete(handler);
	}

	@Override
	public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> handler) {
		Future.<TxnResponse>future(fut ->
				kvStub.txn(
						TxnRequest.newBuilder()
								.addCompare(Compare.newBuilder()
										.setKey(keyPath.getKey(k))
										.setTarget(Compare.CompareTarget.VERSION)
										.setResult(Compare.CompareResult.EQUAL)
										.setVersion(0)
								)
								.addSuccess(RequestOp.newBuilder()
										.setRequestPut(PutRequest.newBuilder()
												.setKey(keyPath.getKey(k))
												.setValue(toByteString(v)))
								)
								.addFailure(RequestOp.newBuilder()
										.setRequestRange(RangeRequest.newBuilder()
												.setKey(keyPath.getKey(k)))
								)
								.build(), fut))
				.<V>map(res -> {
					if (res.getSucceeded()) {
						return null;
					}
					return fromByteString(res.getResponses(0)
							.getResponseRange().getKvs(0).getValue());
				})
				.onComplete(handler);
	}

	@Override
	public void putIfAbsent(K k, V v, long ttl, Handler<AsyncResult<V>> handler) {
		//TODO submit to ttl scheduler
		Future.<TxnResponse>future(fut ->
				kvStub.txn(
						TxnRequest.newBuilder()
								.addCompare(Compare.newBuilder()
										.setKey(keyPath.getKey(k))
										.setTarget(Compare.CompareTarget.VERSION)
										.setResult(Compare.CompareResult.EQUAL)
										.setVersion(0)
								)
								.addSuccess(RequestOp.newBuilder()
										.setRequestPut(PutRequest.newBuilder()
												.setKey(keyPath.getKey(k))
												.setValue(toByteString(v)))
								)
								.addFailure(RequestOp.newBuilder()
										.setRequestRange(RangeRequest.newBuilder()
												.setKey(keyPath.getKey(k)))
								)
								.build(), fut))
				.<V>map(res -> {
					if (res.getSucceeded()) {
						return null;
					}
					return fromByteString(res.getResponses(0)
							.getResponseRange().getKvs(0).getValue());
				})
				.onComplete(handler);
	}

	@Override
	public void remove(K k, Handler<AsyncResult<V>> handler) {
		Future.<DeleteRangeResponse>future(fut ->
				kvStub.deleteRange(
						DeleteRangeRequest.newBuilder()
								.setKey(keyPath.getKey(k))
								.setPrevKv(true)
								.build(), fut))
				.map(res -> res.getPrevKvsList()
						.stream()
						.findFirst()
						.<V>map(kv -> fromByteString(kv.getValue()))
						.orElse(null)
				)
				.onComplete(handler);
	}

	@Override
	public void removeIfPresent(K k, V v, Handler<AsyncResult<Boolean>> handler) {
		Future.<TxnResponse>future(fut ->
				kvStub.txn(
						TxnRequest.newBuilder()
								.addCompare(Compare.newBuilder()
										.setKey(keyPath.getKey(k))
										.setTarget(Compare.CompareTarget.VALUE)
										.setResult(Compare.CompareResult.EQUAL)
										.setValue(toByteString(v))
								)
								.addSuccess(RequestOp.newBuilder()
										.setRequestDeleteRange(DeleteRangeRequest.newBuilder()
												.setKey(keyPath.getKey(k)))
								)
								.build(), fut))
				.map(TxnResponse::getSucceeded)
				.onComplete(handler);
	}

	@Override
	public void replace(K k, V v, Handler<AsyncResult<V>> handler) {
		Future.<TxnResponse>future(fut ->
				kvStub.txn(
						TxnRequest.newBuilder()
								.addCompare(Compare.newBuilder()
										.setKey(keyPath.getKey(k))
										.setTarget(Compare.CompareTarget.VERSION)
										.setResult(Compare.CompareResult.GREATER)
										.setVersion(0)
								)
								.addSuccess(RequestOp.newBuilder()
										.setRequestPut(PutRequest.newBuilder()
												.setKey(keyPath.getKey(k))
												.setPrevKv(true)
												.setValue(toByteString(v)))
								)
								.build(), fut))
				.<V>map(res -> {
					if (!res.getSucceeded()) {
						return null;
					} else {
						return fromByteString(res.getResponses(0)
								.getResponsePut().getPrevKv().getValue());
					}
				})
				.onComplete(handler);
	}

	@Override
	public void replaceIfPresent(K k, V oldValue, V newValue, Handler<AsyncResult<Boolean>> handler) {
		Future.<TxnResponse>future(fut ->
				kvStub.txn(
						TxnRequest.newBuilder()
								.addCompare(Compare.newBuilder()
										.setKey(keyPath.getKey(k))
										.setTarget(Compare.CompareTarget.VALUE)
										.setResult(Compare.CompareResult.EQUAL)
										.setValue(toByteString(oldValue))
								)
								.addSuccess(RequestOp.newBuilder()
										.setRequestPut(PutRequest.newBuilder()
												.setKey(keyPath.getKey(k))
												.setValue(toByteString(newValue)))
								)
								.build(), fut))
				.map(TxnResponse::getSucceeded)
				.onComplete(handler);
	}

	@Override
	public void clear(Handler<AsyncResult<Void>> handler) {
		Future.<DeleteRangeResponse>future(fut ->
				kvStub.deleteRange(
						DeleteRangeRequest.newBuilder()
								.setKey(keyPath.rangeBegin())
								.setRangeEnd(keyPath.rangeEnd())
								.build(), fut))
				.<Void>mapEmpty()
				.onComplete(handler);
	}

	@Override
	public void size(Handler<AsyncResult<Integer>> handler) {
		Future.<RangeResponse>future(fut ->
				kvStub.range(
						RangeRequest.newBuilder()
								.setKey(keyPath.rangeBegin())
								.setRangeEnd(keyPath.rangeEnd())
								.setCountOnly(true)
								.build(), fut))
				.map(res -> (int) res.getCount())
				.onComplete(handler);
	}

}
