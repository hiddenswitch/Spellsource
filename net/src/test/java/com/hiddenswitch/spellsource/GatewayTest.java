package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.ApiClient;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.models.CreateGameSessionRequest;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.SendContext;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.web.client.WebClient;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.decks.DeckFormat;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vertx.ext.sync.Sync.awaitResult;
import static org.junit.Assert.*;

/**
 * Created by bberman on 2/18/17.
 */
public class GatewayTest extends SpellsourceTestBase {

	@Test(timeout = 100000L)
	public void testAccountFlow(TestContext context) throws InterruptedException {
		Set<String> decks = Spellsource.spellsource().getStandardDecks().stream().map(DeckCreateRequest::getName).collect(Collectors.toSet());

		final int expectedCount = 10;
		CountDownLatch latch = new CountDownLatch(expectedCount);

		for (int i = 0; i < expectedCount; i++) {
			final int j = i;

			Thread t = new Thread(() -> {
				ApiClient client = new ApiClient().setBasePath(UnityClient.basePath);
				DefaultApi api = new DefaultApi(client);
				String random = RandomStringUtils.randomAlphanumeric(36) + Integer.toString(j);
				try {
					CreateAccountResponse response1 = api.createAccount(new CreateAccountRequest()
							.name("username" + random)
							.email("email" + random + "@email.com")
							.password("password"));

					api.getApiClient().setApiKey(response1.getLoginToken());
					final String userId = response1.getAccount().getId();
					assertNotNull(userId);
					GetAccountsResponse response2 = api.getAccount(userId);
					assertTrue(response2.getAccounts().size() > 0);

					for (Account account : new Account[]{response1.getAccount(), response2.getAccounts().get(0)}) {
						assertNotNull(account.getId());
						assertNotNull(account.getEmail());
						assertNotNull(account.getName());
						assertNotNull(account.getPersonalCollection());
						assertNotNull(account.getDecks());
						assertEquals(account.getDecks().size(), Spellsource.spellsource().getStandardDecks().size());
						assertTrue(account.getDecks().stream().map(InventoryCollection::getName).collect(Collectors.toSet()).containsAll(decks));
						assertTrue(account.getPersonalCollection().getInventory().size() > 0);
					}
				} catch (ApiException e) {
					fail("API error: " + e.getMessage());
					return;
				}
				latch.countDown();
			});

			t.start();
		}

		latch.await(90L, TimeUnit.SECONDS);
		assertEquals(latch.getCount(), 0L);
	}

	@Test
	public void testLoginWithInvalidToken(TestContext context) throws ApiException {
		Async async = context.async();

		vertx.runOnContext(v -> {
			vertx.executeBlocking(fut -> {
				DefaultApi api = new DefaultApi(new ApiClient().setBasePath(UnityClient.basePath));
				CreateAccountResponse car = null;
				try {
					car = api.createAccount(new CreateAccountRequest()
							.email(RandomStringUtils.randomAlphanumeric(32) + "@test.com")
							.name(RandomStringUtils.randomAlphanumeric(32) + "name")
							.password("password"));
				} catch (ApiException e) {
					vertx.exceptionHandler().handle(e);
					fut.fail(e);
					return;
				}
				api.getApiClient().setApiKey(car.getLoginToken());
				GetAccountsResponse accountsResponse = null;
				try {
					accountsResponse = api.getAccount(car.getAccount().getId());
				} catch (ApiException e) {
					vertx.exceptionHandler().handle(e);
					fut.fail(e);
					return;
				}
				// Assert we have access to private information here
				context.assertNotNull(accountsResponse.getAccounts().get(0).getEmail());
				// Change the key to something differently invalid
				api.getApiClient().setApiKey("invaliduserid:invalidtoken");
				try {
					accountsResponse = api.getAccount(car.getAccount().getId());
					context.fail("Successfully received account when we shouldn't have had.");
				} catch (ApiException ex) {
					context.assertEquals(ex.getCode(), 403, "Assert not authorized");
				}
				// Change the key to something invalid
				api.getApiClient().setApiKey(car.getAccount().getId() + ":invalidtoken");
				try {
					accountsResponse = api.getAccount(car.getAccount().getId());
					context.fail("Successfully received account when we shouldn't have had.");
				} catch (ApiException ex) {
					context.assertEquals(ex.getCode(), 403, "Assert not authorized");
				}
				fut.complete();
			}, context.asyncAssertSuccess(v2 -> {
				async.complete();
			}));
		});

	}

	@Test
	public void testUnityClient(TestContext context) throws InterruptedException, SuspendExecution {
		final Async async = context.async();

		for (int i = 0; i < 10; i++) {
			UnityClient client = new UnityClient(context);
			client.createUserAccount(null);
			client.matchmakeAndPlayAgainstAI(null);
			client.waitUntilDone();
			assertTrue(client.isGameOver());
		}
		async.complete();
	}

