package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.models.MulliganRequest;
import com.hiddenswitch.spellsource.models.MulliganResponse;
import com.hiddenswitch.spellsource.models.RequestActionRequest;
import com.hiddenswitch.spellsource.models.RequestActionResponse;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by bberman on 12/7/16.
 */
public class BotsTest extends SpellsourceTestBase {

	@Test
	public void testMulligan(TestContext context) {
		Logging.setLoggingLevel(Level.ERROR);
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
				RequestActionRequest requestActionRequest = new RequestActionRequest(
						context1.getGameStateCopy(),
						context1.getActivePlayerId(),
						context1.getValidActions(),
						context1.getDeckFormat());

				RequestActionResponse response = Bots.requestAction(requestActionRequest);
				gameAction = response.gameAction;
				assertNotNull(gameAction);
				context1.getLogic().performGameAction(context1.getActivePlayerId(), gameAction);
			}
			assertTrue(context1.getTurn() > startTurn);
		});
	}

	@Test
	public void testBroker(TestContext context) throws CardParseException {
		sync(() -> {
			final RpcClient<Bots> bots = Rpc.connect(Bots.class);
			final MulliganRequest request = new MulliganRequest(
					Arrays.asList(
							CardCatalogue.getCardById("spell_fireball"),
							CardCatalogue.getCardById("spell_arcane_missiles"),
							CardCatalogue.getCardById("spell_assassinate")));
			MulliganResponse r = Bots.mulligan(request);
			assertEquals(2, r.discardedCards.size());
		});
	}
}