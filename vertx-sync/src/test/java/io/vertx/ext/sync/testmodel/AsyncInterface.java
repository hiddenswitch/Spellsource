package io.vertx.ext.sync.testmodel;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface AsyncInterface {

  // Non Handler<AsyncResult<>> methods

  String someMethod(String foo, long bar);

  // Methods with Handler<AsyncResult<>>

  void methodWithParamsAndHandlerNoReturn(String foo, long bar, Handler<AsyncResult<String>> resultHandler);

  void methodWithNoParamsAndHandlerNoReturn(Handler<AsyncResult<String>> resultHandler);

  String methodWithParamsAndHandlerWithReturn(String foo, long bar, Handler<AsyncResult<String>> resultHandler);

  String methodWithNoParamsAndHandlerWithReturn(Handler<AsyncResult<String>> resultHandler);

  void methodWithParamsAndHandlerInterface(String foo, long bar, Handler<AsyncResult<ReturnedInterface>> resultHandler);

  void methodThatFails(String foo, Handler<AsyncResult<String>> resultHandler);

  String methodWithNoParamsAndHandlerWithReturnTimeout(Handler<AsyncResult<String>> resultHandler, long timeout);


}
