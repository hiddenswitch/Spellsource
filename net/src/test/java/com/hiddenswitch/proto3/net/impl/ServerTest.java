package com.hiddenswitch.proto3.net.impl;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.proto3.net.Games;
import com.hiddenswitch.proto3.net.Logic;
import com.hiddenswitch.proto3.net.client.ApiClient;
import com.hiddenswitch.proto3.net.client.ApiException;
import com.hiddenswitch.proto3.net.client.api.DefaultApi;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.models.CreateGameSessionRequest;
import com.hiddenswitch.proto3.net.models.CurrentMatchRequest;
import com.hiddenswitch.proto3.net.util.Serialization;
import com.hiddenswitch.proto3.net.util.UnityClient;
import com.hiddenswitch.proto3.net.util.VertxBufferInputStream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.SendContext;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by bberman on 2/18/17.
 */
public class ServerTest extends ServiceTest<ServerImpl> {
	private String deploymentId;

	@Test(timeout = 120000L)
	public void testShutdownAndRestartServer(TestContext context) throws InterruptedException, SuspendExecution {
		setLoggingLevel(Level.ERROR);
		wrap(context);
		final Async async = context.async();

		getContext().assertNotNull(service.server);
		getContext().assertNotNull(service.logic.deploymentID());
		getContext().assertNotNull(service.games.deploymentID());

		vertx.executeBlocking(done -> {
			UnityClient client = new UnityClient(getContext());
			client.createUserAccount("testaccount23");
			client.matchmakeAndPlayAgainstAI(null);
			client.waitUntilDone();
			getContext().assertTrue(client.isGameOver());
			done.complete(true);
		}, true, then -> {
			vertx.undeploy(deploymentId, then2 -> {
				service = null;
				deployServices(vertx, then3 -> {
					service = then3.result();
					getContext().assertNotNull(service.logic.deploymentID());
					getContext().assertNotNull(service.accounts.deploymentID());
					vertx.executeBlocking(done2 -> {
						UnityClient client2 = new UnityClient(getContext());
						client2.loginWithUserAccount("testaccount23");
						client2.matchmakeAndPlayAgainstAI(null);
						client2.waitUntilDone();
						getContext().assertTrue(client2.isGameOver());
						done2.complete(true);
					}, true, then4 -> {
						getContext().assertTrue(then4.succeeded());
						async.complete();
						unwrap();
					});
				});
			});
		});

	}

