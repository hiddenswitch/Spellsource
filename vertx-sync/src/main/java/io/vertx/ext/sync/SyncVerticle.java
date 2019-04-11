package io.vertx.ext.sync;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

/**
 * A `Verticle` which runs its `start` and `stop` methods using fibers.
 *
 * You should subclass this class instead of `AbstractVerticle` to create any verticles that use vertx-sync.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class SyncVerticle extends AbstractVerticle {

  protected FiberScheduler instanceScheduler;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    instanceScheduler = Sync.getContextScheduler();
    new Fiber<Void>(null, instanceScheduler, Sync.DEFAULT_STACK_SIZE, () -> {
      try {
        SyncVerticle.this.start();
        startFuture.complete();
      } catch (Throwable t) {
        startFuture.fail(t);
      }
    }).start();
  }

  @Override
  public void stop(Future<Void> stopFuture) throws Exception {
    new Fiber<Void>(null, instanceScheduler, Sync.DEFAULT_STACK_SIZE, () -> {
      try {
        SyncVerticle.this.stop();
        stopFuture.complete();
      } catch (Throwable t) {
        stopFuture.fail(t);
      } finally {
        Sync.removeContextScheduler();
      }
    }).start();
  }

  /**
   * Override this method in your verticle
   */
  @Override
  @Suspendable
  public void start() throws Exception {
  }

  /**
   * Optionally override this method in your verticle if you have cleanup to do
   */
  @Override
  @Suspendable
  public void stop() throws Exception {
  }

}
