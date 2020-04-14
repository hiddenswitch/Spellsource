package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.ApiClient;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.Games;
import com.hiddenswitch.spellsource.net.Spellsource;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.net.impl.GameId;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import net.demilich.metastone.game.decks.DeckFormat;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.net.impl.Sync.invoke;
import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;

public class GatewayTest extends SpellsourceTestBase {
	private static Logger LOGGER = LoggerFactory.getLogger(GatewayTest.class);

	@Test(timeout = 30000L)
	public void testAccountFlow(TestContext context) throws InterruptedException {
		Set<String> decks = Spellsource.spellsource().getStandardDecks().stream().map(DeckCreateRequest::getName).collect(Collectors.toSet());
		final int expectedCount = Runtime.getRuntime().availableProcessors() * 3;
		WorkerExecutor executor = contextRule.vertx().createSharedWorkerExecutor("gatewayTest", expectedCount);
		for (int i = 0; i < expectedCount; i++) {
			final int j = i;
			executor.executeBlocking(fut -> {
				ApiClient client = new ApiClient().setBasePath(UnityClient.BASE_PATH);
				DefaultApi api = new DefaultApi(client);
				String random = RandomStringUtils.randomAlphanumeric(36) + Integer.toString(j);
				try {
					CreateAccountResponse response1 = api.createAccount(new CreateAccountRequest()
							.name("username" + random)
							.email("email" + random + "@email.com")
							.password("password"));

					api.getApiClient().setApiKey(response1.getLoginToken());
					final String userId = response1.getAccount().getId();
					context.assertNotNull(userId);
					GetAccountsResponse response2 = api.getAccount(userId);
					context.assertTrue(response2.getAccounts().size() > 0);

					for (Account account : new Account[]{response1.getAccount(), response2.getAccounts().get(0)}) {
						context.assertNotNull(account.getId());
						context.assertNotNull(account.getEmail());
						context.assertNotNull(account.getName());
						context.assertNotNull(account.getPersonalCollection());
						context.assertNotNull(account.getDecks());
						context.assertEquals(account.getDecks().size(), Spellsource.spellsource().getStandardDecks().size());
						context.assertTrue(account.getDecks().stream().map(InventoryCollection::getName).collect(Collectors.toSet()).containsAll(decks));
						context.assertTrue(account.getPersonalCollection().getInventory().size() > 0);
					}
				} catch (ApiException e) {
					context.fail("API error: " + e.getMessage());
					return;
				}
				fut.complete();
			}, false, context.asyncAssertSuccess());
		}
	}

	@Test(timeout = 10000L)
	public void testLoginWithInvalidToken(TestContext context) throws ApiException {
		Vertx vertx = contextRule.vertx();
		vertx.executeBlocking(fut -> {
			DefaultApi api = new DefaultApi(new ApiClient().setBasePath(UnityClient.BASE_PATH));
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
		}, false, context.asyncAssertSuccess());
	}

	@Test(timeout = 12000L)
	public void testUnityClient(TestContext context) throws InterruptedException, SuspendExecution {
		contextRule.vertx().executeBlocking(fut -> {
			// Play twice
			try (UnityClient client = new UnityClient(context)) {
				client.createUserAccount(null);
				client.matchmakeQuickPlay(null);
				client.waitUntilDone();
				context.assertTrue(client.getTurnsPlayed() > 0);
				context.assertTrue(client.isGameOver());
				client.matchmakeQuickPlay(null);
				client.waitUntilDone();
				context.assertTrue(client.getTurnsPlayed() > 0);
				context.assertTrue(client.isGameOver());
				client.disconnect();
				fut.complete();
			}
		}, context.asyncAssertSuccess());

	}


	@Test(timeout = 18000L)
	public void testConcede(TestContext context) {
		sync(() -> {
			try (UnityClient client = new UnityClient(context)) {
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
				GetAccountsResponse account = invoke(client.getApi()::getAccount, client.getAccount().getId());
				context.assertFalse(account.getAccounts().get(0).isInMatch());
				SuspendableMap<UserId, GameId> games = Games.getUsersInGames();
				boolean hasUser = games.containsKey(new UserId(client.getAccount().getId()));
				context.assertFalse(hasUser);

				// Can go into another game
				invoke0(() -> {
					try {
						Strand.sleep(200L);
					} catch (SuspendExecution | InterruptedException execution) {
						context.fail(execution);
						return;
					}

					client.getTurnsToPlay().set(999);
					client.matchmakeQuickPlay(null);
					client.waitUntilDone();
					context.assertTrue(client.getTurnsPlayed() > 0);
					context.assertTrue(client.isGameOver());
				});
			}
		}, context);
	}

