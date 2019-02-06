package io.vertx.ext.sync.testmodel;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

public interface ReturnedInterface {

  void methodWithParamsAndHandlerNoReturn(String foo, long bar, Handler<AsyncResult<String>> resultHandler);

}
