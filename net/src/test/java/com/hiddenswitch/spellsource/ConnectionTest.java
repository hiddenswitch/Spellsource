package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebsocketRejectedException;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitEvent;

public class ConnectionTest extends SpellsourceTestBase {
	@Test
	public void testConnectionNoAuthFails(TestContext testContext) {
		sync(() -> {
			WebSocket socket = null;
			try {
				Connection.connected(connection -> {
					testContext.fail("Should not be connected");
				});

				HttpClient client = Vertx.currentContext().owner().createHttpClient();
				CountDownLatch latch = new CountDownLatch(1);
				socket = awaitEvent(h -> client.websocket(Port.port(), "localhost", "/realtime", h, excepted -> {
					testContext.assertEquals(WebsocketRejectedException.class, excepted.getClass());
					latch.countDown();
				}));
				latch.await();
			} finally {
				socket.close();
			}
		});
	}

	@Test
	public void testConnectionWithAuthSuceeds(TestContext testContext) {
		sync(() -> {
			WebSocket socket = null;
			try {
				CountDownLatch latch = new CountDownLatch(1);
				CreateAccountResponse account = createRandomAccount();
				Connection.connected(suspendableHandler(connection -> {
					latch.countDown();
				}));

				HttpClient client = Vertx.currentContext().owner().createHttpClient();
				socket = awaitEvent(h -> client.websocket(Port.port(), "localhost", "/realtime?X-Auth-Token=" + account.getLoginToken().getToken(), h, testContext::fail));
				latch.await();
			} finally {
				socket.close();
			}
		});
	}

	@Test
	public void testConnectionWithInvalidAuthFails(TestContext testContext) {
		sync(() -> {
			WebSocket socket =null;
			try {
				CountDownLatch latch = new CountDownLatch(1);
				CreateAccountResponse account = createRandomAccount();
				Connection.connected(connection -> {
					testContext.fail("Should not connect");
				});

				HttpClient client = Vertx.currentContext().owner().createHttpClient();
				socket = awaitEvent(h -> client.websocket(Port.port(), "localhost", "/realtime?X-Auth-Token=invalid:auth", h, excepted -> {
					testContext.assertEquals(WebsocketRejectedException.class, excepted.getClass());
					latch.countDown();
				}));
				latch.await();
			} finally {
				socket.close();
			}
		});
	}
}
