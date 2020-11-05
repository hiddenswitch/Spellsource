package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.ApiClient;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.Games;
import com.hiddenswitch.spellsource.net.Spellsource;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.DeckFormat;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.net.impl.Sync.invoke;
import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;
import static org.junit.jupiter.api.Assertions.*;

public class GatewayTest extends SpellsourceTestBase {
	private static Logger LOGGER = LoggerFactory.getLogger(GatewayTest.class);

	@Test
	@Timeout(30000)
	public void testAccountFlow(Vertx vertx, VertxTestContext context) throws InterruptedException {
		var decks = Spellsource.spellsource().getStandardDecks().stream().map(DeckCreateRequest::getName).collect(Collectors.toSet());
		var expectedCount = Runtime.getRuntime().availableProcessors() * 3;
		var executor = vertx.createSharedWorkerExecutor("gatewayTest", expectedCount);
		var checkpoint = context.checkpoint(expectedCount);
		for (var i = 0; i < expectedCount; i++) {
			var j = i;
			executor.executeBlocking(fut -> verify(context, () -> {
				var client = new ApiClient().setBasePath(UnityClient.BASE_PATH);
				var api = new DefaultApi(client);
				var random = RandomStringUtils.randomAlphanumeric(36) + Integer.toString(j);
				try {
					var response1 = api.createAccount(new CreateAccountRequest()
							.name("username" + random)
							.email("email" + random + "@email.com")
							.password("password"));

					api.getApiClient().setApiKey(response1.getLoginToken());
					final var userId = response1.getAccount().getId();
					assertNotNull(userId);
					var response2 = api.getAccount(userId);
					assertTrue(response2.getAccounts().size() > 0);

					for (var account : new Account[]{response1.getAccount(), response2.getAccounts().get(0)}) {
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
				fut.complete();
			}), false, context.succeeding(v -> checkpoint.flag()));
		}
		context.awaitCompletion(30000, TimeUnit.MILLISECONDS);
	}

	@Test
	@Timeout(30000)
	public void testLoginWithInvalidToken(Vertx vertx, VertxTestContext context) throws ApiException {
		var api = new DefaultApi(new ApiClient().setBasePath(UnityClient.BASE_PATH));
		CreateAccountResponse car = null;
		try {
			car = api.createAccount(new CreateAccountRequest()
					.email(RandomStringUtils.randomAlphanumeric(32) + "@test.com")
					.name(RandomStringUtils.randomAlphanumeric(32) + "name")
					.password("password"));
		} catch (ApiException e) {
			fail(e);
			return;
		}
		api.getApiClient().setApiKey(car.getLoginToken());
		GetAccountsResponse accountsResponse = null;
		try {
			accountsResponse = api.getAccount(car.getAccount().getId());
		} catch (ApiException e) {
			fail(e);
			return;
		}
		// Assert we have access to private information here
		assertNotNull(accountsResponse.getAccounts().get(0).getEmail());
		// Change the key to something differently invalid
		api.getApiClient().setApiKey("invaliduserid:invalidtoken");
		try {
			api.getAccount(car.getAccount().getId());
			fail("Successfully received account when we shouldn't have had.");
		} catch (ApiException ex) {
			assertEquals(ex.getCode(), 403, "Assert not authorized");
		}
		// Change the key to something invalid
		api.getApiClient().setApiKey(car.getAccount().getId() + ":invalidtoken");
		try {
			accountsResponse = api.getAccount(car.getAccount().getId());
			fail("Successfully received account when we shouldn't have had.");
		} catch (ApiException ex) {
			assertEquals(ex.getCode(), 403, "Assert not authorized");
		}
		context.completeNow();
	}

	@Test
	@Timeout(12000)
	public void testUnityClient(Vertx vertx, VertxTestContext context) throws InterruptedException, SuspendExecution {
		try (var client = new UnityClient(context)) {
			client.createUserAccount(null);
			client.matchmakeQuickPlay(null);
			client.waitUntilDone();
			assertTrue(client.getTurnsPlayed() > 0);
			assertTrue(client.isGameOver());
			client.matchmakeQuickPlay(null);
			client.waitUntilDone();
			assertTrue(client.getTurnsPlayed() > 0);
			assertTrue(client.isGameOver());
			client.disconnect();
		}

		context.completeNow();
	}


	@Test
	@Timeout(18000)
	public void testConcede(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			try (var client = new UnityClient(context)) {
				invoke0(() -> {
					client.createUserAccount();
					client.getTurnsToPlay().set(1);
					client.matchmakeQuickPlay(null);
				});

				Strand.sleep(3000L);
				// 1 turn was played, concede
				client.concede();
				// You must wait until you receive the end game message to be confident you can requeue
				invoke0(client::waitUntilDone);
				var account = invoke(client.getApi()::getAccount, client.getAccount().getId());
				assertFalse(account.getAccounts().get(0).isInMatch());
				var hasUser = Games.isInGame(new UserId(client.getAccount().getId()));
				assertFalse(hasUser);

				// Can go into another game
				invoke0(() -> {
					try {
						Strand.sleep(200L);
					} catch (SuspendExecution | InterruptedException execution) {
						fail(execution);
						return;
					}

					client.getTurnsToPlay().set(999);
					client.matchmakeQuickPlay(null);
					client.waitUntilDone();
					assertTrue(client.getTurnsPlayed() > 0);
					assertTrue(client.isGameOver());
				});
			}
		}, context, vertx);
	}

