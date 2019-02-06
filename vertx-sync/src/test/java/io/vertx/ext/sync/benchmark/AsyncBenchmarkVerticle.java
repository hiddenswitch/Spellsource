package io.vertx.ext.sync.benchmark;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class AsyncBenchmarkVerticle extends AbstractVerticle {

  private Benchmarker benchmarker = new Benchmarker(100000);

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(AsyncBenchmarkVerticle.class.getName());
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

    ai.asyncMethod("foo", res -> {
      if (res.succeeded()) {

        String result = res.result();

        benchmarker.iterDone(result.hashCode());

        vertx.runOnContext(v -> benchmarkMethod());

      } else {
        res.cause().printStackTrace();
      }
    });

  }


}
