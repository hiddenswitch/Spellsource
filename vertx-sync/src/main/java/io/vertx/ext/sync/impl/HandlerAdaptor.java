package io.vertx.ext.sync.impl;

import com.github.fromage.quasi.fibers.FiberAsync;
import io.vertx.core.Handler;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class HandlerAdaptor<T> extends FiberAsync<T, Throwable> implements Handler<T>  {

  @Override
  public void handle(T res) {
    asyncCompleted(res);
  }
}