	@Test
	@Timeout(25000)
	public void testGameClosedAfterNoActivity(Vertx vertx, VertxTestContext context) {
		System.setProperty("games.defaultNoActivityTimeout", "8000");
		assertEquals(Games.getDefaultNoActivityTimeout(), 8000L);

		// Assert that session was closed
		runOnFiberContext(() -> {
			try (var client = new UnityClient(context)) {
				invoke0(() -> {
					client.setShouldDisconnect(true);
					client.getTurnsToPlay().set(1);
					client.createUserAccount(null);
					client.matchmakeQuickPlay(null);
				});
				var token = client.getToken();
				var userId = client.getAccount().getId();
				// wait 10 seconds
				Strand.sleep(10000L);
				assertFalse(Games.isInGame(new UserId(userId)));

				var done = invoke(() -> {
					var client2 = new UnityClient(context, token);
					try {
						return client2.getApi().getAccount(userId).getAccounts().get(0).isInMatch();
					} catch (ApiException e) {
						fail(e);
						return false;
					}
				});

				assertEquals(false, done);
				// Should not have received end game message
				assertFalse(client.receivedGameOverMessage());
			}
		}, context, vertx);
	}

	@Test
	@Timeout(32000)
	public void testGameDoesntCloseAfterActivity(Vertx vertx, VertxTestContext context) {
		System.setProperty("games.defaultNoActivityTimeout", "12000");
		assertEquals(Games.getDefaultNoActivityTimeout(), 12000L);
		var counter = new AtomicInteger();
		var startTime = new AtomicLong();
		try (var client = new UnityClient(context) {
			@Override
			protected int getActionIndex(ServerToClientMessage message) {
				var endTurn = message.getActions().getAll().stream().filter(ga -> ga.getActionType().equals(ActionType.END_TURN)).findFirst();
				if (endTurn.isPresent()) {
					return endTurn.get().getAction();
				}
				return super.getActionIndex(message);
			}

			@Override
			protected boolean onRequestAction(ServerToClientMessage message) {
				LOGGER.trace("testGameDoesntCloseAfterActivity: Sending request {}", counter.get());
				var andIncrement = counter.getAndIncrement();
				if (andIncrement == 0) {
					startTime.set(System.currentTimeMillis());
					vertx.runOnContext(v -> {
						vertx.setTimer(6000L, ignored -> {
							this.respondRandomAction(message);
						});
					});

				} else if (andIncrement == 1) {
					// Otherwise, slow things down only a little bit
					vertx.runOnContext(v -> {
						vertx.setTimer(6000L, ignored -> {
							this.respondRandomAction(message);
						});
					});
				} else {
					vertx.runOnContext(v -> {
						vertx.setTimer(150L, ignored -> {
							this.respondRandomAction(message);
						});
					});
				}
				return false;
			}
		}) {
			client.createUserAccount(null);
			client.matchmakeQuickPlay(null);
			client.waitUntilDone();
			assertTrue(System.currentTimeMillis() - startTime.get() > 12000L);
			assertTrue(client.getTurnsPlayed() > 0);
			assertTrue(client.isGameOver());
		}
		context.completeNow();
	}


	@Test
	public void testCardsCollection(Vertx vertx, VertxTestContext context) throws ApiException {
		var defaultApi = getApi();

		GetCardsResponse response1 = null;
		try {
			response1 = defaultApi.getCards(null);
		} catch (ApiException e) {
			fail(e);
		}
		// The game is now sending formats and classes to the client
		final var count = CardCatalogue.getRecords().values().stream().filter(
				cd -> DeckFormat.spellsource().isInFormat(cd.getDesc().getSet())
						&& cd.getDesc().getType() != CardType.GROUP).count();
		assertEquals((long) response1.getCards().size(), count);
		var etag = defaultApi.getApiClient().getResponseHeaders().get("ETag").get(0);
		try {
			defaultApi.getCards(etag);
			fail();
		} catch (ApiException ex) {
			assertEquals(ex.getCode(), 304);
		}

		GetCardsResponse response3 = null;
		try {
			response3 = defaultApi.getCards("invalid token");
		} catch (ApiException e) {
			fail(e);
		}
		assertNotNull(response3);
		var newETag = defaultApi.getApiClient().getResponseHeaders().get("ETag").get(0);
		assertEquals(newETag, etag);
		context.completeNow();
	}

	@Test
	public void testMatchmakingCancellation(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			try (var client1 = new UnityClient(context)) {
				invoke0(client1::createUserAccount);
				Future<Void> matchmaking = client1.matchmake(null, "constructed");
				Strand.sleep(4000L);
				invoke0(matchmaking::cancel, true);
				try (var client2 = new UnityClient(context)) {
					invoke0(client2::createUserAccount);
					Future<Void> other = client2.matchmake(null, "constructed");
					Strand.sleep(4000L);
					assertFalse(Games.isInGame(new UserId(client1.getAccount().getId())));
					other.cancel(true);
					Strand.sleep(4000L);
				}
			}
		}, context, vertx);
	}

	/*
	@Test
	public void testHealthcheck(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			try (var client1 = new UnityClient(context)) {
				var res = invoke(client1.getApi()::healthCheck);
				assertEquals(res, "OK");
			}
		}, context, vertx);
	}*/
}
