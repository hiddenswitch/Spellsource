package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebsocketRejectedException;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.vertx.ext.sync.Sync.awaitEvent;

public class ConnectionTest extends SpellsourceTestBase {

	@Test
	public void testConnectionNoAuthFails(TestContext testContext) {
		sync(() -> {
			HttpClient client = Vertx.currentContext().owner().createHttpClient();
			CountDownLatch latch = new CountDownLatch(1);
			client.websocket(Port.port(), "localhost", "/realtime", (ws) -> {
				testContext.fail("Should not be connected");
			}, excepted -> {
				testContext.assertEquals(WebsocketRejectedException.class, excepted.getClass());
				latch.countDown();
			});
			latch.await();
		});
	}

	@Test(timeout = 15000)
	public void testConnectionWithAuthSuceeds(TestContext testContext) {
		sync(() -> {
			WebSocket socket = null;
			try {
				CountDownLatch latch = new CountDownLatch(1);
				CreateAccountResponse account = createRandomAccount();

				HttpClient client = Vertx.currentContext().owner().createHttpClient();
				socket = awaitEvent(h -> client.websocket(Port.port(), "localhost", "/realtime?X-Auth-Token=" + account.getLoginToken().getToken(), h, testContext::fail));
				socket.handler(buf -> {
					Envelope env = Json.decodeValue(buf, Envelope.class);
					latch.countDown();
				});
				latch.await();
			} finally {
				socket.close();
			}
		});
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
			client.websocket(Port.port(), "localhost", "/realtime?X-Auth-Token=invalid:auth", (ws) -> {
				testContext.fail("Should not connect");
			}, excepted -> {
				testContext.assertEquals(WebsocketRejectedException.class, excepted.getClass());
				latch.countDown();
			});
			latch.await();
		});
	}
}