	@Test(timeout = 60000L)
	public void testAccountFlow(TestContext context) throws InterruptedException {
		wrap(context);
		Set<String> decks = new HashSet<>(Arrays.asList(Logic.STARTING_DECKS));
		final Async async = context.async();
		final AtomicInteger count = new AtomicInteger(20);
		// Interleave these calls
		for (int i = 0; i < 100; i++) {
			final int j = i;
			Thread t = new Thread(() -> {
				try {
					Thread.sleep(RandomUtils.nextInt(10, 100));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ApiClient client = new ApiClient().setBasePath("http://localhost:8080/v1");
				client.getHttpClient().setConnectTimeout(2, TimeUnit.MINUTES);
				client.getHttpClient().setWriteTimeout(2, TimeUnit.MINUTES);
				client.getHttpClient().setReadTimeout(2, TimeUnit.MINUTES);
				DefaultApi api = new DefaultApi(client);
				String random = RandomStringUtils.randomAlphanumeric(36) + Integer.toString(j);
				try {
					CreateAccountResponse response1 = api.createAccount(new CreateAccountRequest()
							.name("username" + random)
							.email("email" + random + "@email.com")
							.password("password"));

					api.getApiClient().setApiKey(response1.getLoginToken());
					final String userId = response1.getAccount().getId();
					getContext().assertNotNull(userId);
					GetAccountsResponse response2 = api.getAccount(userId);
					getContext().assertTrue(response2.getAccounts().size() > 0);

					for (Account account : new Account[]{response1.getAccount(), response2.getAccounts().get(0)}) {
						getContext().assertNotNull(account.getId());
						getContext().assertNotNull(account.getEmail());
						getContext().assertNotNull(account.getName());
						getContext().assertNotNull(account.getPersonalCollection());
						getContext().assertNotNull(account.getDecks());
						getContext().assertTrue(account.getDecks().size() == Logic.STARTING_DECKS.length);
						getContext().assertTrue(account.getDecks().stream().map(InventoryCollection::getName).collect(Collectors.toSet()).containsAll(decks));
						getContext().assertTrue(account.getPersonalCollection().getInventory().size() > 0);
					}
				} catch (ApiException e) {
					getContext().fail("API error: " + e.getMessage());
					return;
				}
				count.decrementAndGet();
			});
			t.start();
		}

		float timeout = 50f;
		while (count.get() > 0 && timeout > 0) {
			Thread.sleep(100);
			timeout -= 0.1f;
		}
		async.complete();
		unwrap();
	}

	@Test(timeout = 60000L)
	public void testUnityClient(TestContext context) throws InterruptedException, SuspendExecution {
		setLoggingLevel(Level.ERROR);
		wrap(context);
		final String property = System.getProperty("minionate.unityTests");
		final int tests = Integer.parseInt(property != null ? property : "1");
		final Async async = context.async();

		for (int i = 0; i < tests; i++) {
			UnityClient client = new UnityClient(getContext());
			client.createUserAccount(null);
			client.matchmakeAndPlayAgainstAI(null);
			float time = 0f;
			while (!(time > 120f || client.isGameOver())) {
				Strand.sleep(1000);
				time += 1f;
			}
			getContext().assertTrue(client.isGameOver());
		}
		async.complete();
		unwrap();
	}

	@Test(timeout = 60000L)
	public void testDisconnectingUnityClient(TestContext context) throws InterruptedException, SuspendExecution {
		wrap(context);
		setLoggingLevel(Level.ERROR);
		getContext().assertEquals(Games.getDefaultNoActivityTimeout(), 8000L);
		final Async async = context.async();

		UnityClient client = new UnityClient(getContext(), 5);
		Thread clientThread = new Thread(() -> {
			client.createUserAccount(null);
			client.matchmakeAndPlayAgainstAI(null);
		});
		clientThread.start();

		// wait 16 seconds
		Strand.sleep(16000);
		// Assert that session was closed
		getContext().assertEquals(service.matchmaking.getCurrentMatch(new CurrentMatchRequest(client.getAccount().getId())).getGameId(), null);
		async.complete();
		unwrap();
	}

	@Test(timeout = 60000L)
	@SuppressWarnings("unchecked")
	public void testDistinctDecks(TestContext context) throws InterruptedException, SuspendExecution {
		setLoggingLevel(Level.ERROR);
		wrap(context);


		final Handler<SendContext> interceptor = h -> {
			if (h.message().address().equals("com.hiddenswitch.proto3.net.Games::createGameSession")) {
				Message<Buffer> message = h.message();
				VertxBufferInputStream inputStream = new VertxBufferInputStream(message.body());

				CreateGameSessionRequest request = null;
				try {
					request = Serialization.deserialize(inputStream);
				} catch (IOException | ClassNotFoundException e) {
					getContext().fail(e.getMessage());
				}

				if (request != null) {
					getContext().assertNotEquals(request.getPregame1().getDeck().getName(), request.getPregame2().getDeck().getName(), "The decks are distinct between the two users.");
				} else {
					getContext().fail("Request was null.");
				}

			}
			h.next();
		};
		vertx.eventBus().addInterceptor(interceptor);


		UnityClient client1 = new UnityClient(getContext());
		Thread clientThread1 = new Thread(() -> {
			client1.createUserAccount("user1");
			final String startDeckId1 = client1.getAccount().getDecks().stream().filter(p -> p.getName().equals(Logic.STARTING_DECKS[0])).findFirst().get().getId();
			try {
				client1.matchmakeAndPlay(startDeckId1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		UnityClient client2 = new UnityClient(getContext());
		Thread clientThread2 = new Thread(() -> {
			client2.createUserAccount("user2");
			String startDeckId2 = client2.getAccount().getDecks().stream().filter(p -> p.getName().equals(Logic.STARTING_DECKS[1])).findFirst().get().getId();
			try {
				client2.matchmakeAndPlay(startDeckId2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		clientThread1.start();
		clientThread2.start();
		float time = 0f;
		while ((!client1.isGameOver() || !client2.isGameOver()) && time < 60f) {
			Strand.sleep(1000);
			time += 1f;
		}
		getContext().assertTrue(client1.isGameOver(), "The client ended the game");
		getContext().assertTrue(client2.isGameOver(), "The client ended the game");
		vertx.eventBus().removeInterceptor(interceptor);
		unwrap();
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<ServerImpl>> done) {
		System.setProperty("games.defaultNoActivityTimeout", "8000");
		ServerImpl instance = new ServerImpl();
		instance.bots.setBotBehaviour(PlayRandomBehaviour.class);
		vertx.deployVerticle(instance, then -> {
			deploymentId = then.result();
			done.handle(Future.succeededFuture(instance));
		});
	}
}
