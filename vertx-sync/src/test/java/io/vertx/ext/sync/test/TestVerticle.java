package io.vertx.ext.sync.test;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.ReceivePort;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.sync.HandlerReceiverAdaptor;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.sync.testmodel.AsyncInterface;
import io.vertx.ext.sync.testmodel.AsyncInterfaceImpl;
import io.vertx.ext.sync.testmodel.ReturnedInterface;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static io.vertx.ext.sync.Sync.*;

import static org.hamcrest.core.Is.*;

/**
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TestVerticle extends SyncVerticle {

  private static final String ADDRESS1 = "address1";
  private static final String ADDRESS2 = "address2";
  private static final String ADDRESS3 = "address3";

  private AsyncInterface ai;
  private Channel<Object> completeChannel;

  @Override
  @Suspendable
  public void start() throws Exception {

    ai = new AsyncInterfaceImpl(vertx);

    completeChannel = Channels.newChannel(1, Channels.OverflowPolicy.THROW);

    try {

      String testName = config().getString("testName");

      Method meth = this.getClass().getDeclaredMethod(testName);
      meth.setAccessible(true);
      meth.invoke(this);

    } catch (AssertionError e) {
      e.printStackTrace();
      throw new IllegalStateException("Tests failed", e);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException("Failed to invoke test", e);
    }

    completeChannel.receive(10, TimeUnit.SECONDS);

  }

  @Suspendable
  protected void complete() {
    try {
      completeChannel.send(new Object());
    } catch (Exception e) {
      throw new VertxException(e);
    }
  }

  @Suspendable
  protected void testContext() {
    Context ctx = Vertx.currentContext();
    assertTrue(ctx.isEventLoopContext());
    complete();
  }

  @Suspendable
  protected void testSleep() throws Exception {
    Thread th = Thread.currentThread();
    AtomicInteger cnt = new AtomicInteger();
    vertx.setPeriodic(1, tid -> {
      assertSame(Thread.currentThread(), th);
      cnt.incrementAndGet();
    });
    assertSame(Thread.currentThread(), th);
    Strand.sleep(1000, TimeUnit.MILLISECONDS);
    assertSame(Thread.currentThread(), th);
    assertTrue(cnt.get() > 900);
    complete();
  }

  @Suspendable
  protected void testFiberHandler() {
    HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8080));
    server.requestHandler(fiberHandler(req -> {
      String res = awaitResult(h -> ai.methodWithParamsAndHandlerNoReturn("oranges", 23, h));
      assertEquals("oranges23", res);
      req.response().end();
    }));
    server.listen(res -> {
      assertTrue(res.succeeded());
      HttpClient client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080));
        client.getNow("/somepath", resp -> {
          assertTrue(resp.statusCode() == 200);
          client.close();
          server.close(res2 -> {
            complete();
          });
        });
    });
  }

  @Suspendable
  protected void testExecNestedSyncMethods() {
    String res = awaitResult(done -> {
      fiberHandler((Void ignore) -> {
        String ab = "A";
        ab += awaitResult(done2 -> {
          done2.handle(Future.succeededFuture("B"));
        });
        done.handle(Future.succeededFuture(ab));
      }).handle(null);
    });
    assertEquals("AB", res);
    complete();
  }

  @Suspendable
  protected void testExecSyncWithAwaitFiber() {
    String res = awaitFiber(done -> {
        String ab = "A";
        ab += awaitFiber(done2 -> {
          done2.handle(Future.succeededFuture("B"));
        });
        done.handle(Future.succeededFuture(ab));
    });
    assertEquals("AB", res);
    complete();
  }

  @Suspendable
  protected void testExecSyncMethodWithParamsAndHandlerNoReturn() {
    Thread th = Thread.currentThread();
    String res = awaitResult(h -> ai.methodWithParamsAndHandlerNoReturn("oranges", 23, h));
    assertEquals("oranges23", res);
    assertSame(Thread.currentThread(), th);
    complete();
  }

  @Suspendable
  protected void testExecSyncMethodWithNoParamsAndHandlerNoReturn() {
    String res = awaitResult(h -> ai.methodWithNoParamsAndHandlerNoReturn(h));
    assertEquals("wibble", res);
    complete();
  }

  @Suspendable
  protected void testExecSyncMethodWithParamsAndHandlerWithReturn() {
    String res = awaitResult(h -> ai.methodWithParamsAndHandlerWithReturn("oranges", 23, h));
    assertEquals("oranges23", res);
    complete();
  }

  @Suspendable
  protected void testExecSyncMethodWithNoParamsAndHandlerWithReturn() {
    String res = awaitResult(h -> ai.methodWithNoParamsAndHandlerWithReturn(h));
    assertEquals("wibble", res);
    complete();
  }

  @Suspendable
  protected void testExecSyncMethodWithNoParamsAndHandlerWithReturnNoTimeout() {
    String res = awaitResult(h -> ai.methodWithNoParamsAndHandlerWithReturnTimeout(h, 1000), 2000);
    assertEquals("wibble", res);
    complete();
  }

  @Suspendable
  protected void testExecSyncMethodWithNoParamsAndHandlerWithReturnTimedout() {
    String res = awaitResult(h -> ai.methodWithNoParamsAndHandlerWithReturnTimeout(h, 1000), 500);
    assertNull(res);
    complete();
  }

  @Suspendable
  protected void testExecSyncMethodWithParamsAndHandlerInterface() {
    ReturnedInterface returned = awaitResult(h -> ai.methodWithParamsAndHandlerInterface("apples", 123, h));
    assertNotNull(returned);
    String res = awaitResult(h -> returned.methodWithParamsAndHandlerNoReturn("bananas", 100, h));
    assertEquals(res, "bananas100");
    complete();
  }

  @Suspendable
  protected void testExecSyncMethodThatFails() {
    try {
      String res = awaitResult(h -> ai.methodThatFails("oranges", h));
      fail("Should throw exception");
    } catch (Exception e) {
      assertTrue(e instanceof VertxException);
      VertxException ve = (VertxException)e;
      assertEquals("oranges", ve.getCause().getMessage());
      complete();
    }

  }

  @Suspendable
  protected void testReceiveEvent() {

    long start = System.currentTimeMillis();
    long tid = awaitEvent(h -> vertx.setTimer(500, h));
    long end = System.currentTimeMillis();
    assertTrue(end - start >= 500);
    assertTrue(tid >= 0);

    complete();
  }

  @Suspendable
  protected void testReceiveEventTimedout() {

    long start = System.currentTimeMillis();
    try {
    	long tid = awaitEvent(h -> vertx.setTimer(500, h), 250);
    } catch(NullPointerException npe) {
    	assertThat(npe, isA(NullPointerException.class));
    } catch(Exception e) {
    	assertTrue(false);
    } finally {
    	complete();
	}
  }

  @Suspendable
  protected void testReceiveEventNoTimeout() {

    long start = System.currentTimeMillis();
    long tid = awaitEvent(h -> vertx.setTimer(500, h), 1000);
    long end = System.currentTimeMillis();
    assertTrue(end - start >= 500);
    assertTrue(tid >= 0);

    complete();
  }

  @Suspendable
  protected void testHandlerAdaptor() throws Exception {

    EventBus eb = vertx.eventBus();

    // Create a couple of consumers on different addresses
    // The adaptor allows handler to be used as a Channel

    HandlerReceiverAdaptor<Message<String>> adaptor1 = streamAdaptor();
    eb.<String>consumer(ADDRESS1).handler(adaptor1);

    HandlerReceiverAdaptor<Message<String>> adaptor2 = streamAdaptor();
    eb.<String>consumer(ADDRESS2).handler(adaptor2);

    // Set up a periodic timer to send messages to these addresses

    long start = System.currentTimeMillis();

    vertx.setPeriodic(10, tid -> {
      eb.send(ADDRESS1, "wibble");
      eb.send(ADDRESS2, "flibble");
    });

    for (int i = 0; i < 10; i++) {

      Message<String> received1 = adaptor1.receive();
      assertEquals("wibble", received1.body());

      Message<String> received2 = adaptor2.receive();
      assertEquals("flibble", received2.body());

    }

    long end = System.currentTimeMillis();
    assertTrue(end - start >= 100);

    // Try a receive with timeout

    Message<String> received1 = adaptor1.receive(1000);
    assertEquals("wibble", received1.body());

    // And timing out

    HandlerReceiverAdaptor<Message<String>> adaptor3 = streamAdaptor();
    eb.<String>consumer(ADDRESS3).handler(adaptor3);

    Message<String> received3 = adaptor3.receive(100);

    assertNull(received3);

    // Try underlying receivePort

    ReceivePort<Message<String>> channel = adaptor1.receivePort();
    assertNotNull(channel);
    received1 = channel.receive();
    assertEquals("wibble", received1.body());


    complete();
  }

  @Override
  @Suspendable
  public void stop() {

    try {

      testContext();

    } catch (AssertionError e) {
      e.printStackTrace();
      fail("tests failed");
    }

  }

}
