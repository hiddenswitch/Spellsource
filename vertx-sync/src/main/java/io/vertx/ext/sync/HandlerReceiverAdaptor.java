package io.vertx.ext.sync;

import io.vertx.core.Handler;

/**
 *
 * Represents an object that is both a handler of a particular event and also a receiver of that event.
 * <p>
 * In other words it converts an asynchronous stream of events into a synchronous receiver of events
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface HandlerReceiverAdaptor<T> extends Handler<T>, Receiver<T> {
}