	@Test(timeout = 165000L)
	public void testSimultaneousGames(TestContext context) throws InterruptedException, SuspendExecution {
		final int processorCount = Runtime.getRuntime().availableProcessors();
		final int count = processorCount * 3;
		CountDownLatch latch = new CountDownLatch(count);
		CompositeFuture.join(Collections.nCopies(2, Arrays.asList(
				Games.create()))
				.stream().flatMap(Collection::stream).map(v -> {
					Future<String> future = Future.future();
					vertx.deployVerticle(v, future);
					return future;
				}).collect(Collectors.toList())).setHandler(then -> {
			Stream.generate(() -> new Thread(() -> {
				try {
					UnityClient client = new UnityClient(context);
					client.createUserAccount(null);
					client.matchmakeAndPlay(null);
					client.waitUntilDone();
					assertTrue(client.isGameOver());
					assertFalse(client.getApi().getAccount(client.getAccount().getId()).getAccounts().get(0).isInMatch());
					// Try two games in a row
					client.matchmakeAndPlay(null);
					client.waitUntilDone();
					assertTrue(client.isGameOver());
					assertFalse(client.getApi().getAccount(client.getAccount().getId()).getAccounts().get(0).isInMatch());
					latch.countDown();
				} catch (Throwable t) {
					context.exceptionHandler().handle(t);
				}
			})).limit(count).forEach(Thread::start);
		});

		// Random games can take quite a long time to finish so be patient...
		latch.await(135L, TimeUnit.SECONDS);
		assertEquals(0L, latch.getCount());
	}

	@Test
	public void testConcede(TestContext context) {
		sync(() -> {
			Logging.setLoggingLevel(Level.DEBUG);
			UnityClient client = new UnityClient(context);
			client.setShouldDisconnect(false);
			Sync.invoke0(() -> {
				client.createUserAccount();
				client.getTurnsToPlay().set(1);
				client.matchmakeAndPlayAgainstAI(null);
			});

			Strand.sleep(200L);
			// 1 turn was played, concede

			Sync.invoke0(client::concede);
			try {
				assertFalse(client.getApi().getAccount(client.getAccount().getId()).getAccounts().get(0).isInMatch());
			} catch (ApiException e) {
				throw new AssertionError(e);
			}
			SuspendableMap<UserId, GameId> games = Games.getGames();
			boolean hasUser = games.containsKey(new UserId(client.getAccount().getId()));
			context.assertFalse(hasUser);

			// Can go into another game
			Sync.invoke0(() -> {
				client.disconnect();
				client.getTurnsToPlay().set(999);
				client.matchmakeAndPlayAgainstAI(null);
				client.waitUntilDone();
				context.assertTrue(client.isGameOver());
			});
		});
	}

	@Test
	@Ignore
	@SuppressWarnings("unchecked")
	public void testScenario(TestContext context) throws InterruptedException, SuspendExecution {
		final Async async = context.async();

		UnityClient client = new UnityClient(context) {
			@Override
			protected void assertValidStateAndChanges(ServerToClientMessage message) {
				super.assertValidStateAndChanges(message);
				assertTrue(message.getGameState().getEntities().stream().filter(e -> e.getCardId() != null && e.getCardId().equals("hero_necromancer")).count() >= 1L);
			}
		};

		client.createUserAccount(null);
		client.matchmakeAndPlayAgainstAI(client.getAccount().getDecks()
				.stream().filter(d -> d.getName().equals("Necromancer (Scenario)")).findFirst().orElseThrow(AssertionError::new).getId());
		client.waitUntilDone();
		assertTrue(client.isGameOver());

		async.complete();
	}

