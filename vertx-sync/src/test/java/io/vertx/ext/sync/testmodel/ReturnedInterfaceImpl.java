package io.vertx.ext.sync.testmodel;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class ReturnedInterfaceImpl implements ReturnedInterface {

  private final Vertx vertx;

  public ReturnedInterfaceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void methodWithParamsAndHandlerNoReturn(String foo, long bar, Handler<AsyncResult<String>> resultHandler) {
    vertx.runOnContext(v -> resultHandler.handle(Future.succeededFuture(foo + bar)));
  }
}
