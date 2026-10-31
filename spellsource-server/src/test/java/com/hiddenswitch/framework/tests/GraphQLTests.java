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

	private static final int GRAPHQL_PORT = GraphQL.defaultPort();

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
	public void testPublishedCatalogueIncludesGitCards(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var result = await(graphql(graphqlClient(vertx), "{ catalogueCards { id createdBy cardScript } }"));
			assertNull(result.getJsonArray("errors"), () -> result.encode());
			var cards = result.getJsonObject("data").getJsonArray("catalogueCards");
			assertFalse(cards.isEmpty());
			boolean hasClass = false;
			boolean hasAvenger = false;
			for (var value : cards) {
				var card = (JsonObject) value;
				var script = card.getJsonObject("cardScript");
				assertEquals(card.getString("id"), script.getString("id"));
				assertNotEquals("TEST", script.getString("set"));
				assertNotNull(script.getString("name"));
				hasClass |= "CLASS".equals(script.getString("type"));
				hasAvenger |= "minion_fassnu_avenger".equals(card.getString("id"));
			}
			assertTrue(hasClass, "class definitions must survive removal of Git cards from SQL");
			assertTrue(hasAvenger, "Git card definitions must be available without a SQL card row");
		});
	}

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
			var heartbeatPromise = Promise.<String>promise();
			var heartbeatNonce = java.util.UUID.randomUUID().toString();
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
						case "PINGPONG":
							heartbeatPromise.tryComplete(gameMessage.getString("id"));
							break;
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

			// A gateway acknowledgement alone is insufficient: the engine must echo the nonce
			// through the same subscribed stream used for authoritative game state.
			var heartbeat = await(graphql(webClient,
					"mutation($nonce: String!) { gameHeartbeat(nonce: $nonce) }",
					new JsonObject().put("nonce", heartbeatNonce), token));
			assertNull(heartbeat.getJsonArray("errors"));
			assertTrue(heartbeat.getJsonObject("data").getBoolean("gameHeartbeat"));
			assertEquals(heartbeatNonce, await(heartbeatPromise.future().timeout(10, java.util.concurrent.TimeUnit.SECONDS)));

			// 9. Wait for game to finish (messages arrive via WebSocket subscription)
			var gameOverMessage = await(gameOverPromise.future());
			assertNotNull(gameOverMessage, "should have received game over message");
			assertNotNull(gameOverMessage.getJsonObject("gameOver"), "should have gameOver data");

			// 10. Close WebSocket and verify we're no longer in a match
			await(ws.close());
			await(wsClient.close());
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
		assertConnectionReadiness(vertx, testContext, null);
	}

	@Test
	public void testReconnectWaitsForItsOwnSubscription(Vertx vertx, VertxTestContext testContext) {
		assertConnectionReadiness(vertx, testContext, UUID.randomUUID().toString());
	}

	private void assertConnectionReadiness(Vertx vertx, VertxTestContext testContext, String connectionId) {
		testVirtual(vertx, testContext, () -> {
			await(startAll(vertx));
			var webClient = graphqlClient(vertx);
			var accountData = await(createAccountViaGraphQL(webClient,
					UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(accountData);
			var userId = accountData.getJsonObject("userEntity").getString("id");

			var firstMessage = Promise.<Spellsource.ClientToServerMessage>promise();
			var gameConsumer = vertx.eventBus()
					.<Spellsource.ClientToServerMessage>consumer(Games.getMessagesFromClientAddress(userId));
			gameConsumer.handler(message -> firstMessage.tryComplete(message.body()));
			await(gameConsumer.completion());

			var wsClient = vertx.createWebSocketClient();
			var ws = await(wsClient.connect(new WebSocketConnectOptions()
					.setHost("localhost")
					.setPort(GRAPHQL_PORT)
					.setURI("/graphql")
					.addSubProtocol("graphql-transport-ws")));
			// Vert.x 5 cleans up an unreachable client even while its socket is in use.
			// Keep the client alive through explicit close and exercise collection here.
			System.gc();
			await(Environment.sleep(vertx, 250));
			var connectionAck = Promise.<Void>promise();
			var subscriptionFailure = new java.util.concurrent.atomic.AtomicReference<String>();
			ws.exceptionHandler(cause -> {
				connectionAck.tryFail(cause);
				subscriptionFailure.set(cause.toString());
			});
			ws.closeHandler(ignored -> {
				var reason = "WebSocket closed: " + ws.closeStatusCode() + " " + ws.closeReason();
				connectionAck.tryFail(reason);
				subscriptionFailure.set(reason);
			});
			ws.textMessageHandler(text -> {
				var message = new JsonObject(text);
				if ("error".equals(message.getString("type"))
						|| message.getJsonObject("payload", new JsonObject()).containsKey("errors")) {
					subscriptionFailure.set(text);
				}
				if ("connection_ack".equals(message.getString("type"))) {
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
			if (connectionId != null) {
				ws.writeTextMessage(new JsonObject().put("type", "subscribe").put("id", "stale-subscription")
						.put("payload", new JsonObject().put("query", "subscription { gameMessages(connectionId: \"stale\") { messageType } }")).encode());
				await(Environment.sleep(vertx, 100));
			}
			var connect = connectionId == null ? connectToGame(webClient, token)
					: graphql(webClient, "mutation($id: String) { connectToGame(connectionId: $id) }",
						new JsonObject().put("id", connectionId), token);
			await(Environment.sleep(vertx, 250));
			assertFalse(connect.isComplete(),
					"connectToGame acknowledged before gameMessages was ready");

			ws.writeTextMessage(new JsonObject()
					.put("type", "subscribe")
					.put("id", "game-connection")
					.put("payload", new JsonObject().put("query", """
							subscription($id: String) {
							  gameMessages(connectionId: $id) { messageType }
							}""").put("variables", new JsonObject().put("id", connectionId)))
					.encode());

			var response = await(connect);
			assertNull(response.getJsonArray("errors"), "connectToGame errors: " + response + "; subscription failure: " + subscriptionFailure.get());
			assertTrue(response.getJsonObject("data").getBoolean("connectToGame"));
			assertEquals(Spellsource.MessageTypeMessage.MessageType.FIRST_MESSAGE,
					await(firstMessage.future()).getMessageType());

			await(ws.close());
			await(wsClient.close());
			await(gameConsumer.unregister());
		});
	}

	private Future<String> createBotQueue() {
		var queueId = UUID.randomUUID().toString();
		return Matchmaking.createQueue(new MatchmakingQueues()
						.setId(queueId)
						.setAutomaticallyClose(false)
						.setLobbySize(1)
						.setAwaitingLobbyTimeout(0L)
						.setBotOpponent(true)
						.setEmptyLobbyTimeout(0L)
						.setName("graphql matchmaking readiness queue")
						.setPrivateLobby(false)
						.setOnce(false)
						.setStartsAutomatically(true)
						.setStillConnectedTimeout(0L))
				.map(queueId);
	}

	private String createDeck(WebClient webClient, String token) {
		var cardIds = getAvailableCardIds(webClient, token, 30);
		assertTrue(cardIds.size() >= 30, "need at least 30 cards from SQL catalogue for deck");
		var createDeckRes = await(graphql(webClient,
				"""
				mutation CreateDeck($input: DecksPutInput!) {
				  createDeck(input: $input) { deckId }
				}""",
				new JsonObject().put("input", new JsonObject()
						.put("name", "Matchmaking Readiness Deck")
						.put("heroClass", "ANY")
						.put("format", "Spellsource")
						.put("cardIds", new JsonArray(cardIds))),
				token));
		assertNull(createDeckRes.getJsonArray("errors"), "create deck errors: " + createDeckRes);
		return createDeckRes.getJsonObject("data").getJsonObject("createDeck").getString("deckId");
	}

	private Future<JsonObject> enqueueMatchmaking(WebClient webClient, String token, String deckId, String queueId) {
		return graphql(webClient,
				"""
				mutation Enqueue($input: MatchmakingEnqueueInput!) {
				  enqueueMatchmaking(input: $input)
				}""",
				new JsonObject().put("input", new JsonObject()
						.put("deckId", deckId)
						.put("queueId", queueId)),
				token);
	}

	@Test
	@Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
	public void testMatchFoundDeliveredWhenSubscriptionOpensAfterGameCreated(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAllWithMatchmaking(vertx));
			var queueId = await(createBotQueue());
			var webClient = graphqlClient(vertx);
			var accountData = await(createAccountViaGraphQL(webClient,
					UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(accountData);
			var deckId = createDeck(webClient, token);

			// The client opens matchFound and enqueues at the same time, and the bot queue
			// matches within one scan, so the game routinely exists before the subscription's
			// consumer does. Reproduce the worst case: the game is already created when the
			// subscription opens. The matchmaker must keep offering the game until then.
			var enqueueRes = await(enqueueMatchmaking(webClient, token, deckId, queueId));
			assertNull(enqueueRes.getJsonArray("errors"), "enqueueMatchmaking errors: " + enqueueRes);
			assertTrue(enqueueRes.getJsonObject("data").getBoolean("enqueueMatchmaking"));

			String createdGameId = null;
			for (var attempt = 0; attempt < 20 && createdGameId == null; attempt++) {
				await(Environment.sleep(vertx, 250));
				var matchRes = await(graphql(webClient, "{ isInMatch }", null, token));
				createdGameId = matchRes.getJsonObject("data").getString("isInMatch");
			}
			assertNotNull(createdGameId, "the bot queue should have created a game");

			var wsClient = vertx.createWebSocketClient();
			var ws = await(wsClient.connect(new WebSocketConnectOptions()
					.setHost("localhost")
					.setPort(GRAPHQL_PORT)
					.setURI("/graphql")
					.addSubProtocol("graphql-transport-ws")));
			// Vert.x 5 cleans up an unreachable client even while its socket is in use.
			// Keep the client alive through explicit close and exercise collection here.
			System.gc();
			await(Environment.sleep(vertx, 250));
			var connectionAck = Promise.<Void>promise();
			var matchFound = Promise.<String>promise();
			ws.exceptionHandler(cause -> {
				connectionAck.tryFail(cause);
				matchFound.tryFail(cause);
			});
			ws.closeHandler(ignored -> {
				connectionAck.tryFail("WebSocket closed before connection acknowledgement: " + ws.closeStatusCode() + " " + ws.closeReason());
				matchFound.tryFail("WebSocket closed before matchFound delivery: " + ws.closeStatusCode() + " " + ws.closeReason());
			});
			ws.textMessageHandler(text -> {
				var message = new JsonObject(text);
				switch (message.getString("type")) {
					case "connection_ack" -> connectionAck.tryComplete();
					case "next" -> matchFound.tryComplete(message.getJsonObject("payload")
							.getJsonObject("data").getJsonObject("matchFound").getString("gameId"));
					case "error" -> matchFound.tryFail(text);
					default -> {
					}
				}
			});
			ws.writeTextMessage(new JsonObject()
					.put("type", "connection_init")
					.put("payload", new JsonObject().put("Authorization", "Bearer " + token))
					.encode());
			await(connectionAck.future());
			ws.writeTextMessage(new JsonObject()
					.put("type", "subscribe")
					.put("id", "match-found")
					.put("payload", new JsonObject().put("query", """
							subscription {
							  matchFound { gameId }
							}"""))
					.encode());

			assertEquals(createdGameId, await(matchFound.future().timeout(15, TimeUnit.SECONDS)),
					"matchFound must deliver the game created before the subscription opened");

			await(ws.close());
			await(wsClient.close());
			await(Matchmaking.deleteQueue(queueId));
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

	private Future<JsonObject> graphqlTraced(WebClient webClient, String query, JsonObject variables, String token, String traceId, String spanId, String parentSpanId) {
		var body = new JsonObject().put("query", query);
		if (variables != null) {
			body.put("variables", variables);
		}
		var request = webClient.post("/graphql");
		if (token != null) {
			request.putHeader("Authorization", "Bearer " + token);
		}
		if (traceId != null && spanId != null) {
			request.putHeader("uber-trace-id", traceId + ":" + spanId + ":" + (parentSpanId != null ? parentSpanId : "0") + ":1");
			request.putHeader("traceparent", "00-" + traceId + "-" + spanId + "-01");
		}
		return request.sendJsonObject(body).map(HttpResponse::bodyAsJsonObject);
	}

	@Test
	@Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
	public void testOpenTracingEndToEndMatchmakingLifecycleAndClientIngressCollector(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAllWithMatchmaking(vertx));
			var webClient = graphqlClient(vertx);

			var accountData = await(createAccountViaGraphQL(webClient,
					UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(accountData);
			var userId = accountData.getJsonObject("userEntity").getString("id");
			var deckId = createDeck(webClient, token);

			// ── 1. Verify Matchmaking/enqueue stays open while queued and finishes with status="cancelled" on cancelMatchmaking ──
			var twoPlayerQueueId = UUID.randomUUID().toString();
			await(Matchmaking.createQueue(new MatchmakingQueues()
					.setId(twoPlayerQueueId)
					.setAutomaticallyClose(false)
					.setLobbySize(2)
					.setAwaitingLobbyTimeout(0L)
					.setBotOpponent(false)
					.setEmptyLobbyTimeout(0L)
					.setName("opentracing 2p cancel queue")
					.setPrivateLobby(false)
					.setOnce(false)
					.setStartsAutomatically(true)
					.setStillConnectedTimeout(0L)));

			com.hiddenswitch.diagnostics.Tracing.clearRecordedSpans();
			var cancelTraceId = "102030405060708090a0b0c0d0e0f001";
			var cancelClientSpanId = "1111222233334444";

			// Open matchFound subscription so verifyTicketLive succeeds while waiting in 2-player queue
			var wsCancelClient = vertx.createWebSocketClient();
			var wsCancel = await(wsCancelClient.connect(new WebSocketConnectOptions()
					.setHost("localhost")
					.setPort(GRAPHQL_PORT)
					.setURI("/graphql")
					.addSubProtocol("graphql-transport-ws")));
			var cancelWsAck = Promise.<Void>promise();
			wsCancel.textMessageHandler(text -> {
				var msg = new JsonObject(text);
				if ("connection_ack".equals(msg.getString("type"))) {
					cancelWsAck.tryComplete();
				}
			});
			wsCancel.writeTextMessage(new JsonObject()
					.put("type", "connection_init")
					.put("payload", new JsonObject()
							.put("Authorization", "Bearer " + token)
							.put("traceparent", "00-" + cancelTraceId + "-" + cancelClientSpanId + "-01"))
					.encode());
			await(cancelWsAck.future());
			wsCancel.writeTextMessage(new JsonObject()
					.put("type", "subscribe")
					.put("id", "match-cancel-sub")
					.put("payload", new JsonObject().put("query", "subscription { matchFound { gameId } }"))
					.encode());
			await(Environment.sleep(vertx, 100));

			var enqueueCancelRes = await(graphqlTraced(webClient,
					"""
					mutation Enqueue($input: MatchmakingEnqueueInput!) {
					  enqueueMatchmaking(input: $input)
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("deckId", deckId)
							.put("queueId", twoPlayerQueueId)),
					token, cancelTraceId, cancelClientSpanId, "0"));
			assertNull(enqueueCancelRes.getJsonArray("errors"), "enqueueMatchmaking errors: " + enqueueCancelRes);

			// Wait for at least one matchmaking scan so unplayableTicket release logs on the active span without finishing it
			await(Environment.sleep(vertx, 300));
			assertTrue(com.hiddenswitch.diagnostics.Tracing.getRecordedSpans().stream()
							.noneMatch(s -> "Matchmaking/enqueue".equals(s.operationName()) && cancelTraceId.equals(s.traceId())),
					"Matchmaking/enqueue span must remain open (unfinished) while the player is waiting in the matchmaking queue");

			var cancelRes = await(graphqlTraced(webClient,
					"mutation CancelMatchmaking { cancelMatchmaking }",
					null, token, cancelTraceId, "1111222233335555", cancelClientSpanId));
			assertNull(cancelRes.getJsonArray("errors"), "cancelMatchmaking errors: " + cancelRes);
			assertTrue(cancelRes.getJsonObject("data").getBoolean("cancelMatchmaking"));

			var cancelledEnqueueSpan = com.hiddenswitch.diagnostics.Tracing.getRecordedSpans().stream()
					.filter(s -> "Matchmaking/enqueue".equals(s.operationName()) && cancelTraceId.equals(s.traceId()))
					.findFirst()
					.orElse(null);
			assertNotNull(cancelledEnqueueSpan, "Matchmaking/enqueue span must be finished when cancelMatchmaking is called");
			assertEquals("cancelled", cancelledEnqueueSpan.tags().get("status"));
			assertEquals(Boolean.TRUE, cancelledEnqueueSpan.tags().get("cancelled"));
			assertEquals(userId, cancelledEnqueueSpan.tags().get("userId"));
			assertEquals(twoPlayerQueueId, cancelledEnqueueSpan.tags().get("queueId"));

			await(wsCancel.close());
			await(wsCancelClient.close());
			await(Matchmaking.deleteQueue(twoPlayerQueueId));

			// ── 2. Verify full end-to-end Matchmaking -> Game -> Client Ingress Collector trace tree ──
			var botQueueId = await(createBotQueue());
			com.hiddenswitch.diagnostics.Tracing.clearRecordedSpans();

			var matchTraceId = "abcdef0123456789abcdef0123456789";
			var clientMatchmakingSpanId = "2222333344445555";
			var clientEnqueueSpanId = "3333444455556666";
			var clientGameSessionSpanId = "4444555566667777";
			var clientConnectSpanId = "5555666677778888";
			var clientMulliganSpanId = "6666777788889999";
			var clientActionSpanId = "777788889999aaaa";
			var clientConcedeSpanId = "88889999aaaabbbb";

			var wsClient = vertx.createWebSocketClient();
			var ws = await(wsClient.connect(new WebSocketConnectOptions()
					.setHost("localhost")
					.setPort(GRAPHQL_PORT)
					.setURI("/graphql")
					.addSubProtocol("graphql-transport-ws")));

			var ackPromise = Promise.<Void>promise();
			var matchFoundPromise = Promise.<String>promise();
			var gameOverPromise = Promise.<JsonObject>promise();
			var actionSent = new java.util.concurrent.atomic.AtomicBoolean(false);

			ws.textMessageHandler(text -> {
				var msg = new JsonObject(text);
				var type = msg.getString("type");
				if ("connection_ack".equals(type)) {
					ackPromise.tryComplete();
					return;
				}
				if ("next".equals(type)) {
					var data = msg.getJsonObject("payload").getJsonObject("data");
					if (data.containsKey("matchFound")) {
						matchFoundPromise.tryComplete(data.getJsonObject("matchFound").getString("gameId"));
						return;
					}
					if (data.containsKey("gameMessages")) {
						var gameMessage = data.getJsonObject("gameMessages");
						var messageType = gameMessage.getString("messageType");
						switch (messageType) {
							case "ON_MULLIGAN" -> graphqlTraced(webClient,
									"""
									mutation SendMulligan($indices: [Int!]!, $repliesTo: String!) {
									  sendMulligan(discardedCardIndices: $indices, repliesTo: $repliesTo)
									}""",
									new JsonObject()
											.put("indices", new JsonArray().add(0))
											.put("repliesTo", gameMessage.getString("id")),
									token, matchTraceId, clientMulliganSpanId, clientGameSessionSpanId)
									.onFailure(gameOverPromise::tryFail);
							case "ON_REQUEST_ACTION" -> {
								if (actionSent.compareAndSet(false, true)) {
									graphqlTraced(webClient,
											"""
											mutation SendAction($actionIndex: Int!, $repliesTo: String!) {
											  sendGameAction(actionIndex: $actionIndex, repliesTo: $repliesTo)
											}""",
											new JsonObject()
													.put("actionIndex", 0)
													.put("repliesTo", gameMessage.getString("id")),
											token, matchTraceId, clientActionSpanId, clientGameSessionSpanId)
											.compose(v -> graphqlTraced(webClient,
													"mutation ConcedeGame { concedeGame }",
													null, token, matchTraceId, clientConcedeSpanId, clientGameSessionSpanId))
											.onFailure(gameOverPromise::tryFail);
								}
							}
							case "ON_GAME_END" -> gameOverPromise.tryComplete(gameMessage);
							default -> {
							}
						}
					}
				}
			});

			ws.writeTextMessage(new JsonObject()
					.put("type", "connection_init")
					.put("payload", new JsonObject()
							.put("Authorization", "Bearer " + token)
							.put("uber-trace-id", matchTraceId + ":" + clientMatchmakingSpanId + ":0:1"))
					.encode());
			await(ackPromise.future());

			ws.writeTextMessage(new JsonObject()
					.put("type", "subscribe")
					.put("id", "match-found-traced")
					.put("payload", new JsonObject().put("query", "subscription { matchFound { gameId } }"))
					.encode());

			var enqueueMatchRes = await(graphqlTraced(webClient,
					"""
					mutation Enqueue($input: MatchmakingEnqueueInput!) {
					  enqueueMatchmaking(input: $input)
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("deckId", deckId)
							.put("queueId", botQueueId)),
					token, matchTraceId, clientEnqueueSpanId, clientMatchmakingSpanId));
			assertNull(enqueueMatchRes.getJsonArray("errors"), "enqueueMatchmaking errors: " + enqueueMatchRes);

			var matchedGameId = await(matchFoundPromise.future().timeout(15, TimeUnit.SECONDS));
			assertNotNull(matchedGameId);

			// Subscribe to gameMessages and connectToGame
			ws.writeTextMessage(new JsonObject()
					.put("type", "subscribe")
					.put("id", "game-messages-traced")
					.put("payload", new JsonObject().put("query", """
							subscription {
							  gameMessages {
							    messageType
							    id
							    actions { all { action actionType } }
							    gameOver { localPlayerWon winningPlayerId }
							  }
							}"""))
					.encode());

			var connectRes = await(graphqlTraced(webClient,
					"""
					mutation Connect($key: String!, $secret: String!) {
					  connectToGame(playerKey: $key, playerSecret: $secret)
					}""",
					new JsonObject().put("key", "").put("secret", ""),
					token, matchTraceId, clientConnectSpanId, clientGameSessionSpanId));
			assertNull(connectRes.getJsonArray("errors"), "connectToGame errors: " + connectRes);

			var gameOverMessage = await(gameOverPromise.future().timeout(30, TimeUnit.SECONDS));
			assertNotNull(gameOverMessage);

			// Wait briefly for ServerGameContext/dispose and ServerGameContext/play to finish
			for (int i = 0; i < 20; i++) {
				if (com.hiddenswitch.diagnostics.Tracing.getRecordedSpans().stream()
						.anyMatch(s -> "ServerGameContext/dispose".equals(s.operationName()) && matchTraceId.equals(s.traceId()))) {
					break;
				}
				await(Environment.sleep(vertx, 50));
			}

			// ── 3. Simulate Unity client UnityTracing exporting spans to the same GraphQL ingress (/graphql/traces & /traces) ──
			var nowMicros = System.currentTimeMillis() * 1000L;
			var clientSpansPayload = new JsonObject()
					.put("serviceName", "spellsource-unity-client")
					.put("spans", new JsonArray()
							.add(new JsonObject()
									.put("traceId", matchTraceId)
									.put("spanId", clientMatchmakingSpanId)
									.put("parentSpanId", "0")
									.put("operationName", "Client/Matchmaking")
									.put("startTimeMicros", nowMicros - 500_000L)
									.put("durationMicros", 200_000L)
									.put("tags", new JsonObject()
											.put("queueId", botQueueId)
											.put("deckId", deckId)
											.put("gameId", matchedGameId)
											.put("status", "matched")))
							.add(new JsonObject()
									.put("traceId", matchTraceId)
									.put("spanId", clientEnqueueSpanId)
									.put("parentSpanId", clientMatchmakingSpanId)
									.put("operationName", "Client/EnqueueMatchmaking")
									.put("startTimeMicros", nowMicros - 490_000L)
									.put("durationMicros", 20_000L)
									.put("tags", new JsonObject().put("graphql.operation", "Enqueue")))
							.add(new JsonObject()
									.put("traceId", matchTraceId)
									.put("spanId", clientGameSessionSpanId)
									.put("parentSpanId", clientMatchmakingSpanId)
									.put("operationName", "Client/GameSession")
									.put("startTimeMicros", nowMicros - 300_000L)
									.put("durationMicros", 250_000L)
									.put("tags", new JsonObject()
											.put("gameId", matchedGameId)
											.put("status", "finished"))
									.put("logs", new JsonArray().add(new JsonObject()
											.put("timestampMicros", nowMicros - 60_000L)
											.put("fields", new JsonObject().put("event", "ON_GAME_END")))))
							.add(new JsonObject()
									.put("traceId", matchTraceId)
									.put("spanId", clientConnectSpanId)
									.put("parentSpanId", clientGameSessionSpanId)
									.put("operationName", "Client/ConnectToGame")
									.put("startTimeMicros", nowMicros - 290_000L)
									.put("durationMicros", 15_000L))
							.add(new JsonObject()
									.put("traceId", matchTraceId)
									.put("spanId", clientMulliganSpanId)
									.put("parentSpanId", clientGameSessionSpanId)
									.put("operationName", "Client/SendMulligan")
									.put("startTimeMicros", nowMicros - 200_000L)
									.put("durationMicros", 10_000L))
							.add(new JsonObject()
									.put("traceId", matchTraceId)
									.put("spanId", clientActionSpanId)
									.put("parentSpanId", clientGameSessionSpanId)
									.put("operationName", "Client/SendGameAction")
									.put("startTimeMicros", nowMicros - 120_000L)
									.put("durationMicros", 10_000L))
							.add(new JsonObject()
									.put("traceId", matchTraceId)
									.put("spanId", clientConcedeSpanId)
									.put("parentSpanId", clientGameSessionSpanId)
									.put("operationName", "Client/ConcedeGame")
									.put("startTimeMicros", nowMicros - 80_000L)
									.put("durationMicros", 10_000L)));

			var optionsRes = await(webClient.request(io.vertx.core.http.HttpMethod.OPTIONS, "/graphql/traces").send());
			assertEquals(204, optionsRes.statusCode(), "OPTIONS /graphql/traces CORS preflight should return 204");

			var traceIngestRes = await(webClient.post("/graphql/traces").sendJsonObject(clientSpansPayload));
			assertEquals(202, traceIngestRes.statusCode(), "POST /graphql/traces should return 202 Accepted");
			assertEquals(7, traceIngestRes.bodyAsJsonObject().getInteger("accepted"));

			// Verify /traces alias on the same ingress
			var rootTraceIngestRes = await(webClient.post("/traces").sendJsonObject(new JsonObject().put("spans", new JsonArray())));
			assertEquals(202, rootTraceIngestRes.statusCode(), "POST /traces should return 202 Accepted");

			// ── 4. Verify complete parent-child hierarchy across Client -> GraphQL -> Matchmaking -> ClusteredGames -> ServerGameContext -> GameLogic ──
			var traceSpans = com.hiddenswitch.diagnostics.Tracing.getRecordedSpans().stream()
					.filter(s -> matchTraceId.equals(s.traceId()))
					.toList();
			var byOp = traceSpans.stream()
					.collect(java.util.stream.Collectors.groupingBy(com.hiddenswitch.diagnostics.Tracing.RecordedSpan::operationName));

			assertTrue(byOp.containsKey("Client/Matchmaking"), "Client/Matchmaking span should be collected");
			assertTrue(byOp.containsKey("Client/GameSession"), "Client/GameSession span should be collected");
			assertTrue(byOp.containsKey("GraphQL/Enqueue"), "GraphQL/Enqueue span should be recorded under client traceId");
			assertTrue(byOp.containsKey("Matchmaking/enqueue"), "Matchmaking/enqueue span should be recorded under client traceId");
			assertTrue(byOp.containsKey("Matchmaking/startLobbyMatch"), "Matchmaking/startLobbyMatch span should be recorded under client traceId");
			assertTrue(byOp.containsKey("Matchmaking/createGame"), "Matchmaking/createGame span should be recorded under client traceId");
			assertTrue(byOp.containsKey("EventBus/games:createGameSession"), "EventBus/games:createGameSession span should be recorded under client traceId");
			assertTrue(byOp.containsKey("ClusteredGames/createGameSession"), "ClusteredGames/createGameSession span should be recorded under client traceId");
			assertTrue(byOp.containsKey("ServerGameContext/init"), "ServerGameContext/init span should be recorded under client traceId");
			assertTrue(byOp.containsKey("ServerGameContext/play"), "ServerGameContext/play span should be recorded under client traceId");
			assertTrue(byOp.containsKey("ServerGameContext/resume"), "ServerGameContext/resume span should be recorded under client traceId");
			assertTrue(byOp.containsKey("GameLogic/performGameAction"), "GameLogic/performGameAction span should be recorded under client traceId");
			assertTrue(byOp.containsKey("ServerGameContext/dispose"), "ServerGameContext/dispose span should be recorded under client traceId");

			var graphqlEnqueueSpan = byOp.get("GraphQL/Enqueue").get(0);
			var matchmakingEnqueueSpan = byOp.get("Matchmaking/enqueue").get(0);
			var startLobbySpan = byOp.get("Matchmaking/startLobbyMatch").get(0);
			var createGameSpan = byOp.get("Matchmaking/createGame").get(0);
			var ebCreateSessionSpan = byOp.get("EventBus/games:createGameSession").get(0);
			var createSessionSpan = byOp.get("ClusteredGames/createGameSession").get(0);
			var sgcInitSpan = byOp.get("ServerGameContext/init").get(0);
			var sgcPlaySpan = byOp.get("ServerGameContext/play").get(0);

			// Verify causal parent-child chain from Unity client span all the way to ServerGameContext/play
			assertEquals(clientEnqueueSpanId, graphqlEnqueueSpan.parentSpanId(),
					"GraphQL/Enqueue must be a child of Unity client's Client/EnqueueMatchmaking span");
			assertEquals(graphqlEnqueueSpan.spanId(), matchmakingEnqueueSpan.parentSpanId(),
					"Matchmaking/enqueue must be a child of GraphQL/Enqueue");
			assertEquals("matched", matchmakingEnqueueSpan.tags().get("status"),
					"Matchmaking/enqueue must finish with status='matched'");
			assertEquals(matchedGameId, matchmakingEnqueueSpan.tags().get("gameId"),
					"Matchmaking/enqueue must be tagged with the created gameId");
			assertEquals(userId, matchmakingEnqueueSpan.tags().get("userId"),
					"Matchmaking/enqueue must be tagged with userId");
			assertEquals(botQueueId, matchmakingEnqueueSpan.tags().get("queueId"),
					"Matchmaking/enqueue must be tagged with queueId");
			assertEquals(matchmakingEnqueueSpan.spanId(), startLobbySpan.parentSpanId(),
					"Matchmaking/startLobbyMatch must be linked as a child of Matchmaking/enqueue");
			assertEquals(startLobbySpan.spanId(), createGameSpan.parentSpanId(),
					"Matchmaking/createGame must be a child of Matchmaking/startLobbyMatch");
			assertEquals(createGameSpan.spanId(), ebCreateSessionSpan.parentSpanId(),
					"EventBus/games:createGameSession must be a child of Matchmaking/createGame");
			assertEquals(ebCreateSessionSpan.spanId(), createSessionSpan.parentSpanId(),
					"ClusteredGames/createGameSession must be a child of EventBus/games:createGameSession");
			assertEquals(createSessionSpan.spanId(), sgcInitSpan.parentSpanId(),
					"ServerGameContext/init must be a child of ClusteredGames/createGameSession");
			assertEquals(sgcInitSpan.spanId(), sgcPlaySpan.parentSpanId(),
					"ServerGameContext/play must be a child of ServerGameContext/init");

			// Verify GameLogic/performGameAction is parented to a ServerGameContext/resume span
			var resumeSpanIds = byOp.get("ServerGameContext/resume").stream()
					.map(com.hiddenswitch.diagnostics.Tracing.RecordedSpan::spanId)
					.collect(java.util.stream.Collectors.toSet());
			assertTrue(byOp.get("GameLogic/performGameAction").stream()
							.anyMatch(s -> resumeSpanIds.contains(s.parentSpanId())),
					"GameLogic/performGameAction must be a child of ServerGameContext/resume");

			await(ws.close());
			await(wsClient.close());
			await(Matchmaking.deleteQueue(botQueueId));
		});
	}

	@Test
	@Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
	public void testSupersedingMatchFoundSubscriptionDoesNotCancelNewTicket(Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startAllWithMatchmaking(vertx));
			var webClient = graphqlClient(vertx);
			var accountData = await(createAccountViaGraphQL(webClient,
					UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token = extractToken(accountData);
			var deckId = createDeck(webClient, token);
			var queueId = UUID.randomUUID().toString();
			await(Matchmaking.createQueue(new MatchmakingQueues()
					.setId(queueId)
					.setAutomaticallyClose(false)
					.setLobbySize(2)
					.setAwaitingLobbyTimeout(0L)
					.setBotOpponent(false)
					.setEmptyLobbyTimeout(0L)
					.setName("supersede 2p queue")
					.setPrivateLobby(false)
					.setOnce(false)
					.setStartsAutomatically(true)
					.setStillConnectedTimeout(0L)));

			var wsClient = vertx.createWebSocketClient();
			var connectOptions = new WebSocketConnectOptions()
					.setHost("localhost")
					.setPort(GRAPHQL_PORT)
					.setURI("/graphql")
					.addSubProtocol("graphql-transport-ws");

			// 1. Open stale WebSocket #1 and subscribe to matchFound
			var ws1 = await(wsClient.connect(connectOptions));
			var ack1 = Promise.<Void>promise();
			ws1.textMessageHandler(text -> {
				var msg = new JsonObject(text);
				if ("connection_ack".equals(msg.getString("type"))) {
					ack1.tryComplete();
				}
			});
			ws1.writeTextMessage(new JsonObject()
					.put("type", "connection_init")
					.put("payload", new JsonObject().put("Authorization", "Bearer " + token))
					.encode());
			await(ack1.future());
			ws1.writeTextMessage(new JsonObject()
					.put("type", "subscribe")
					.put("id", "match-stale")
					.put("payload", new JsonObject().put("query", "subscription { matchFound { gameId } }"))
					.encode());
			await(Environment.sleep(vertx, 100));

			// 2. Client reconnects on WebSocket #2, subscribes to matchFound, and enqueues
			var ws2 = await(wsClient.connect(connectOptions));
			var ack2 = Promise.<Void>promise();
			var matchFound2 = Promise.<String>promise();
			ws2.textMessageHandler(text -> {
				var msg = new JsonObject(text);
				if ("connection_ack".equals(msg.getString("type"))) {
					ack2.tryComplete();
				} else if ("next".equals(msg.getString("type")) && "match-live".equals(msg.getString("id"))) {
					var mf = msg.getJsonObject("payload").getJsonObject("data").getJsonObject("matchFound");
					if (mf != null) {
						matchFound2.tryComplete(mf.getString("gameId"));
					}
				}
			});
			ws2.writeTextMessage(new JsonObject()
					.put("type", "connection_init")
					.put("payload", new JsonObject().put("Authorization", "Bearer " + token))
					.encode());
			await(ack2.future());
			ws2.writeTextMessage(new JsonObject()
					.put("type", "subscribe")
					.put("id", "match-live")
					.put("payload", new JsonObject().put("query", "subscription { matchFound { gameId } }"))
					.encode());
			await(Environment.sleep(vertx, 100));

			var enqueueRes = await(graphql(webClient,
					"""
					mutation Enqueue($input: MatchmakingEnqueueInput!) {
					  enqueueMatchmaking(input: $input)
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("queueId", queueId)
							.put("deckId", deckId)),
					token));
			assertNull(enqueueRes.getJsonArray("errors"), "enqueueMatchmaking errors: " + enqueueRes);

			// 3. Stale WebSocket #1 finally closes/cancels after WebSocket #2 has already enqueued
			await(ws1.close());
			await(Environment.sleep(vertx, 150));

			// 4. Second player enqueues into the same queue -> should match with Player 1 on ws2!
			var accountData2 = await(createAccountViaGraphQL(webClient,
					UUID.randomUUID() + "@test.com", UUID.randomUUID().toString(), "password"));
			var token2 = extractToken(accountData2);
			var deckId2 = createDeck(webClient, token2);
			var wsP2 = await(wsClient.connect(connectOptions));
			var ackP2 = Promise.<Void>promise();
			wsP2.textMessageHandler(text -> {
				var msg = new JsonObject(text);
				if ("connection_ack".equals(msg.getString("type"))) {
					ackP2.tryComplete();
				}
			});
			wsP2.writeTextMessage(new JsonObject()
					.put("type", "connection_init")
					.put("payload", new JsonObject().put("Authorization", "Bearer " + token2))
					.encode());
			await(ackP2.future());
			wsP2.writeTextMessage(new JsonObject()
					.put("type", "subscribe")
					.put("id", "match-p2")
					.put("payload", new JsonObject().put("query", "subscription { matchFound { gameId } }"))
					.encode());
			await(Environment.sleep(vertx, 100));
			await(graphql(webClient,
					"""
					mutation Enqueue($input: MatchmakingEnqueueInput!) {
					  enqueueMatchmaking(input: $input)
					}""",
					new JsonObject().put("input", new JsonObject()
							.put("queueId", queueId)
							.put("deckId", deckId2)),
					token2));

			var gameId = await(matchFound2.future().timeout(5, TimeUnit.SECONDS));
			assertNotNull(gameId, "Live matchFound subscription #2 should receive gameId even after stale subscription #1 closes");

			// Also verify VirtualThreadRoutingContextHandler operationName branches and non-virtual thread guard
			var opNameRes = await(webClient.post("/graphql")
					.putHeader("Authorization", "Bearer " + token)
					.sendJsonObject(new JsonObject()
							.put("operationName", "ExplicitAccountOp")
							.put("query", "query ExplicitAccountOp { account { id } }"))
					.map(HttpResponse::bodyAsJsonObject));
			assertNull(opNameRes.getJsonArray("errors"));

			var blankOpRes = await(webClient.post("/graphql")
					.putHeader("Authorization", "Bearer " + token)
					.sendJsonObject(new JsonObject()
							.put("operationName", "")
							.put("query", "{ account { id } }"))
					.map(HttpResponse::bodyAsJsonObject));
			assertNull(blankOpRes.getJsonArray("errors"));

			// Also exercise VirtualThreadRoutingContextHandler with empty body and null context user()
			await(webClient.post("/graphql")
					.putHeader("Authorization", "Bearer " + token)
					.send());
			com.hiddenswitch.framework.virtual.VirtualThreadRoutingContextHandler.clearUser();
			assertNull(com.hiddenswitch.framework.virtual.VirtualThreadRoutingContextHandler.user());

			var subResolver = new com.hiddenswitch.framework.impl.GraphQLSubscriptionResolverImpl();
			assertThrows(Exception.class, () -> subResolver.gameMessages("conn"));
			assertThrows(Exception.class, subResolver::matchFound);
			assertThrows(Exception.class, subResolver::friendUpdated);
			assertThrows(Exception.class, subResolver::inviteUpdated);
			assertThrows(Exception.class, subResolver::editableCardUpdated);

			var nonVtDone = Promise.<Void>promise();
			((io.vertx.core.internal.VertxInternal) vertx).createEventLoopContext().runOnContext(_ -> {
				try {
					assertThrows(IllegalStateException.class, () ->
							com.hiddenswitch.framework.virtual.VirtualThreadRoutingContextHandler.create(rc -> {}).handle(null));
					nonVtDone.complete();
				} catch (Throwable t) {
					nonVtDone.fail(t);
				}
			});
			await(nonVtDone.future());

			// Exercise GraphQLGameBridge.toGraphQL optional proto branches (draw GameOver, empty Changes, CardEvent without card, Art & Entity optional fields)
			var art1 = Spellsource.Art.newBuilder();
			art1.getBodyBuilder();
			var art2 = Spellsource.Art.newBuilder()
					.setHighlight(Spellsource.Color.newBuilder().setR(0.5f))
					.setPrimary(Spellsource.Color.newBuilder().setG(0.5f))
					.setSecondary(Spellsource.Color.newBuilder().setB(0.5f))
					.setShadow(Spellsource.Color.newBuilder().setA(0.5f));
			art2.getBodyBuilder().setVertex(Spellsource.Color.newBuilder().setR(1f).setG(1f).setB(1f).setA(1f));
			art2.getLoopBuilder().setNamed("loop");
			art2.getMissileBuilder().setNamed("missile");
			art2.getOnCastBuilder().setNamed("onCast");
			art2.getOnHitBuilder().setNamed("onHit");
			art2.getSpellBuilder().setNamed("spell");
			art2.getSpriteBuilder().setNamed("sprite");
			art2.getSpriteShadowBuilder().setNamed("spriteShadow");

			var eventBuilder = Spellsource.GameEvent.newBuilder()
					.setEventType(Spellsource.GameEventTypeMessage.GameEventType.PERFORMED_GAME_ACTION)
					.setEntityTouched(10)
					.setEntityUntouched(11);
			eventBuilder.getCardEventBuilder().setShowLocal(true);

			var emptyProtoMsg = com.hiddenswitch.framework.impl.GraphQLGameBridge.toGraphQL(
					Spellsource.ServerToClientMessage.newBuilder()
							.setMessageType(Spellsource.MessageTypeMessage.MessageType.ON_GAME_END)
							.setId("msg-draw")
							.setGameOver(Spellsource.GameOver.newBuilder().setLocalPlayerWon(false))
							.setChanges(Spellsource.EntityChangeSet.newBuilder())
							.setEvent(eventBuilder)
							.addStartingCards(Spellsource.Entity.newBuilder()
									.setId(1)
									.setCardType(Spellsource.CardTypeMessage.CardType.WEAPON)
									.setEntityType(Spellsource.EntityTypeMessage.EntityType.CARD)
									.setRarity(Spellsource.RarityMessage.Rarity.LEGENDARY)
									.setArt(art1)
									.setDurability(3)
									.setSpellDamage(2)
									.setOverload(1)
									.setExtraAttack(4)
									.setCharges(2)
									.setCountUntilCast(3)
									.setFires(5))
							.addStartingCards(Spellsource.Entity.newBuilder()
									.setId(2)
									.setCardType(Spellsource.CardTypeMessage.CardType.HERO)
									.setEntityType(Spellsource.EntityTypeMessage.EntityType.HERO)
									.setRarity(Spellsource.RarityMessage.Rarity.FREE)
									.setArt(art2))
							.build());
			assertNull(emptyProtoMsg.getGameOver().getWinningPlayerId());
			assertNull(emptyProtoMsg.getChangedEntityIds());
			assertNull(emptyProtoMsg.getEvent().getCardEvent().getCard());
			assertEquals(Integer.valueOf(10), emptyProtoMsg.getEvent().getEntityTouched());
			assertEquals(Integer.valueOf(11), emptyProtoMsg.getEvent().getEntityUntouched());
			assertEquals(Integer.valueOf(3), emptyProtoMsg.getStartingCards().get(0).getDurability());
			assertNotNull(emptyProtoMsg.getStartingCards().get(1).getArt().getBody().getVertex());
			assertNotNull(emptyProtoMsg.getStartingCards().get(1).getArt().getSpriteShadow());

			await(ws2.close());
			await(wsP2.close());
			await(wsClient.close());
			await(Matchmaking.deleteQueue(queueId));
		});
	}
}
