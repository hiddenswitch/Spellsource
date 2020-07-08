package io.vertx.spi.cluster.etcd.impl;

import static io.vertx.spi.cluster.etcd.impl.Codec.toByteString;

import javax.annotation.Nonnull;

import io.etcd.jetcd.api.Compare;
import io.etcd.jetcd.api.DeleteRangeRequest;
import io.etcd.jetcd.api.KVGrpc;
import io.etcd.jetcd.api.PutRequest;
import io.etcd.jetcd.api.RangeRequest;
import io.etcd.jetcd.api.RangeResponse;
import io.etcd.jetcd.api.RequestOp;
import io.etcd.jetcd.api.TxnRequest;
import io.etcd.jetcd.api.TxnResponse;
import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.Counter;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class CounterImpl implements Counter {

	private ByteString key;

	private KVGrpc.KVVertxStub kvStub;

	private long sharedLease;

	public CounterImpl(String name, long sharedLease, ManagedChannel channel) {
		this.sharedLease = sharedLease;
		this.key = ByteString.copyFromUtf8(name);
		kvStub = KVGrpc.newVertxStub(channel);
	}

	@Override
	public void get(@Nonnull Handler<AsyncResult<Long>> handler) {
		Future.<RangeResponse>future(fut ->
				kvStub.range(RangeRequest.newBuilder()
						.setKey(key)
						.build(), fut))
				.map(res ->
						res.getKvsCount() > 0
								? Codec.fromByteString(res.getKvs(0).getValue())
								: 0L
				)
				.onComplete(handler);
	}

	@Override
	public void incrementAndGet(@Nonnull Handler<AsyncResult<Long>> handler) {
		addAndGet(1, handler);
	}

	@Override
	public void getAndIncrement(@Nonnull Handler<AsyncResult<Long>> handler) {
		getAndAdd(1, handler);
	}

	@Override
	public void decrementAndGet(@Nonnull Handler<AsyncResult<Long>> handler) {
		addAndGet(-1, handler);
	}

	@Override
	public void addAndGet(long increment, @Nonnull Handler<AsyncResult<Long>> handler) {
		Future.<Long>future(this::get)
				.compose(oldVal -> {
					long newVal = oldVal + increment;
					return Future.<Boolean>future(fut -> compareAndSet(oldVal, newVal, fut))
							.compose(casRes -> {
								if (casRes) {
									return Future.succeededFuture(newVal);
								} else {
									return Future.future(fut -> addAndGet(increment, fut));
								}
							});
				})
				.onComplete(handler);
	}

	@Override
	public void getAndAdd(long value, @Nonnull Handler<AsyncResult<Long>> handler) {
		Future.<Long>future(this::get)
				.compose(expected -> Future.<Boolean>future(fut ->
						compareAndSet(expected, value, fut)))
				.compose(casRes -> {
					if (casRes) {
						return Future.succeededFuture(value);
					} else {
						return Future.future(fut -> addAndGet(value, fut));
					}
				})
				.onComplete(handler);
	}

	@Override
	public void compareAndSet(long expected, long value,
	                          @Nonnull Handler<AsyncResult<Boolean>> handler) {
		Compare.Builder compare = expected == 0 ?
				Compare.newBuilder()
						.setKey(key)
						.setTarget(Compare.CompareTarget.VERSION)
						.setResult(Compare.CompareResult.EQUAL)
						.setVersion(0) :
				Compare.newBuilder()
						.setKey(key)
						.setTarget(Compare.CompareTarget.VALUE)
						.setResult(Compare.CompareResult.EQUAL)
						.setValue(toByteString(expected));
		RequestOp.Builder successOp = RequestOp.newBuilder();
		if (value == 0) {
			successOp.setRequestDeleteRange(
					DeleteRangeRequest.newBuilder()
							.setKey(key)
							.build()
			);
		} else {
			successOp.setRequestPut(
					PutRequest.newBuilder()
							.setKey(key)
							.setValue(toByteString(value))
							.setLease(sharedLease)
							.setPrevKv(true)
							.build()
			);
		}
		Future.<TxnResponse>future(fut ->
				kvStub.txn(TxnRequest.newBuilder()
						.addCompare(compare)
						.addSuccess(successOp)
						.addFailure(RequestOp.newBuilder()
								.setRequestRange(RangeRequest.newBuilder().setKey(key))
						)
						.build(), fut))
				.map(TxnResponse::getSucceeded)
				.onComplete(handler);
	}

}