	@Test(timeout = 25000L)
	public void testGameClosedAfterNoActivity(TestContext context) {
		System.setProperty("games.defaultNoActivityTimeout", "8000");
		context.assertEquals(Games.getDefaultNoActivityTimeout(), 8000L);

		// Assert that session was closed
		sync(() -> {
			try (UnityClient client = new UnityClient(context)) {

				invoke0(() -> {
					client.setShouldDisconnect(true);
					client.getTurnsToPlay().set(1);
					client.createUserAccount(null);
					client.matchmakeQuickPlay(null);
				});
				String token = client.getToken();
				String userId = client.getAccount().getId();
				// wait 10 seconds
				Strand.sleep(10000L);
				context.assertNull(Games.getUsersInGames().get(new UserId(userId)));

				Boolean done = invoke(() -> {
					UnityClient client2 = new UnityClient(context, token);
					try {
						return client2.getApi().getAccount(userId).getAccounts().get(0).isInMatch();
					} catch (ApiException e) {
						context.fail(e);
						return false;
					}
				});

				context.assertEquals(false, done);
				// Should not have received end game message
				context.assertFalse(client.receivedGameOverMessage());
			}
		}, context);
	}

	@Test(timeout = 32000L)
	public void testGameDoesntCloseAfterActivity(TestContext context) {
		System.setProperty("games.defaultNoActivityTimeout", "12000");
		context.assertEquals(Games.getDefaultNoActivityTimeout(), 12000L);
		AtomicInteger counter = new AtomicInteger();
		AtomicLong startTime = new AtomicLong();
		Vertx vertx = contextRule.vertx();
		contextRule.vertx().executeBlocking(fut -> {
			try (UnityClient client = new UnityClient(context) {
				@Override
				protected int getActionIndex(ServerToClientMessage message) {
					Optional<SpellAction> endTurn = message.getActions().getAll().stream().filter(ga -> ga.getActionType().equals(ActionType.END_TURN)).findFirst();
					if (endTurn.isPresent()) {
						return endTurn.get().getAction();
					}
					return super.getActionIndex(message);
				}

				@Override
				protected boolean onRequestAction(ServerToClientMessage message) {
					LOGGER.trace("testGameDoesntCloseAfterActivity: Sending request {}", counter.get());
					int andIncrement = counter.getAndIncrement();
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
				context.assertTrue(System.currentTimeMillis() - startTime.get() > 12000L);
				context.assertTrue(client.getTurnsPlayed() > 0);
				context.assertTrue(client.isGameOver());
			}
			fut.complete();
		}, false, context.asyncAssertSuccess());

	}


	@Test
	public void testCardsCollection(TestContext context) throws ApiException {
		contextRule.vertx().executeBlocking(fut -> {
			DefaultApi defaultApi = getApi();

			GetCardsResponse response1 = null;
			try {
				response1 = defaultApi.getCards(null);
			} catch (ApiException e) {
				fut.fail(e);
			}
			// The game is now sending formats and classes to the client
			final long count = CardCatalogue.getRecords().values().stream().filter(
					cd -> DeckFormat.spellsource().isInFormat(cd.getDesc().getSet())
							&& cd.getDesc().getType() != CardType.GROUP
							&& cd.getDesc().getType() != CardType.HERO_POWER
							&& cd.getDesc().getType() != CardType.ENCHANTMENT).count();
			context.assertEquals((long) response1.getCards().size(), count);
			String etag = defaultApi.getApiClient().getResponseHeaders().get("ETag").get(0);
			try {
				GetCardsResponse response2 = defaultApi.getCards(etag);
				context.fail();
			} catch (ApiException ex) {
				context.assertEquals(ex.getCode(), 304);
			}

			GetCardsResponse response3 = null;
			try {
				response3 = defaultApi.getCards("invalid token");
			} catch (ApiException e) {
				fut.fail(e);
			}
			context.assertNotNull(response3);
			String newETag = defaultApi.getApiClient().getResponseHeaders().get("ETag").get(0);
			context.assertEquals(newETag, etag);
			fut.complete();
		}, false, context.asyncAssertSuccess());
	}

	@Test
	public void testMatchmakingCancellation(TestContext context) {
		sync(() -> {
			try (UnityClient client1 = new UnityClient(context)) {
				invoke0(client1::createUserAccount);
				java.util.concurrent.Future<Void> matchmaking = client1.matchmake(null, "constructed");
				Strand.sleep(2000L);
				invoke0(matchmaking::cancel, true);
				try (UnityClient client2 = new UnityClient(context)) {
					invoke0(client2::createUserAccount);
					java.util.concurrent.Future<Void> other = client2.matchmake(null, "constructed");
					Strand.sleep(2000L);
					context.assertFalse(Games.getUsersInGames().containsKey(new UserId(client1.getAccount().getId())));
					other.cancel(true);
					Strand.sleep(2000L);
				}
			}
		}, context);

	}

	@Test
	public void testHealthcheck(TestContext context) {
		sync(() -> {
			try (UnityClient client1 = new UnityClient(context)) {
				String res = invoke(client1.getApi()::healthCheck);
				context.assertEquals(res, "OK");
			}
		}, context);
	}
}
