package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.*;
import com.hiddenswitch.framework.impl.ClusteredGames;
import com.hiddenswitch.framework.impl.RogueManager;
import com.hiddenswitch.framework.impl.ServerGameContext;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.core.*;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.vertx.await.Async.await;
import static org.junit.jupiter.api.Assertions.*;

public class GraphQLTests extends FrameworkTestBase {

	private static final int GRAPHQL_PORT = 4000;

	protected Future<String> startGraphQL(Vertx vertx) {
		return vertx.deployVerticle(GraphQL.class, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD).setInstances(1));
	}

	protected Future<Void> startAll(Vertx vertx) {
		return startGateway(vertx)
				.compose(v -> startGraphQL(vertx))
				.mapEmpty();
	}

	private WebClient graphqlClient(Vertx vertx) {
		return WebClient.create(vertx, new WebClientOptions().setDefaultPort(GRAPHQL_PORT).setDefaultHost("localhost"));
	}

	private Future<JsonObject> graphql(WebClient webClient, String query) {
		return graphql(webClient, query, null, null);
	}

	private Future<JsonObject> graphql(WebClient webClient, String query, JsonObject variables) {
		return graphql(webClient, query, variables, null);
	}

	private Future<JsonObject> graphql(WebClient webClient, String query, JsonObject variables, String token) {
		var body = new JsonObject().put("query", query);
		if (variables != null) {
			body.put("variables", variables);
		}
		var request = webClient.post("/graphql");
		if (token != null) {
			request.putHeader("Authorization", "Bearer " + token);
		}
		return request.sendJsonObject(body)
				.map(HttpResponse::bodyAsJsonObject);
	}

	private String createAccountMutation() {
		return """
				mutation CreateAccount($input: CreateAccountInput!) {
				  createAccount(input: $input) {
				    accessToken { token }
				    userEntity { id email username }
				  }
				}""";
	}

	private String loginMutation() {
		return """
				mutation Login($input: LoginInput!) {
				  login(input: $input) {
				    accessToken { token }
				    userEntity { id email username }
				  }
				}""";
	}

	private Future<JsonObject> createAccountViaGraphQL(WebClient webClient, String email, String username, String password) {
		return graphql(webClient, createAccountMutation(), new JsonObject()
				.put("input", new JsonObject()
						.put("email", email)
						.put("username", username)
						.put("password", password)))
				.map(res -> res.getJsonObject("data").getJsonObject("createAccount"));
	}

	private String extractToken(JsonObject createAccountData) {
		return createAccountData.getJsonObject("accessToken").getString("token");
	}

	private Future<JsonObject> connectToGame(WebClient webClient, String token) {
		return graphql(webClient, """
				mutation Connect {
				  connectToGame(playerKey: "", playerSecret: "")
				}""", null, token);
	}

	/**
	 * Query available card IDs from the SQL catalogue via the GraphQL cards endpoint.
	 * Returns a list of card IDs that can be used for deck creation.
	 */
	private java.util.List<String> getAvailableCardIds(WebClient webClient, String token, int count) {
		var res = await(graphql(webClient,
				"{ cards { cards { cardId entity { cardType } } } }",
				null, token));
		var cards = res.getJsonObject("data").getJsonObject("cards").getJsonArray("cards");
		var cardIds = new ArrayList<String>();
		for (int i = 0; i < cards.size() && cardIds.size() < count; i++) {
			var card = cards.getJsonObject(i);
			// only use MINION and SPELL cards for deck building
			var cardType = card.getJsonObject("entity").getString("cardType");
			if ("MINION".equals(cardType) || "SPELL".equals(cardType)) {
				cardIds.add(card.getString("cardId"));
			}
		}
		return cardIds;
	}

	// ── Account Tests ────────────────────────────────────────────

	@Test
	public void testCreateAccountViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var email = UUID.randomUUID() + "@test.com";
			var username = UUID.randomUUID().toString();

			var data = await(createAccountViaGraphQL(webClient, email, username, "password"));

			assertNotNull(data.getJsonObject("accessToken").getString("token"), "should have token");
			assertEquals(email, data.getJsonObject("userEntity").getString("email"));
			assertEquals(username, data.getJsonObject("userEntity").getString("username"));
			assertNotNull(data.getJsonObject("userEntity").getString("id"));
		});
	}

	@Test
	public void testCreateGuestAccountViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);

			var res = await(graphql(webClient, createAccountMutation(), new JsonObject()
					.put("input", new JsonObject()
							.put("email", "ignored@test.com")
							.put("username", "ignored")
							.put("password", "ignored")
							.put("guest", true))));

			var data = res.getJsonObject("data").getJsonObject("createAccount");
			assertNotNull(data.getJsonObject("accessToken").getString("token"));
			assertNotNull(data.getJsonObject("userEntity").getString("email"));
		});
	}

	@Test
	public void testLoginViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var email = UUID.randomUUID() + "@test.com";
			var username = UUID.randomUUID().toString();

			await(createAccountViaGraphQL(webClient, email, username, "password"));

			var loginRes = await(graphql(webClient, loginMutation(), new JsonObject()
					.put("input", new JsonObject()
							.put("usernameOrEmail", email)
							.put("password", "password"))));

			var data = loginRes.getJsonObject("data").getJsonObject("login");
			assertNotNull(data.getJsonObject("accessToken").getString("token"));
			assertEquals(email, data.getJsonObject("userEntity").getString("email"));
		});
	}

	@Test
	public void testCurrentUserIdQuery(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var email = UUID.randomUUID() + "@test.com";
			var data = await(createAccountViaGraphQL(webClient, email, UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);
			var userId = data.getJsonObject("userEntity").getString("id");

			var res = await(graphql(webClient, "{ currentUserId }", null, token));

			assertEquals(userId, res.getJsonObject("data").getString("currentUserId"));
		});
	}

	@Test
	public void testAccountQuery(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var email = UUID.randomUUID() + "@test.com";
			var username = UUID.randomUUID().toString();
			var data = await(createAccountViaGraphQL(webClient, email, username, "password"));
			var token = extractToken(data);

			var res = await(graphql(webClient, "{ account { id email username } }", null, token));

			var account = res.getJsonObject("data").getJsonObject("account");
			assertEquals(email, account.getString("email"));
			assertEquals(username, account.getString("username"));
		});
	}

	@Test
	public void testAccountsQueryEmailPrivacy(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);

			var email1 = UUID.randomUUID() + "@test.com";
			var data1 = await(createAccountViaGraphQL(webClient, email1, UUID.randomUUID().toString(), "password"));
			var token1 = extractToken(data1);
			var userId1 = data1.getJsonObject("userEntity").getString("id");

			var email2 = UUID.randomUUID() + "@test.com";
			var data2 = await(createAccountViaGraphQL(webClient, email2, UUID.randomUUID().toString(), "password"));
			var userId2 = data2.getJsonObject("userEntity").getString("id");

			var res = await(graphql(webClient,
					"query Accounts($ids: [String!]!) { accounts(userIds: $ids) { id email } }",
					new JsonObject().put("ids", new JsonArray().add(userId1).add(userId2)),
					token1));

			var accounts = res.getJsonObject("data").getJsonArray("accounts");
			assertEquals(2, accounts.size());
			for (int i = 0; i < accounts.size(); i++) {
				var acct = accounts.getJsonObject(i);
				if (acct.getString("id").equals(userId1)) {
					assertEquals(email1, acct.getString("email"), "should see own email");
				} else {
					assertEquals("", acct.getString("email"), "should not see other user email");
				}
			}
		});
	}

	@Test
	public void testChangePasswordViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var email = UUID.randomUUID() + "@test.com";
			var data = await(createAccountViaGraphQL(webClient, email, UUID.randomUUID().toString(), "oldpassword"));
			var token = extractToken(data);

			var changeRes = await(graphql(webClient,
					"mutation { changePassword(newPassword: \"newpassword\") { userEntity { id } } }",
					null, token));
			assertNull(changeRes.getJsonArray("errors"), "should not have errors");

			var loginRes = await(graphql(webClient, loginMutation(), new JsonObject()
					.put("input", new JsonObject()
							.put("usernameOrEmail", email)
							.put("password", "newpassword"))));
			assertNotNull(loginRes.getJsonObject("data").getJsonObject("login").getJsonObject("accessToken").getString("token"));
		});
	}

	// ── Configuration Test ───────────────────────────────────────

	@Test
	public void testConfigurationQuery(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			var res = await(graphql(webClient,
					"{ configuration { keycloakResetPasswordUrl keycloakAccountManagementUrl graphQlUrl } }",
					null, token));

			var config = res.getJsonObject("data").getJsonObject("configuration");
			assertNotNull(config.getString("keycloakResetPasswordUrl"));
			assertNotNull(config.getString("keycloakAccountManagementUrl"));
		});
	}

	// ── Deck Tests ───────────────────────────────────────────────

	@Test
	public void testCreateDeckViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			// get card IDs from the SQL catalogue
			var cardIds = getAvailableCardIds(webClient, token, 30);
			assertTrue(cardIds.size() >= 30, "need at least 30 cards from SQL catalogue");

			var res = await(graphql(webClient,
					"""
					mutation CreateDeck($input: DecksPutInput!) {
					  createDeck(input: $input) {
					    deckId
					    collection { id name heroClass inventory { cardId } }
					  }
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("name", "Test Deck")
							.put("heroClass", "ANY")
							.put("format", "Spellsource")
							.put("cardIds", new JsonArray(cardIds))),
					token));

			assertNull(res.getJsonArray("errors"), "should not have errors: " + res);
			var deckData = res.getJsonObject("data").getJsonObject("createDeck");
			assertNotNull(deckData.getString("deckId"));
			assertEquals(30, deckData.getJsonObject("collection").getJsonArray("inventory").size());
		});
	}

	@Test
	public void testUpdateDeckViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			// create an empty deck first
			var createRes = await(graphql(webClient,
					"""
					mutation CreateDeck($input: DecksPutInput!) {
					  createDeck(input: $input) { deckId }
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("name", "Test Deck")
							.put("heroClass", "ANY")
							.put("format", "Spellsource")),
					token));
			assertNull(createRes.getJsonArray("errors"), "create should not have errors: " + createRes);
			var deckId = createRes.getJsonObject("data").getJsonObject("createDeck").getString("deckId");

			// get a card ID from the catalogue to push
			var cardIds = getAvailableCardIds(webClient, token, 1);
			assertFalse(cardIds.isEmpty(), "need at least 1 card from SQL catalogue");
			var cardToAdd = cardIds.get(0);

			// update the deck: push a card and set a new name
			var updateRes = await(graphql(webClient,
					"""
					mutation UpdateDeck($input: DecksUpdateInput!) {
					  updateDeck(input: $input) {
					    collection { name inventory { cardId } }
					  }
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("deckId", deckId)
							.put("setName", "Updated Deck")
							.put("pushCardIds", new JsonArray().add(cardToAdd))),
					token));

			assertNull(updateRes.getJsonArray("errors"), "should not have errors: " + updateRes);
			var updatedCollection = updateRes.getJsonObject("data").getJsonObject("updateDeck").getJsonObject("collection");
			assertEquals("Updated Deck", updatedCollection.getString("name"));
			assertEquals(1, updatedCollection.getJsonArray("inventory").size(), "should have added one card");
		});
	}

	@Test
	public void testUpdateDeckRemoveCardsByCardId(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			// get card IDs from SQL catalogue
			var availableCards = getAvailableCardIds(webClient, token, 2);
			assertTrue(availableCards.size() >= 2, "need at least 2 cards from SQL catalogue");
			var card1 = availableCards.get(0);
			var card2 = availableCards.get(1);

			// create empty deck
			var createRes = await(graphql(webClient,
					"""
					mutation CreateDeck($input: DecksPutInput!) {
					  createDeck(input: $input) { deckId }
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("name", "Card ID Deck")
							.put("heroClass", "ANY")
							.put("format", "Spellsource")),
					token));
			assertNull(createRes.getJsonArray("errors"), "create should not have errors: " + createRes);
			var deckId = createRes.getJsonObject("data").getJsonObject("createDeck").getString("deckId");

			// add cards: 3 copies of card1 and 1 copy of card2
			var addRes = await(graphql(webClient,
					"""
					mutation UpdateDeck($input: DecksUpdateInput!) {
					  updateDeck(input: $input) { collection { inventory { cardId } } }
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("deckId", deckId)
							.put("pushCardIds", new JsonArray()
									.add(card1).add(card1).add(card1).add(card2))),
					token));
			assertNull(addRes.getJsonArray("errors"), "add should not have errors: " + addRes);

			// remove 2 copies of card1 and 1 copy of card2
			var removeRes = await(graphql(webClient,
					"""
					mutation UpdateDeck($input: DecksUpdateInput!) {
					  updateDeck(input: $input) { collection { inventory { cardId } } }
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("deckId", deckId)
							.put("pullAllCardIds", new JsonArray()
									.add(card1).add(card1).add(card2))),
					token));

			assertNull(removeRes.getJsonArray("errors"), "should not have errors: " + removeRes);
			var inventory = removeRes.getJsonObject("data").getJsonObject("updateDeck").getJsonObject("collection").getJsonArray("inventory");
			assertEquals(1, inventory.size(), "should have 1 remaining copy of card1");
			assertEquals(card1, inventory.getJsonObject(0).getString("cardId"));
		});
	}

	@Test
	public void testDeleteDeckViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			// create an empty deck (no card IDs needed to test delete)
			var createRes = await(graphql(webClient,
					"""
					mutation CreateDeck($input: DecksPutInput!) {
					  createDeck(input: $input) { deckId }
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("name", "To Delete")
							.put("heroClass", "ANY")
							.put("format", "Spellsource")),
					token));
			assertNull(createRes.getJsonArray("errors"), "create should not have errors: " + createRes);
			var deckId = createRes.getJsonObject("data").getJsonObject("createDeck").getString("deckId");

			// count decks before delete
			var beforeRes = await(graphql(webClient, "{ decks { collection { id } } }", null, token));
			assertNull(beforeRes.getJsonArray("errors"), "decks query should not have errors: " + beforeRes);
			var deckCountBefore = beforeRes.getJsonObject("data").getJsonArray("decks").size();

			// delete the deck
			var deleteRes = await(graphql(webClient,
					"mutation DeleteDeck($id: String!) { deleteDeck(deckId: $id) }",
					new JsonObject().put("id", deckId),
					token));
			assertNull(deleteRes.getJsonArray("errors"), "should not have errors: " + deleteRes);
			assertTrue(deleteRes.getJsonObject("data").getBoolean("deleteDeck"));

			// verify the deck count decreased
			var afterRes = await(graphql(webClient, "{ decks { collection { id } } }", null, token));
			assertNull(afterRes.getJsonArray("errors"), "decks query should not have errors: " + afterRes);
			var deckCountAfter = afterRes.getJsonObject("data").getJsonArray("decks").size();
			assertEquals(deckCountBefore - 1, deckCountAfter, "should have one fewer deck");
		});
	}

	@Test
	public void testGetDecksViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			var res = await(graphql(webClient,
					"{ decks { collection { id name heroClass format deckType } } }",
					null, token));

			assertNull(res.getJsonArray("errors"), "should not have errors: " + res);
			var decks = res.getJsonObject("data").getJsonArray("decks");
			assertEquals(Legacy.getPremadeDecks().size(), decks.size(), "should have premade decks");
		});
	}

	@Test
	public void testGetSingleDeckViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			// create an empty deck
			var createRes = await(graphql(webClient,
					"""
					mutation CreateDeck($input: DecksPutInput!) {
					  createDeck(input: $input) { deckId }
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("name", "Specific Deck")
							.put("heroClass", "ANY")
							.put("format", "Spellsource")),
					token));
			assertNull(createRes.getJsonArray("errors"), "create should not have errors: " + createRes);
			var deckId = createRes.getJsonObject("data").getJsonObject("createDeck").getString("deckId");

			// query it by ID
			var deckRes = await(graphql(webClient,
					"query GetDeck($id: String!) { deck(deckId: $id) { collection { id name heroClass } } }",
					new JsonObject().put("id", deckId),
					token));

			assertNull(deckRes.getJsonArray("errors"), "should not have errors: " + deckRes);
			var collection = deckRes.getJsonObject("data").getJsonObject("deck").getJsonObject("collection");
			assertEquals("Specific Deck", collection.getString("name"));
			assertEquals("ANY", collection.getString("heroClass"));
		});
	}

	@Test
	public void testCreateDeckNoPremades(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);

			var res = await(graphql(webClient, createAccountMutation(), new JsonObject()
					.put("input", new JsonObject()
							.put("email", UUID.randomUUID() + "@test.com")
							.put("username", UUID.randomUUID().toString())
							.put("password", "password")
							.put("decks", false))));
			var token = extractToken(res.getJsonObject("data").getJsonObject("createAccount"));

			var decksRes = await(graphql(webClient, "{ decks { collection { id } } }", null, token));
			assertNull(decksRes.getJsonArray("errors"), "should not have errors: " + decksRes);
			assertEquals(0, decksRes.getJsonObject("data").getJsonArray("decks").size(), "should have no decks");
		});
	}

	// ── Cards Tests ──────────────────────────────────────────────

	@Test
	public void testCardsQuery(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			var res = await(graphql(webClient,
					"{ cards { cards { cardId entity { name cardType rarity } } version cachedOk } }",
					null, token));

			assertNull(res.getJsonArray("errors"), "should not have errors: " + res);
			var cardsResponse = res.getJsonObject("data").getJsonObject("cards");
			assertFalse(cardsResponse.getJsonArray("cards").isEmpty(), "should return cards");
			assertNotNull(cardsResponse.getString("version"));
		});
	}

	@Test
	public void testCardsCaching(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			var res1 = await(graphql(webClient,
					"{ cards { version cachedOk } }",
					null, token));
			assertNull(res1.getJsonArray("errors"), "should not have errors: " + res1);
			var version = res1.getJsonObject("data").getJsonObject("cards").getString("version");
			assertFalse(res1.getJsonObject("data").getJsonObject("cards").getBoolean("cachedOk"));

			var res2 = await(graphql(webClient,
					"query Cards($v: String) { cards(ifNoneMatch: $v) { version cachedOk } }",
					new JsonObject().put("v", version),
					token));
			assertTrue(res2.getJsonObject("data").getJsonObject("cards").getBoolean("cachedOk"));
		});
	}

	// ── Matchmaking Tests ────────────────────────────────────────

	@Test
	public void testMatchmakingQueuesQuery(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			var res = await(graphql(webClient,
					"{ matchmakingQueues { queueId name requires { deck heroClass } } }",
					null, token));

			assertNull(res.getJsonArray("errors"), "should not have errors: " + res);
			var queues = res.getJsonObject("data").getJsonArray("matchmakingQueues");
			assertNotNull(queues);
		});
	}

	@Test
	public void testEnqueueAndCancelMatchmaking(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			// create an empty deck
			var createRes = await(graphql(webClient,
					"""
					mutation CreateDeck($input: DecksPutInput!) {
					  createDeck(input: $input) { deckId }
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("name", "MM Deck")
							.put("heroClass", "ANY")
							.put("format", "Spellsource")),
					token));
			assertNull(createRes.getJsonArray("errors"), "create deck should not have errors: " + createRes);
			var deckId = createRes.getJsonObject("data").getJsonObject("createDeck").getString("deckId");

			// enqueue
			var enqueueRes = await(graphql(webClient,
					"""
					mutation Enqueue($input: MatchmakingEnqueueInput!) {
					  enqueueMatchmaking(input: $input)
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("deckId", deckId)
							.put("queueId", "constructed")),
					token));

			assertNull(enqueueRes.getJsonArray("errors"), "should not have errors: " + enqueueRes);
			assertTrue(enqueueRes.getJsonObject("data").getBoolean("enqueueMatchmaking"));

			// cancel
			var cancelRes = await(graphql(webClient, "mutation { cancelMatchmaking }", null, token));
			assertNull(cancelRes.getJsonArray("errors"), "should not have errors: " + cancelRes);
		});
	}

	@Test
	public void testIsInMatchQuery(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			var res = await(graphql(webClient, "{ isInMatch }", null, token));
			assertNull(res.getJsonArray("errors"), "should not have errors: " + res);
			assertNull(res.getJsonObject("data").getString("isInMatch"), "should not be in match initially");
		});
	}

	// ── Rogue Tests ──────────────────────────────────────────────

	@Test
	public void testStartRogueRunViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			RogueManager.initCardCatalogue();
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			var res = await(graphql(webClient,
					"mutation { startRogueRun(heroClass: \"COPPER\", seed: 0) { id } }",
					null, token));

			assertNull(res.getJsonArray("errors"), "should not have errors: " + res);
			assertNotNull(res.getJsonObject("data").getJsonObject("startRogueRun").getValue("id"));
		});
	}

	@Test
	public void testCurrentRogueClassesQuery(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			var res = await(graphql(webClient, "{ currentRogueClasses }", null, token));

			assertNull(res.getJsonArray("errors"), "should not have errors: " + res);
			var classes = res.getJsonObject("data").getJsonArray("currentRogueClasses");
			assertTrue(classes.contains(HeroClass.COPPER));
			assertTrue(classes.contains(HeroClass.TOAST));
		});
	}

	// ── Error handling / unimplemented stubs ─────────────────────

	@Test
	public void testUnimplementedEndpointsReturnErrors(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var data = await(createAccountViaGraphQL(webClient, UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(data);

			var draftRes = await(graphql(webClient, "{ draft { status } }", null, token));
			assertNotNull(draftRes.getJsonArray("errors"), "draft should return errors");

			var friendsRes = await(graphql(webClient, "{ friends { friendId } }", null, token));
			assertNotNull(friendsRes.getJsonArray("errors"), "friends should return errors");
		});
	}

	@Test
	public void testUnauthenticatedRequestFails(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);

			var res = await(graphql(webClient, "{ account { id } }"));
			var accountData = res.getJsonObject("data");
			var errors = res.getJsonArray("errors");
			assertTrue(accountData == null || accountData.getValue("account") == null || errors != null,
					"should fail or return null without auth");
		});
	}

	// ── Introspection Test ───────────────────────────────────────

	@Test
	public void testIntrospectionWithoutAuth(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);

			var res = await(graphql(webClient,
					"query IntrospectionQuery { __schema { queryType { name } } }"));

			assertNull(res.getJsonArray("errors"), "introspection should work without auth: " + res);
			assertNotNull(res.getJsonObject("data").getJsonObject("__schema"));
		});
	}

	// ── Full Game Lifecycle Test ─────────────────────────────────

	/**
	 * Start all services needed for a full game: Gateway, GraphQL, Matchmaking, ClusteredGames.
	 */
	protected Future<Void> startAllWithMatchmaking(Vertx vertx) {
		return startGateway(vertx)
				.compose(v -> startGraphQL(vertx))
				.compose(v -> vertx.deployVerticle(Matchmaking.class, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD).setInstances(1)))
				.compose(v -> vertx.deployVerticle(ClusteredGames.class, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD).setInstances(1)))
				.mapEmpty();
	}

	@Test
	@Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
	public void testFullGameLifecycleViaGraphQL(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			// 1. Deploy all services
			await(startAllWithMatchmaking(vertx));

			// 2. Create a single-player bot queue
			var queueId = UUID.randomUUID().toString();
			await(Matchmaking.createQueue(new MatchmakingQueues()
					.setId(queueId)
					.setAutomaticallyClose(false)
					.setLobbySize(1)
					.setAwaitingLobbyTimeout(0L)
					.setBotOpponent(true)
					.setEmptyLobbyTimeout(0L)
					.setName("graphql test bot queue")
					.setPrivateLobby(false)
					.setOnce(false)
					.setStartsAutomatically(true)
					.setStillConnectedTimeout(0L)));

			var webClient = graphqlClient(vertx);

			// 3. Create account via GraphQL
			var accountData = await(createAccountViaGraphQL(webClient,
					UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(accountData);

			// 4. Get available cards and create a deck via GraphQL
			var cardIds = getAvailableCardIds(webClient, token, 30);
			assertTrue(cardIds.size() >= 30, "need at least 30 cards from SQL catalogue for deck");

			var createDeckRes = await(graphql(webClient,
					"""
					mutation CreateDeck($input: DecksPutInput!) {
					  createDeck(input: $input) { deckId }
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("name", "Game Test Deck")
							.put("heroClass", "ANY")
							.put("format", "Spellsource")
							.put("cardIds", new JsonArray(cardIds))),
					token));
			assertNull(createDeckRes.getJsonArray("errors"), "create deck errors: " + createDeckRes);
			var deckId = createDeckRes.getJsonObject("data").getJsonObject("createDeck").getString("deckId");

			// 5. Enqueue matchmaking via GraphQL
			var enqueueRes = await(graphql(webClient,
					"""
					mutation Enqueue($input: MatchmakingEnqueueInput!) {
					  enqueueMatchmaking(input: $input)
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("deckId", deckId)
							.put("queueId", queueId)),
					token));
			assertNull(enqueueRes.getJsonArray("errors"), "enqueue errors: " + enqueueRes);
			assertTrue(enqueueRes.getJsonObject("data").getBoolean("enqueueMatchmaking"));

			// 6. Poll isInMatch via GraphQL until game is found
			String gameId = null;
			for (int attempt = 0; attempt < 30; attempt++) {
				await(Environment.sleep(vertx, 1000));
				var matchRes = await(graphql(webClient, "{ isInMatch }", null, token));
				assertNull(matchRes.getJsonArray("errors"), "isInMatch errors: " + matchRes);
				gameId = matchRes.getJsonObject("data").getString("isInMatch");
				if (gameId != null) {
					break;
				}
			}
			assertNotNull(gameId, "should have been matched into a game");

			// 7. Open a WebSocket and subscribe to gameMessages via graphql-transport-ws protocol.
			//    Auth is passed in the connection_init payload.
			var gameOverPromise = Promise.<JsonObject>promise();
			var random = new XORShiftRandom(System.nanoTime());

			var wsClient = vertx.createWebSocketClient();
			var ws = await(wsClient.connect(new WebSocketConnectOptions()
					.setHost("localhost")
					.setPort(GRAPHQL_PORT)
					.setURI("/graphql")
					.addSubProtocol("graphql-transport-ws")));

			// 7a. Send connection_init with auth token
			ws.writeTextMessage(new JsonObject()
					.put("type", "connection_init")
					.put("payload", new JsonObject()
							.put("Authorization", "Bearer " + token))
					.encode());

			// 7b. Wait for connection_ack before subscribing
			var ackPromise = Promise.<Void>promise();
			ws.textMessageHandler(text -> {
				var msg = new JsonObject(text);
				var type = msg.getString("type");

				if ("connection_ack".equals(type)) {
					ackPromise.tryComplete();
					return;
				}

				if ("next".equals(type)) {
					var payload = msg.getJsonObject("payload");
					var gameMessage = payload.getJsonObject("data").getJsonObject("gameMessages");
					var messageType = gameMessage.getString("messageType");

					switch (messageType) {
						case "ON_MULLIGAN":
							var msgId = gameMessage.getString("id");
							graphql(webClient,
									"""
									mutation SendMulligan($indices: [Int!]!, $repliesTo: String!) {
									  sendMulligan(discardedCardIndices: $indices, repliesTo: $repliesTo)
									}""",
									new JsonObject()
											.put("indices", new JsonArray().add(0))
											.put("repliesTo", msgId),
									token)
									.onFailure(gameOverPromise::tryFail);
							break;

						case "ON_REQUEST_ACTION":
							var actionsMsgId = gameMessage.getString("id");
							var actions = gameMessage.getJsonObject("actions").getJsonArray("all");
							var actionIndex = random.nextInt(actions.size());
							graphql(webClient,
									"""
									mutation SendAction($actionIndex: Int!, $repliesTo: String!) {
									  sendGameAction(actionIndex: $actionIndex, repliesTo: $repliesTo)
									}""",
									new JsonObject()
											.put("actionIndex", actionIndex)
											.put("repliesTo", actionsMsgId),
									token)
									.onFailure(gameOverPromise::tryFail);
							break;

						case "ON_GAME_END":
							gameOverPromise.tryComplete(gameMessage);
							break;

						default:
							// ON_UPDATE, ON_GAME_EVENT, TIMER etc. - no action needed
							break;
					}
				}
			});

			ws.exceptionHandler(gameOverPromise::tryFail);
			ws.closeHandler(v -> {
				if (!gameOverPromise.future().isComplete()) {
					gameOverPromise.tryFail("WebSocket closed before game ended");
				}
			});

			await(ackPromise.future());

			// 7c. Send subscribe message for gameMessages
			ws.writeTextMessage(new JsonObject()
					.put("type", "subscribe")
					.put("id", "game-1")
					.put("payload", new JsonObject()
							.put("query", """
									subscription {
									  gameMessages {
									    messageType
									    id
									    localPlayerId
									    isReplayMessage
									    actions { all { action actionType } }
									    startingCards { cardId }
									    gameOver { localPlayerWon winningPlayerId }
									  }
									}"""))
					.encode());

			// 8. Connect to game via GraphQL mutation to send FIRST_MESSAGE
			var connectRes = await(graphql(webClient,
					"""
					mutation Connect($key: String!, $secret: String!) {
					  connectToGame(playerKey: $key, playerSecret: $secret)
					}""",
					new JsonObject()
							.put("key", "")
							.put("secret", ""),
					token));
			assertNull(connectRes.getJsonArray("errors"), "connectToGame errors: " + connectRes);
			assertTrue(connectRes.getJsonObject("data").getBoolean("connectToGame"));

			// 9. Wait for game to finish (messages arrive via WebSocket subscription)
			var gameOverMessage = await(gameOverPromise.future());
			assertNotNull(gameOverMessage, "should have received game over message");
			assertNotNull(gameOverMessage.getJsonObject("gameOver"), "should have gameOver data");

			// 10. Close WebSocket and verify we're no longer in a match
			ws.close();
			await(Environment.sleep(vertx, 1000));
			var afterRes = await(graphql(webClient, "{ isInMatch }", null, token));
			assertNull(afterRes.getJsonArray("errors"), "isInMatch after game errors: " + afterRes);
			assertNull(afterRes.getJsonObject("data").getString("isInMatch"), "should no longer be in match");

			// Cleanup
			await(Matchmaking.deleteQueue(queueId));
		});
	}

	@Test
	public void testConnectToGameWaitsForGameMessageSubscription(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var accountData = await(createAccountViaGraphQL(webClient,
					UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(accountData);
			var userId = accountData.getJsonObject("userEntity").getString("id");

			var firstMessage = Promise.<Spellsource.ClientToServerMessage>promise();
			var gameConsumer = vertx.eventBus()
					.<Spellsource.ClientToServerMessage>consumer(ServerGameContext.getMessagesFromClientAddress(userId));
			gameConsumer.handler(message -> firstMessage.tryComplete(message.body()));
			await(gameConsumer.completion());

			var ws = await(vertx.createWebSocketClient().connect(new WebSocketConnectOptions()
					.setHost("localhost")
					.setPort(GRAPHQL_PORT)
					.setURI("/graphql")
					.addSubProtocol("graphql-transport-ws")));
			var connectionAck = Promise.<Void>promise();
			ws.textMessageHandler(text -> {
				if ("connection_ack".equals(new JsonObject(text).getString("type"))) {
					connectionAck.tryComplete();
				}
			});
			ws.writeTextMessage(new JsonObject()
					.put("type", "connection_init")
					.put("payload", new JsonObject().put("Authorization", "Bearer " + token))
					.encode());
			await(connectionAck.future());

			// Deliberately let the HTTP mutation race ahead of the WebSocket subscribe frame.
			// connectToGame must not acknowledge success until the outbound game-message
			// consumer exists, or a restored game can publish its initial state into a void.
			var connect = connectToGame(webClient, token);
			await(Environment.sleep(vertx, 250));
			assertFalse(connect.isComplete(),
					"connectToGame acknowledged before gameMessages was ready");

			ws.writeTextMessage(new JsonObject()
					.put("type", "subscribe")
					.put("id", "game-connection")
					.put("payload", new JsonObject().put("query", """
							subscription {
							  gameMessages { messageType }
							}"""))
					.encode());

			var response = await(connect);
			assertNull(response.getJsonArray("errors"), "connectToGame errors: " + response);
			assertTrue(response.getJsonObject("data").getBoolean("connectToGame"));
			assertEquals(Spellsource.MessageTypeMessage.MessageType.FIRST_MESSAGE,
					await(firstMessage.future()).getMessageType());

			await(ws.close());
			await(gameConsumer.unregister());
		});
	}

	@Test
	public void testConnectToGameWithoutSubscriptionDoesNotReportSuccess(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var accountData = await(createAccountViaGraphQL(webClient,
					UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(accountData);

			var response = await(connectToGame(webClient, token));

			assertNotNull(response.getJsonArray("errors"),
					"connectToGame must fail rather than report success without gameMessages");
			assertTrue(response.getValue("data") == null
							|| response.getJsonObject("data").getValue("connectToGame") == null,
					"failed connection must not contain a successful result");
		});
	}
}
