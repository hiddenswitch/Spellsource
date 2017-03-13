package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.proto3.net.common.GameState;
import com.hiddenswitch.proto3.net.impl.BotsImpl;
import com.hiddenswitch.proto3.net.impl.GamesImpl;
import com.hiddenswitch.proto3.net.models.MulliganRequest;
import com.hiddenswitch.proto3.net.models.MulliganResponse;
import com.hiddenswitch.proto3.net.models.RequestActionRequest;
import com.hiddenswitch.proto3.net.models.RequestActionResponse;
import com.hiddenswitch.proto3.net.util.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.tests.DebugContext;
import net.demilich.metastone.tests.TestBase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import static net.demilich.metastone.game.GameContext.PLAYER_1;

/**
 * Created by bberman on 12/7/16.
 */
@RunWith(VertxUnitRunner.class)
public class BotsTest extends ServiceTest<BotsImpl> {
	private GamesImpl games;

	@Test
	public void testMulligan(TestContext context) throws Exception {
		setLoggingLevel(Level.ERROR);
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
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::requestAction);
	}

	private void requestAction() throws SuspendExecution, InterruptedException {
		DebugContext context = TestBase.createContext(HeroClass.HUNTER, HeroClass.PALADIN);
		context.endTurn();
		context.forceStartTurn(context.getActivePlayerId());
		int startTurn = context.getTurn();
		GameAction gameAction = null;
		while (gameAction == null
				|| gameAction.getActionType() != ActionType.END_TURN) {
			RequestActionRequest requestActionRequest = new RequestActionRequest(
					new GameState(context),
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
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			final ServiceProxy<Bots> bots = Broker.proxy(Bots.class, vertx.eventBus());
			final MulliganRequest request = new MulliganRequest(
					Arrays.asList(
							CardCatalogue.getCardById("spell_fireball"),
							CardCatalogue.getCardById("spell_arcane_missiles"),
							CardCatalogue.getCardById("spell_assassinate")));
			MulliganResponse r = bots.sync().mulligan(request);
			context.assertTrue(r.discardedCards.size() == 2);
		});
	}

	@Test
	public void testPlaysGameAgainstAI(TestContext context) throws CardParseException, IOException, URISyntaxException, SuspendExecution {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::playAgainstAI);
	}

	private void playAgainstAI() throws SuspendExecution, InterruptedException {
		TwoClients twoClients = null;

		try {
			twoClients = new TwoClients().invoke(games, true);
		} catch (IOException | URISyntaxException | CardParseException e) {
			throw new AssertionError();
		}

		twoClients.play(PLAYER_1);
		float time = 0f;
		while (time < 120f && !twoClients.gameDecided()) {
			Strand.sleep(1000);
			time += 1.0f;
		}
		twoClients.assertGameOver();

	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<BotsImpl>> done) {
		games = new GamesImpl();
		BotsImpl instance = new BotsImpl();

		vertx.deployVerticle(games, then1 -> {
			vertx.deployVerticle(instance, then -> {
				done.handle(Future.succeededFuture(instance));
			});
		});
	}
}