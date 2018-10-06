package io.vertx.ext.sync;

import com.github.fromage.quasi.fibers.SuspendExecution;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface SuspendableRunnable {

  void run() throws SuspendExecution, InterruptedException;

}
