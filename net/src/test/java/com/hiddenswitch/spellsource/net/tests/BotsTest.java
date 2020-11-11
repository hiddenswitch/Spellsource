package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.models.ActionType;
import com.hiddenswitch.spellsource.net.Accounts;
import com.hiddenswitch.spellsource.net.Bots;
import com.hiddenswitch.spellsource.net.Games;
import com.hiddenswitch.spellsource.net.impl.GameId;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.models.BotMulliganRequest;
import com.hiddenswitch.spellsource.net.models.RequestActionRequest;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.ext.sync.Sync;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.tests.util.TestBase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static org.junit.jupiter.api.Assertions.*;

public class BotsTest extends SpellsourceTestBase {

	@Test
	public void testMulligan(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var request = new BotMulliganRequest(
					Arrays.asList(
							CardCatalogue.getCardById("spell_test_deal_6"),
							CardCatalogue.getCardById("minion_test_3_2"),
							CardCatalogue.getCardById("spell_test_summon_tokens")));
			assertEquals(1, Bots.mulligan(request).discardedCards.size());
		}, context, vertx);
	}

	@Test
	public void testRequestAction(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var testBase = new TestBase();
			var context1 = testBase.createContext("JADE", "JADE");
			context1.endTurn();
			context1.startTurn(context1.getActivePlayerId());
			var startTurn = context1.getTurn();
			GameAction gameAction = null;
			Bots.BEHAVIOUR.set(() -> new GameStateValueBehaviour().setParallel(false));
			while (gameAction == null
					|| gameAction.getActionType() != ActionType.END_TURN) {
				var requestActionRequest = new RequestActionRequest(new GameId(context1.getGameId()),
						context1.getActivePlayerId(),
						context1.getValidActions(),
						context1.getDeckFormat(), context1.getGameStateCopy(),
						null);

				var response = Bots.requestAction(requestActionRequest);
				gameAction = response.gameAction;
				assertNotNull(gameAction);
				context1.performAction(context1.getActivePlayerId(), gameAction);
			}
			Bots.BEHAVIOUR.set(PlayRandomBehaviour::new);
			assertTrue(context1.getTurn() > startTurn);
		}, context, vertx);
	}

	@Test
	public void testBotReused(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			try (var client = new UnityClient(context)) {
				Sync.invoke0(client::createUserAccount);

				Sync.NoArgs playAndWait = () -> {
					verify(context, () -> {
						client.matchmakeQuickPlay(null);
						client.waitUntilDone();
						assertTrue(client.isGameOver());
						assertTrue(client.getTurnsPlayed() > 0);
						try {
							assertFalse(client.getApi().getAccount(client.getAccount().getId()).getAccounts().get(0).isInMatch());
						} catch (ApiException e) {
							throw new AssertionError(e);
						}
					});
				};
				mongo().removeDocuments(Accounts.USERS, json("bot", true));
				io.vertx.ext.sync.Sync.invoke0(playAndWait);
				var botIds = Bots.getBotIds();
				assertEquals(botIds.size(), 1, "Only one bot document should have been created");

				for (var id : botIds) {
					assertFalse(Games.isInGame(new UserId(id)));
				}

				io.vertx.ext.sync.Sync.invoke0(playAndWait);
				assertEquals(botIds.size(), 1, "Only one bot document should have been created");
				for (var id : botIds) {
					assertFalse(Games.isInGame(new UserId(id)));
				}
			}
		}, context, vertx);
	}

	@Test
	public void testBotUpdateDecklist(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var botId = Bots.pollBotId();
			Bots.updateBotDeckList();
			assertTrue(Accounts.get(botId.toString()).getDecks().size() > 0);
		}, context, vertx);
	}
}