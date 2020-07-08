package io.vertx.spi.cluster.etcd.impl;

import static com.google.common.base.Preconditions.checkState;

import io.etcd.jetcd.api.Compare;
import io.etcd.jetcd.api.DeleteRangeRequest;
import io.etcd.jetcd.api.Event;
import io.etcd.jetcd.api.KVGrpc;
import io.etcd.jetcd.api.PutRequest;
import io.etcd.jetcd.api.RequestOp;
import io.etcd.jetcd.api.TxnRequest;
import io.etcd.jetcd.api.TxnResponse;
import io.etcd.jetcd.api.WatchCancelRequest;
import io.etcd.jetcd.api.WatchCreateRequest;
import io.etcd.jetcd.api.WatchGrpc;
import io.etcd.jetcd.api.WatchRequest;
import io.etcd.jetcd.api.WatchResponse;
import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.shareddata.Lock;
import io.vertx.grpc.GrpcBidiExchange;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class LockImpl implements Lock {

  private Vertx vertx;
  private ByteString key;
  private KVGrpc.KVVertxStub kvStub;
  private WatchGrpc.WatchVertxStub watchStub;
  private Handler<AsyncResult<Lock>> handler;
  private Long timerId;
  private Long watcherId;
  private long timeout;
  private long sharedLease;
  private GrpcBidiExchange<WatchResponse, WatchRequest> exchange;
  private volatile State state = State.NEW;

  public LockImpl(String name, long timeout, long sharedLease,
                  ManagedChannel channel, Vertx vertx) {
    this.vertx = vertx;
    this.key = ByteString.copyFromUtf8(name);
    this.timeout = timeout;
    this.sharedLease = sharedLease;
    kvStub = KVGrpc.newVertxStub(channel);
    watchStub = WatchGrpc.newVertxStub(channel);
  }

  public void aquire(Handler<AsyncResult<Lock>> handler) {
    this.handler = handler;
    state = State.WAITING;
    startTimer();
    tryAquire();
  }

  @Override
  public void release() {
    checkState(state == State.LOCKED);
    kvStub.deleteRange(
      DeleteRangeRequest.newBuilder()
        .setKey(key)
        .build(), (ar) -> {});
  }

  private void tryAquire() {
    Future.<TxnResponse>future(fut ->
      kvStub.txn(
        TxnRequest.newBuilder()
          .addCompare(Compare.newBuilder()
            .setKey(key)
            .setTarget(Compare.CompareTarget.VERSION)
            .setResult(Compare.CompareResult.LESS)
            .setVersion(1)
          )
          .addSuccess(RequestOp.newBuilder()
            .setRequestPut(PutRequest.newBuilder()
              .setKey(key)
              .setValue(ByteString.EMPTY)
              .setLease(sharedLease))
          )
          .build(), fut)
      )
      .onComplete(ar -> {
        if (ar.failed()) {
          triggerFailed(ar.cause());
          return;
        }
        if (ar.result().getSucceeded()) {
          triggerLocked();
        } else {
          startWatching();
        }
      });
  }

  private void startWatching() {
    watchStub.watch(newExchange -> {
      if (state != State.WAITING) return;
      exchange = newExchange;
      exchange.write(WatchRequest.newBuilder()
        .setCreateRequest(WatchCreateRequest.newBuilder().setKey(key))
        .build());
      exchange.handler(watchRes -> {
        if (state != State.WAITING) return;
        if (watchRes.getCreated()) {
          watcherId = watchRes.getWatchId();
          return;
        }
        if (!watchRes.getEventsList().stream().anyMatch((
            event -> event.getType() == Event.EventType.DELETE))) {
          return;
        }
        tryAquire();
      });
    });
  }

  private void startTimer() {
    timerId = vertx.setTimer(timeout,
      (ignore) -> triggerFailed(
        new VertxException("Lock aquire timeout:" + key.toStringUtf8())));
  }

  private void triggerLocked() {
    if (state != State.WAITING) {
      return;
    }
    cleanup();
    state = State.LOCKED;
    handler.handle(Future.succeededFuture(this));
    exchange.end();
  }

  private void triggerFailed(Throwable t) {
    cleanup();
    state = State.FAILED;
    handler.handle(Future.failedFuture(t));
  }

  private void cleanup() {
    if (timerId != null) {
      vertx.cancelTimer(timerId);
    }
    timerId = null;
    if (watcherId != null) {
      exchange.write(WatchRequest.newBuilder()
        .setCancelRequest(WatchCancelRequest.newBuilder()
          .setWatchId(watcherId))
        .build()).end();
    }
  }

  private enum State {
    NEW, LOCKED, WAITING, FAILED
  }

}
