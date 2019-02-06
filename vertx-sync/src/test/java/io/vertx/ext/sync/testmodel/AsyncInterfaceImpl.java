package io.vertx.ext.sync.testmodel;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class AsyncInterfaceImpl implements AsyncInterface {

  private final Vertx vertx;

  public AsyncInterfaceImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public String someMethod(String foo, long bar) {
    return foo + bar;
  }

  @Override
  public void methodWithParamsAndHandlerNoReturn(String foo, long bar, Handler<AsyncResult<String>> resultHandler) {
    vertx.runOnContext(v -> resultHandler.handle(Future.succeededFuture(foo + bar)));
  }

  @Override
  public void methodWithNoParamsAndHandlerNoReturn(Handler<AsyncResult<String>> resultHandler) {
    vertx.runOnContext(v -> resultHandler.handle(Future.succeededFuture("wibble")));
  }

  @Override
  public String methodWithParamsAndHandlerWithReturn(String foo, long bar, Handler<AsyncResult<String>> resultHandler) {
    vertx.runOnContext(v -> resultHandler.handle(Future.succeededFuture(foo + bar)));
    return "ooble";
  }

  @Override
  public String methodWithNoParamsAndHandlerWithReturn(Handler<AsyncResult<String>> resultHandler) {
    vertx.runOnContext(v -> resultHandler.handle(Future.succeededFuture("wibble")));
    return "flooble";
  }

  @Override
  public void methodWithParamsAndHandlerInterface(String foo, long bar, Handler<AsyncResult<ReturnedInterface>> resultHandler) {
    vertx.runOnContext(v -> resultHandler.handle(Future.succeededFuture(new ReturnedInterfaceImpl(vertx))));
  }

  @Override
  public void methodThatFails(String foo, Handler<AsyncResult<String>> resultHandler) {
    vertx.runOnContext(v -> resultHandler.handle(Future.failedFuture(new Exception(foo))));
  }

  @Override
  public String methodWithNoParamsAndHandlerWithReturnTimeout(Handler<AsyncResult<String>> resultHandler, long timeout) {
	try {
		Thread.sleep(timeout);
	} catch(InterruptedException e) {
	}
    vertx.runOnContext(v -> resultHandler.handle(Future.succeededFuture("wibble")));
    return "flooble";
  }
}
