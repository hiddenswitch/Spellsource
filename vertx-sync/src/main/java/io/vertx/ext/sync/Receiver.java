package io.vertx.ext.sync;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.ReceivePort;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a synchronous receiver of events.
 * <p>
 * Note that the `receive` methods may block the calling fiber but will not block an underlying kernel thread.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface Receiver<T> {

  /**
   * @return the underlying Quasar receivePort
   */
  ReceivePort<T> receivePort();

  /**
   * Return an event when one is available. This method will block the fiber until one is available.
   * No kernel thread is blocked.
   *
   * @return  the event
   */
  @Suspendable
  @Nullable
  T receive();

  /**
   * Return an event when one is available. This method will block the fiber until one is available, or timeout occurs.
   * No kernel thread is blocked.
   *
   * @param timeout  the max amount of time in ms to wait for an event to be available
   * @return  the event
   */
  @Suspendable
  T receive(long timeout);
}
