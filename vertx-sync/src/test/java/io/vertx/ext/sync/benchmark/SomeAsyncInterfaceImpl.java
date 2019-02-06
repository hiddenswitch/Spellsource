package io.vertx.ext.sync.benchmark;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SomeAsyncInterfaceImpl implements SomeAsyncInterface {

  private final Vertx vertx;

  public SomeAsyncInterfaceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void asyncMethod(String str, Handler<AsyncResult<String>> resultHandler) {
    vertx.runOnContext(v -> resultHandler.handle(Future.succeededFuture("done")));
  }
}