	@Test(timeout = 25000L)
	public void testDisconnectingUnityClient(TestContext context) {
		System.setProperty("games.defaultNoActivityTimeout", "8000");
		assertEquals(Games.getDefaultNoActivityTimeout(), 8000L);

		UnityClient client = new UnityClient(context);
		client.getTurnsToPlay().set(1);
		client.createUserAccount(null);
		final String token = client.getToken();
		final String userId = client.getAccount().getId();
		client.matchmakeAndPlayAgainstAI(null);

		// Assert that session was closed
		sync(() -> {
			// wait 10 seconds
			Strand.sleep(10000L);
			assertNull(Games.getGames().get(new UserId(userId)));

			Boolean done = awaitResult(h -> vertx.executeBlocking((then) -> {
				UnityClient client2 = new UnityClient(context, token);
				try {
					boolean isInMatch = client2.getApi().getAccount(userId).getAccounts().get(0).isInMatch();
					then.complete(isInMatch);
				} catch (ApiException e) {
					then.fail(new AssertionError());
				}
			}, h));

			assertEquals(false, done);
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	@Ignore
	public void testDistinctDecks(TestContext context) throws InterruptedException, SuspendExecution {
		Handler<SendContext> interceptor = interceptGameCreate(request -> {
			assertNotEquals(request.getPregame1().getDeck().getName(), request.getPregame2().getDeck().getName(), "The decks are distinct between the two users.");
		});

		UnityClient client1 = new UnityClient(context);
		Thread clientThread1 = new Thread(() -> {
			client1.createUserAccount("user1");
			final String startDeckId1 = client1.getAccount().getDecks().stream().filter(p -> p.getName().equals(Spellsource.spellsource().getStandardDecks().get(0).getName())).findFirst().get().getId();
			client1.matchmakeAndPlay(startDeckId1);
		});
		UnityClient client2 = new UnityClient(context);
		Thread clientThread2 = new Thread(() -> {
			client2.createUserAccount("user2");
			String startDeckId2 = client2.getAccount().getDecks().stream().filter(p -> p.getName().equals(Spellsource.spellsource().getStandardDecks().get(1).getName())).findFirst().get().getId();
			client2.matchmakeAndPlay(startDeckId2);
		});
		clientThread1.start();
		clientThread2.start();
		float time = 0f;
		while ((!client1.isGameOver() || !client2.isGameOver()) && time < 60f) {
			Strand.sleep(1000);
			time += 1f;
		}
		assertTrue("The client ended the game", client1.isGameOver());
		assertTrue("The client ended the game", client2.isGameOver());
		vertx.eventBus().removeInterceptor(interceptor);
	}

	private Handler<SendContext> interceptGameCreate(Consumer<CreateGameSessionRequest> assertInHere) {
		final Handler<SendContext> interceptor = h -> {
			if (h.message().address().equals(Rpc.getAddress(Games.class, games -> games.createGameSession(null)))) {
				Message<Buffer> message = h.message();
				VertxBufferInputStream inputStream = new VertxBufferInputStream(message.body());

				CreateGameSessionRequest request = null;
				try {
					request = Serialization.deserialize(inputStream);
				} catch (IOException | ClassNotFoundException e) {
					fail(e.getMessage());
				}

				if (request != null) {
					assertInHere.accept(request);
				} else {
					fail("Request was null.");
				}

			}
			h.next();
		};
		vertx.eventBus().addInterceptor(interceptor);
		return interceptor;
	}

	@Test
	public void testCardsCollection(TestContext context) throws ApiException {
		DefaultApi defaultApi = getApi();

		GetCardsResponse response1 = defaultApi.getCards(null);
		final long count = CardCatalogue.getRecords().values().stream().filter(
				cd -> DeckFormat.CUSTOM.isInFormat(cd.getDesc().getSet()) && cd.getDesc().type != CardType.GROUP && cd.getDesc().type != CardType.HERO_POWER && cd.getDesc().type != CardType.ENCHANTMENT).count();
		context.assertEquals((long) response1.getCards().size(), count);
		try {
			GetCardsResponse response2 = defaultApi.getCards(response1.getVersion());
			context.fail();
		} catch (ApiException ex) {
			context.assertEquals(ex.getCode(), 304);
		}

		GetCardsResponse response3 = defaultApi.getCards("invalid token");
		context.assertNotNull(response3);
		context.assertEquals(response3.getVersion(), response1.getVersion());
	}

	@Test
	public void testDisconnectingClientMatchmaking(TestContext context) throws ApiException, InterruptedException {
		Logging.setLoggingLevel(Level.DEBUG);
		DefaultApi user1 = getApi();
		DefaultApi user2 = getApi();

		CreateAccountResponse res1 = user1.createAccount(new CreateAccountRequest()
				.name("username" + RandomStringUtils.randomAlphanumeric(10))
				.email("email-" + RandomStringUtils.randomAlphanumeric(10) + "@test.com")
				.password("password"));
		CreateAccountResponse res2 = user2.createAccount(new CreateAccountRequest()
				.name("username" + RandomStringUtils.randomAlphanumeric(10))
				.email("email-" + RandomStringUtils.randomAlphanumeric(10) + "@test.com")
				.password("password"));
		user1.getApiClient().setApiKey(res1.getLoginToken());
		user2.getApiClient().setApiKey(res2.getLoginToken());

		HttpClient client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080).setDefaultHost("localhost"));
		WebClient webClient = WebClient.wrap(client);

		AtomicBoolean successfulTimeout = new AtomicBoolean();
		webClient
				.put("/matchmaking/constructed")
				.putHeader("X-Auth-Token", res1.getLoginToken())
				.timeout(100L)
				.sendJson(new MatchmakingQueuePutRequest()
						.casual(false)
						.deckId(res1.getAccount().getDecks().get(0).getId()), res -> {
					successfulTimeout.set(true);
				});

		HttpClient finalClient = client;
		vertx.setTimer(101L, v1 -> {
			finalClient.close();
		});

		vertx.exceptionHandler(context.exceptionHandler());

		Thread.sleep(1000L);

		client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8080).setDefaultHost("localhost"));
		webClient = WebClient.wrap(client);
		Thread.sleep(1000L);
		context.assertTrue(successfulTimeout.get());
		webClient
				.put("/matchmaking/constructed")
				.putHeader("X-Auth-Token", res2.getLoginToken())
				.timeout(100L)
				.sendJson(new MatchmakingQueuePutRequest()
						.casual(false)
						.deckId(res2.getAccount().getDecks().get(0).getId()), res -> {
				});

		Thread.sleep(1000L);
		Async async = context.async();
		sync(() -> {
			context.assertFalse(Games.getGames().containsKey(new UserId(res1.getAccount().getId())));
			async.complete();
		});
	}
}
