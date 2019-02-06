package io.vertx.ext.sync;

import co.paralleluniverse.fibers.SuspendExecution;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface SuspendableRunnable {

  void run() throws SuspendExecution, InterruptedException;

}
