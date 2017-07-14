package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.proto3.net.impl.GamesImpl;
import com.hiddenswitch.proto3.net.impl.MatchmakingImpl;
import com.hiddenswitch.proto3.net.impl.server.GameSession;
import com.hiddenswitch.proto3.net.impl.server.PregamePlayerConfiguration;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.impl.ServiceTest;
import com.hiddenswitch.proto3.net.util.TwoClients;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import net.demilich.metastone.game.actions.PlaySpellCardAction;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardParseException;
import net.demilich.metastone.game.cards.SpellCard;
import net.demilich.metastone.game.decks.DeckFactory;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.TargetSelection;
import net.demilich.metastone.game.targeting.Zones;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static net.demilich.metastone.game.GameContext.PLAYER_2;

@RunWith(VertxUnitRunner.class)
public class GamesTest extends ServiceTest<GamesImpl> {
	private Logger logger = LoggerFactory.getLogger(GamesTest.class);
	private MatchmakingImpl matchmaking;

	@Test
	public void testCreateGameSession(TestContext context) throws CardParseException, IOException, URISyntaxException {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::getAndTestTwoClients);
	}

	private TwoClients getAndTestTwoClients() throws SuspendExecution, InterruptedException {
		TwoClients twoClients = null;

		twoClients = new TwoClients().invoke(this.service);


		twoClients.play();
		float seconds = 0.0f;
		while (seconds <= 40.0f && !twoClients.gameDecided()) {
			if (twoClients.isInterrupted()) {
				break;
			}
			Strand.sleep(1000);
			seconds += 1.0f;
		}

		twoClients.assertGameOver();
		twoClients.dispose();
		return twoClients;
	}

	@Test
	public void testTwoGameSessionsOneAfterAnother(TestContext context) throws CardParseException, IOException, URISyntaxException {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			getAndTestTwoClients();
			getAndTestTwoClients();
		});
	}

	@Test()
	public void testTerminatingSession(TestContext context) throws CardParseException, IOException, URISyntaxException {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			TwoClients clients1 = new TwoClients().invoke(this.service);
			getContext().assertNotNull(clients1);
			clients1.play();
			logger.info("testTerminatingSession: Waiting for game to start...");
			while (clients1.getServerGameContext() == null) {
				Strand.sleep(100);
			}
			logger.info("testTerminatingSession: Waiting for game to reach turn 3...");
			while (clients1.getServerGameContext().getTurn() < 3) {
				Strand.sleep(100);
			}
			String gameId = clients1.getGameId();
			service.endGameSession(new EndGameSessionRequest(gameId));
			getContext().assertNull(service.getGameSession(gameId));
			logger.info("testTerminatingSession: Waiting for players to receive game end message...");
			while (!clients1.getPlayerContext1().updateAndGetGameOver()
					&& !clients1.getPlayerContext2().updateAndGetGameOver()) {
				Strand.sleep(100);
			}
			clients1.assertGameOver();
		});
	}

	@Test()
	public void testTimeoutSession(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			TwoClients clients1 = new TwoClients().invoke(this.service, 10000L);
			clients1.play();
			while (clients1.getServerGameContext() == null
					|| clients1.getServerGameContext().getTurn() < 3) {
				Strand.sleep(100);
			}
			String gameId = clients1.getGameId();
			clients1.disconnect(0);
			// This is greater than the timeout
			Strand.sleep(14000L);
			// From player 2's point of view, the game should be decided because it's over
			getContext().assertNull(service.getGameSession(gameId));
			getContext().assertTrue(clients1.getPlayerContext2().updateAndGetGameOver());
		});
	}

	@Test()
	public void testRemoveSessionAfterNormalGameOver(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			TwoClients twoClients = getAndTestTwoClients();
			String gameId = twoClients.getGameId();
			// Exceeds the cleanup time
			Strand.sleep(4000L);
			getContext().assertNull(service.getGameSession(gameId));
		});
	}

	@Test()
	public void testTwoSimultaneousSessions(TestContext context) throws Exception {
		setLoggingLevel(Level.ERROR);

		wrapSync(context, () -> {
			simultaneousSessions(2);
		});
	}

	@Test(timeout = 4 * 60 * 1000L)
	public void testTenSimultaneousSessionsTwice(TestContext context) throws Exception {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			for (int i = 0; i < 2; i++) {
				simultaneousSessions(10);
				logger.info("Iteration completed : " + (i + 1));
			}
		});
	}

	@Test()
	public void testReconnects(TestContext context) throws Exception {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			TwoClients twoClients = null;
			try {
				twoClients = new TwoClients().invoke(this.service);
				twoClients.play();
				while (twoClients.getServerGameContext() == null
						|| twoClients.getServerGameContext().getTurn() < 3) {
					Strand.sleep(100);
				}
				twoClients.disconnect(PLAYER_2);
				Strand.sleep(1000);
				// Try to reconnect
				twoClients.connect(PLAYER_2);
				twoClients.play(PLAYER_2);

				float seconds = 0.0f;
				while (seconds <= 40.0f && !twoClients.gameDecided()) {
					if (twoClients.isInterrupted()) {
						break;
					}
					Strand.sleep(1000);
					seconds += 1.0f;
				}

				twoClients.assertGameOver();
			} catch (Exception e) {
				getContext().fail(e);
			} finally {
				if (twoClients != null) {
					twoClients.dispose();
				}
			}
		});
	}


	@Test
	public void testPerformGameActionRemotelyInGame(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			CreateGameSessionResponse response = service.createGameSession(new CreateGameSessionRequest()
					.withGameId("gameId1")
					.withPregame1(new PregamePlayerConfiguration(DeckFactory.getRandomDeck(), "testDeck1"))
					.withPregame2(new PregamePlayerConfiguration(DeckFactory.getRandomDeck(), "testDeck2")));

			final GameSession gameSession = service.getGameSession(response.getGameId());
			gameSession.onPlayerConnected(response.getConfigurationForPlayer1().getFirstMessage().getPlayer1(), new TestClient());
			gameSession.onPlayerConnected(response.getConfigurationForPlayer2().getFirstMessage().getPlayer1(), new TestClient());
			final PerformGameActionRequest request = new PerformGameActionRequest();
			SpellCard fireball = (SpellCard) CardCatalogue.getCardById("spell_fireball");
			fireball.setOwner(0);
			fireball.setId(99);
			fireball.moveOrAddTo(gameSession.getGameContext(), Zones.HAND);

			final SpellDesc spell = fireball.getSpell();
			spell.setTarget(gameSession.getGameContext().getPlayer(0).getHero().getReference());
			request.setAction(new PlaySpellCardAction(spell, fireball, TargetSelection.AUTO));
			request.setPlayerId(0);
			request.setGameId(gameSession.getGameId());
			PerformGameActionResponse response1 = service.performGameAction(request);
			getContext().assertEquals(24, response1.getState().player1.getHero().getHp());
		});
	}

	@Suspendable
	private void simultaneousSessions(int sessions) throws SuspendExecution, InterruptedException {
		List<TwoClients> clients = new ArrayList<>();
		for (int i = 0; i < sessions; i++) {
			clients.add(new TwoClients().invoke(this.service));
		}

		clients.forEach(TwoClients::play);
		float seconds = 0.0f;
		while (seconds <= 300.0f && !clients.stream().allMatch(TwoClients::gameDecided)) {
			if (clients.stream().anyMatch(TwoClients::isInterrupted)) {
				break;
			}
			clients.forEach(c -> {
				final long c1 = c.getPlayerContext1().clientDelay();
				final long c2 = c.getPlayerContext2().clientDelay();
				final boolean gd1 = c.getPlayerContext1().updateAndGetGameOver();
				final boolean gd2 = c.getPlayerContext2().updateAndGetGameOver();
				if ((!gd1 && c1 > 20e9) || (!gd2 && c2 > 20e9)) {
					logger.info(String.format("Delayed game %s thread: %s", c.getGameId(), c.getPlayerContext1().isActivePlayer() ? c.getThread1().getName() : c.getThread2().getName()));
				}
			});
			if (clients.stream().anyMatch(c -> c.isTimedOut((long) 40e9))) {
				break;
			}
			try {
				Strand.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			seconds += 1.0f;
		}
		clients.forEach(TwoClients::assertGameOver);
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<GamesImpl>> done) {
		setLoggingLevel(Level.ERROR);
		GamesImpl instance = new GamesImpl();
		matchmaking = new MatchmakingImpl();
		vertx.deployVerticle(matchmaking, then -> {
			vertx.deployVerticle(instance, then2 -> {
				done.handle(Future.succeededFuture(instance));
			});
		});
	}
}
