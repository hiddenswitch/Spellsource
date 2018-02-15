package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.impl.BotsImpl;
import com.hiddenswitch.spellsource.impl.ClusteredGamesImpl;
import com.hiddenswitch.spellsource.impl.ServiceTest;
import com.hiddenswitch.spellsource.models.MulliganRequest;
import com.hiddenswitch.spellsource.models.MulliganResponse;
import com.hiddenswitch.spellsource.models.RequestActionRequest;
import com.hiddenswitch.spellsource.models.RequestActionResponse;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by bberman on 12/7/16.
 */
@RunWith(VertxUnitRunner.class)
public class BotsTest extends ServiceTest<BotsImpl> {
	private ClusteredGamesImpl games;

	@Test
	public void testMulligan(TestContext context) throws Exception {
		Logging.setLoggingLevel(Level.ERROR);
		wrapSync(context, this::mulligan);
	}

	private void mulligan() throws SuspendExecution, InterruptedException {
		MulliganRequest request = new MulliganRequest(
				Arrays.asList(
						CardCatalogue.getCardById("spell_fireball"),
						CardCatalogue.getCardById("spell_arcane_missiles"),
						CardCatalogue.getCardById("spell_assassinate")));
		getContext().assertTrue(service.mulligan(request).discardedCards.size() == 2);
	}

	@Test
	public void testRequestAction(TestContext context) throws Exception {
		Logging.setLoggingLevel(Level.ERROR);
		wrapSync(context, this::requestAction);
	}

	private void requestAction() throws SuspendExecution, InterruptedException {
		DebugContext context = TestBase.createContext(HeroClass.GREEN, HeroClass.GOLD);
		context.endTurn();
		context.forceStartTurn(context.getActivePlayerId());
		int startTurn = context.getTurn();
		GameAction gameAction = null;
		while (gameAction == null
				|| gameAction.getActionType() != ActionType.END_TURN) {
			RequestActionRequest requestActionRequest = new RequestActionRequest(
					context.getGameStateCopy(),
					context.getActivePlayerId(),
					context.getValidActions(),
					context.getDeckFormat());

			RequestActionResponse response = service.requestAction(requestActionRequest);
			gameAction = response.gameAction;
			getContext().assertNotNull(gameAction);
			context.getLogic().performGameAction(context.getActivePlayerId(), gameAction);
		}
		getContext().assertTrue(context.getTurn() > startTurn);
	}

	@Test
	public void testBroker(TestContext context) throws CardParseException, IOException, URISyntaxException {
		Logging.setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			final RpcClient<Bots> bots = Rpc.connect(Bots.class, vertx.eventBus());
			final MulliganRequest request = new MulliganRequest(
					Arrays.asList(
							CardCatalogue.getCardById("spell_fireball"),
							CardCatalogue.getCardById("spell_arcane_missiles"),
							CardCatalogue.getCardById("spell_assassinate")));
			MulliganResponse r = bots.sync().mulligan(request);
			context.assertTrue(r.discardedCards.size() == 2);
		});
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<BotsImpl>> done) {
		games = new ClusteredGamesImpl();
		BotsImpl instance = new BotsImpl();

		vertx.deployVerticle(games, then1 -> {
			vertx.deployVerticle(instance, then -> {
				done.handle(Future.succeededFuture(instance));
			});
		});
	}
}