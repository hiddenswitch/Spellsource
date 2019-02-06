package io.vertx.ext.sync.benchmark;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Vertx;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.sync.impl.AsyncAdaptor;

import static io.vertx.ext.sync.Sync.fiberHandler;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SyncBenchmarkVerticle extends SyncVerticle {

  private Benchmarker benchmarker = new Benchmarker(100000);

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(SyncBenchmarkVerticle.class.getName());
  }

  private SomeAsyncInterface ai;

  @Override
  @Suspendable
  public void start() {

    ai = new SomeAsyncInterfaceImpl(vertx);

    benchmarkMethod();

  }

  @Suspendable
  protected void benchmarkMethod()  {

    try {
      AsyncAdaptor<String> aa = new AsyncAdaptor<String>() {
        @Override
        protected void requestAsync() {
          ai.asyncMethod("foo", this);
        }
      };

      String result = aa.run();

      benchmarker.iterDone(result.hashCode());

      vertx.runOnContext(fiberHandler(v -> benchmarkMethod()));

    } catch (Throwable t) {
      t.printStackTrace();
    }
  }


}
