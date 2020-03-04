package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.google.common.base.Throwables;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.net.Configuration;
import com.hiddenswitch.spellsource.net.Connection;
import com.hiddenswitch.spellsource.net.impl.SpellsourceAuthHandler;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.models.CreateAccountResponse;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebsocketRejectedException;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;
import net.bytebuddy.implementation.bytecode.Throw;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static io.vertx.ext.sync.Sync.awaitEvent;
import static io.vertx.ext.sync.Sync.awaitResult;

public class ConnectionTest extends SpellsourceTestBase {

	@Test
	public void testConnectionNoAuthFails(TestContext testContext) {
		sync(() -> {
			HttpClient client = Vertx.currentContext().owner().createHttpClient();
			CountDownLatch latch = new CountDownLatch(1);
			client.webSocket(Configuration.apiGatewayPort(), "localhost", "/realtime", (ws) -> {
				if (ws.succeeded()) {
					testContext.fail("Should not be connected");
				} else {
					testContext.assertEquals(WebsocketRejectedException.class, ws.cause().getClass());
					latch.countDown();
				}
			});
			latch.await();
		}, testContext);
	}

	/**
	 * Tests the connection handlers too.
	 * <p>
	 * If you see a {@link TimeoutException}, did you make sure to call {@code fut.handle(Future.succeededFuture());} in
	 * your connection handler?
	 *
	 * @param testContext
	 */
	@Test(timeout = 15000)
	public void testConnectionWithAuthSuceeds(TestContext testContext) {
		sync(() -> {
			WebSocket socket = null;
			try {
				CountDownLatch latch = new CountDownLatch(1);
				CreateAccountResponse account = createRandomAccount();

				HttpClient client = Vertx.currentContext().owner().createHttpClient();
				socket = awaitResult(h -> client.webSocket(Configuration.apiGatewayPort(), "localhost", "/realtime?X-Auth-Token=" + account.getLoginToken().getToken(), h));
				socket.handler(buf -> {
					Envelope env = Json.decodeValue(buf, Envelope.class);
					latch.countDown();
				});
				latch.await();
			} finally {
				socket.close();
			}
		}, testContext);
	}

	@Test
	public void testConnectionWithInvalidAuthFails(TestContext testContext) {
		sync(() -> {
			CountDownLatch latch = new CountDownLatch(1);
			CreateAccountResponse account = createRandomAccount();
			Connection.connected((connection, fut) -> {
				testContext.fail("Should not connect");
				fut.handle(Future.succeededFuture());
			});

			HttpClient client = Vertx.currentContext().owner().createHttpClient();
			client.webSocket(Configuration.apiGatewayPort(), "localhost", "/realtime?" + SpellsourceAuthHandler.HEADER + "=invalid:auth", testContext.asyncAssertFailure(cause -> {
				testContext.assertEquals(WebsocketRejectedException.class, cause.getClass());
				latch.countDown();
			}));
			latch.await();
		}, testContext);
	}
}
