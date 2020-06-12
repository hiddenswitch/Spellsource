package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.net.Configuration;
import com.hiddenswitch.spellsource.net.Connection;
import com.hiddenswitch.spellsource.net.impl.SpellsourceAuthHandler;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.UpgradeRejectedException;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

import static io.vertx.ext.sync.Sync.awaitResult;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ConnectionTest extends SpellsourceTestBase {

	@Test
	public void testConnectionNoAuthFails(Vertx vertx, VertxTestContext testContext) {
		runOnFiberContext(() -> {
			var client = Vertx.currentContext().owner().createHttpClient();
			var latch = new CountDownLatch(1);
			client.webSocket(Configuration.apiGatewayPort(), "localhost", "/realtime", (ws) -> {
				verify(testContext, () -> {
					if (ws.succeeded()) {
						fail("Should not be connected");
					} else {
						assertTrue(UpgradeRejectedException.class.isAssignableFrom(ws.cause().getClass()));
						latch.countDown();
					}
				});
			});
			latch.await();
		}, testContext, vertx);
	}

	/**
	 * Tests the connection handlers too.
	 * <p>
	 * If you see a {@link TimeoutException}, did you make sure to call {@code fut.handle(Future.succeededFuture());} in
	 * your connection handler?
	 *
	 * @param testContext
	 */
	@Test
	@Timeout(15000)
	public void testConnectionWithAuthSuceeds(Vertx vertx, VertxTestContext testContext) {
		runOnFiberContext(() -> {
			WebSocket socket = null;
			try {
				var latch = new CountDownLatch(1);
				var account = createRandomAccount();

				var client = Vertx.currentContext().owner().createHttpClient();
				socket = awaitResult(h -> client.webSocket(Configuration.apiGatewayPort(), "localhost", "/realtime?X-Auth-Token=" + account.getLoginToken().getToken(), h));
				socket.handler(buf -> {
					Json.decodeValue(buf, Envelope.class);
					latch.countDown();
				});
				latch.await();
			} finally {
				socket.close();
			}
		}, testContext, vertx);
	}

	@Test
	public void testConnectionWithInvalidAuthFails(Vertx vertx, VertxTestContext testContext) {
		runOnFiberContext(() -> {
			var latch = new CountDownLatch(1);
			createRandomAccount();
			Connection.connected((connection, fut) -> {
				verify(testContext, () -> {
					fail("Should not connect");
					fut.handle(Future.succeededFuture());
				});

			});

			var client = Vertx.currentContext().owner().createHttpClient();
			client.webSocket(Configuration.apiGatewayPort(), "localhost", "/realtime?" + SpellsourceAuthHandler.HEADER + "=invalid:auth", testContext.failing(cause -> {
				verify(testContext, () -> {
					assertTrue(UpgradeRejectedException.class.isAssignableFrom(cause.getClass()));
					latch.countDown();
				});

			}));
			latch.await();
		}, testContext, vertx);
	}
}
