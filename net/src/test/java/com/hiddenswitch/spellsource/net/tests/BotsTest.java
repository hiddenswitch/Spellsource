package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.net.Accounts;
import com.hiddenswitch.spellsource.net.Bots;
import com.hiddenswitch.spellsource.net.Games;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.net.impl.*;
import com.hiddenswitch.spellsource.net.models.MulliganRequest;
import com.hiddenswitch.spellsource.net.models.RequestActionRequest;
import com.hiddenswitch.spellsource.net.models.RequestActionResponse;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.ext.unit.TestContext;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.tests.util.DebugContext;
import net.demilich.metastone.tests.util.TestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static org.junit.Assert.*;

public class BotsTest extends SpellsourceTestBase {

	@Test
	public void testMulligan(TestContext context) {
		sync(() -> {
			MulliganRequest request = new MulliganRequest(
					Arrays.asList(
							CardCatalogue.getCardById("spell_test_deal_6"),
							CardCatalogue.getCardById("minion_test_3_2"),
							CardCatalogue.getCardById("spell_test_summon_tokens")));
			assertEquals(1, Bots.mulligan(request).discardedCards.size());
		}, context);
	}

	@Test
	public void testRequestAction(TestContext context) {
		sync(() -> {
			TestBase testBase = new TestBase();
			DebugContext context1 = testBase.createContext("JADE", "JADE");
			context1.endTurn();
			context1.startTurn(context1.getActivePlayerId());
			int startTurn = context1.getTurn();
			GameAction gameAction = null;
			Bots.BEHAVIOUR.set(() -> new GameStateValueBehaviour().setParallel(false));
			while (gameAction == null
					|| gameAction.getActionType() != ActionType.END_TURN) {
				RequestActionRequest requestActionRequest = new RequestActionRequest(new GameId(context1.getGameId()),
						context1.getActivePlayerId(),
						context1.getValidActions(),
						context1.getDeckFormat(), context1.getGameStateCopy(),
						null);

				RequestActionResponse response = Bots.requestAction(requestActionRequest);
				gameAction = response.gameAction;
				assertNotNull(gameAction);
				context1.performAction(context1.getActivePlayerId(), gameAction);
			}
			Bots.BEHAVIOUR.set(PlayRandomBehaviour::new);
			assertTrue(context1.getTurn() > startTurn);
		}, context);
	}

	@Test
	public void testBotReused(TestContext context) {
		sync(() -> {
			try (UnityClient client = new UnityClient(context)) {
				Sync.invoke0(client::createUserAccount);

				NoArgs playAndWait = () -> {
					client.matchmakeQuickPlay(null);
					client.waitUntilDone();
					context.assertTrue(client.isGameOver());
					context.assertTrue(client.getTurnsPlayed() > 0);
					try {
						assertFalse(client.getApi().getAccount(client.getAccount().getId()).getAccounts().get(0).isInMatch());
					} catch (ApiException e) {
						throw new AssertionError(e);
					}
				};
				Mongo.mongo().removeDocuments(Accounts.USERS, json("bot", true));
				Sync.invoke0(playAndWait);
				List<String> botIds = Bots.getBotIds();
				assertEquals("Only one bot document should have been created", botIds.size(), 1);
				SuspendableMap<UserId, GameId> games = Games.getUsersInGames();

				for (String id : botIds) {
					assertFalse(games.containsKey(new UserId(id)));
				}

				Sync.invoke0(playAndWait);
				assertEquals("Only one bot document should have been created", botIds.size(), 1);
				for (String id : botIds) {
					assertFalse(games.containsKey(new UserId(id)));
				}
			}
		}, context);
	}

	@Test
	public void testBotUpdateDecklist(TestContext context) {
		sync(() -> {
			UserId botId = Bots.pollBotId();
			Bots.updateBotDeckList();
			context.assertTrue(Accounts.get(botId.toString()).getDecks().size() > 0);
		}, context);
	}
}