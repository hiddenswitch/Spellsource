package com.hiddenswitch.spellsource;

import com.github.fromage.quasi.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebsocketRejectedException;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import static io.vertx.ext.sync.Sync.awaitEvent;

public class ConnectionTest extends SpellsourceTestBase {
	@Test
	public void testConnectionNoAuthFails(TestContext testContext) {
		sync(() -> {
			Connection.connected(connection -> {
				testContext.fail("Should not be connected");
			});

			HttpClient client = Vertx.currentContext().owner().createHttpClient();
			CountDownLatch latch = new CountDownLatch(1);
			WebSocket socket = awaitEvent(h -> client.websocket(Port.port(), "localhost", "/realtime", h, excepted -> {
				testContext.assertEquals(WebsocketRejectedException.class, excepted.getClass());
				latch.countDown();
			}));
			latch.await();
		});
	}

	@Test
	public void testConnectionWithAuthSuceeds(TestContext testContext) {
		sync(() -> {
			CountDownLatch latch = new CountDownLatch(1);
			CreateAccountResponse account = createRandomAccount();
			Connection.connected(connection -> {
				latch.countDown();
			});

			HttpClient client = Vertx.currentContext().owner().createHttpClient();
			WebSocket socket = awaitEvent(h -> client.websocket(Port.port(), "localhost", "/realtime?X-Auth-Token=" + account.getLoginToken().getToken(), h, testContext::fail));
			latch.await();
		});
	}

	@Test
	public void testConnectionWithInvalidAuthFails(TestContext testContext) {
		sync(() -> {
			CountDownLatch latch = new CountDownLatch(1);
			CreateAccountResponse account = createRandomAccount();
			Connection.connected(connection -> {
				testContext.fail("Should not connect");
			});

			HttpClient client = Vertx.currentContext().owner().createHttpClient();
			WebSocket socket = awaitEvent(h -> client.websocket(Port.port(), "localhost", "/realtime?X-Auth-Token=invalid:auth", h, excepted -> {
				testContext.assertEquals(WebsocketRejectedException.class, excepted.getClass());
				latch.countDown();
			}));
			latch.await();
		});
	}
}
