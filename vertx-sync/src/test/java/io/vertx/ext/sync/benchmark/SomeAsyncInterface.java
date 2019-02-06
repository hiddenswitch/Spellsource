package io.vertx.ext.sync.benchmark;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface SomeAsyncInterface {

  void asyncMethod(String str, Handler<AsyncResult<String>> resultHandler);
}
