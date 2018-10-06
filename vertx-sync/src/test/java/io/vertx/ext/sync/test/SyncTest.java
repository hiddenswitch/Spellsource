package io.vertx.ext.sync.test;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SyncTest extends VertxTestBase {

  protected void runTest(String testName) throws Exception {

    vertx.deployVerticle(new TestVerticle(),
      new DeploymentOptions().setConfig(new JsonObject().put("testName", testName)), res -> {
        if (res.succeeded()) {
          vertx.undeploy(res.result(), res2 -> {
            if (res2.succeeded()) {
              testComplete();
            } else {
              res2.cause().printStackTrace();
              fail("Failure in undeploying");
            }
          });
        } else {
          res.cause().printStackTrace();
          fail("Failure in running tests");
        }
      });

    await();
  }

  protected String getMethodName() {
    return Thread.currentThread().getStackTrace()[2].getMethodName();
  }

  // Test fiber handler

  @Test
  public void testFiberHandler() throws Exception {
    runTest(getMethodName());
  }

  // Test exec sync

  @Test
  public void testExecSyncMethodWithParamsAndHandlerNoReturn() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testExecSyncMethodWithNoParamsAndHandlerNoReturn() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testExecSyncMethodWithParamsAndHandlerWithReturn() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testExecSyncMethodWithNoParamsAndHandlerWithReturn() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testExecSyncMethodWithParamsAndHandlerInterface() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testExecSyncMethodWithNoParamsAndHandlerWithReturnNoTimeout() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testExecSyncMethodWithNoParamsAndHandlerWithReturnTimedout() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testExecSyncMethodThatFails() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testExecNestedSyncMethods() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testExecSyncWithAwaitFiber() throws Exception {
    runTest(getMethodName());
  }

  // Test receive single event

  @Test
  public void testReceiveEvent() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testReceiveEventTimedout() throws Exception {
    runTest(getMethodName());
  }

  @Test
  public void testReceiveEventNoTimeout() throws Exception {
    runTest(getMethodName());
  }

  // Test Channels

  @Test
  public void testHandlerAdaptor() throws Exception {
    runTest(getMethodName());
  }

  // Various misc

  @Test
  public void testSleep() throws Exception {
    runTest(getMethodName());
  }


}
