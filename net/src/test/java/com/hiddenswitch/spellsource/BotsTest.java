package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.models.MulliganRequest;
import com.hiddenswitch.spellsource.models.MulliganResponse;
import com.hiddenswitch.spellsource.models.RequestActionRequest;
import com.hiddenswitch.spellsource.models.RequestActionResponse;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.FiberBehaviour;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static org.junit.Assert.*;

public class BotsTest extends SpellsourceTestBase {

	@Test
	public void testMulligan(TestContext context) {
		sync(() -> {
			MulliganRequest request = new MulliganRequest(
					Arrays.asList(
							CardCatalogue.getCardById("spell_fireball"),
							CardCatalogue.getCardById("spell_arcane_missiles"),
							CardCatalogue.getCardById("spell_assassinate")));
			assertEquals(2, Bots.mulligan(request).discardedCards.size());
		});
	}

	@Test
	public void testRequestAction(TestContext context) {
		sync(() -> {
			DebugContext context1 = TestBase.createContext(HeroClass.GREEN, HeroClass.GOLD);
			context1.endTurn();
			context1.forceStartTurn(context1.getActivePlayerId());
			int startTurn = context1.getTurn();
			GameAction gameAction = null;
			while (gameAction == null
					|| gameAction.getActionType() != ActionType.END_TURN) {
				RequestActionRequest requestActionRequest = new RequestActionRequest(new GameId(context1.getGameId()),
						context1.getActivePlayerId(),
						context1.getValidActions(),
						context1.getDeckFormat(), context1.getGameStateCopy());

				RequestActionResponse response = Bots.requestAction(requestActionRequest);
				gameAction = response.gameAction;
				assertNotNull(gameAction);
				context1.getLogic().performGameAction(context1.getActivePlayerId(), gameAction);
			}
			assertTrue(context1.getTurn() > startTurn);
		});
	}

	@Test
	public void testFiberBehaviour(TestContext context) {
		sync(() -> {
			GameContext gc = GameContext.fromTwoRandomDecks();
			FiberBehaviour fb1 = new FiberBehaviour();
			FiberBehaviour fb2 = new FiberBehaviour();
			gc.setBehaviour(0, fb1);
			gc.setBehaviour(1, fb2);
			gc.play();
			while (!fb1.getMulliganCards().isEmpty() || !fb2.getMulliganCards().isEmpty()) {
				fb1.setMulligan(Collections.emptyList());
				fb2.setMulligan(Collections.emptyList());
			}
			FiberBehaviour active = (FiberBehaviour) gc.getBehaviours().get(gc.getActivePlayerId());
			assertTrue(!active.getValidActions().isEmpty());
			active.setAction(active.getValidActions().get(0));
		});
	}

	@Test
	public void testBotReused(TestContext context) {
		UnityClient client = new UnityClient(context);
		client.createUserAccount();
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
		sync(() -> {
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
		});
	}
}