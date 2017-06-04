package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.proto3.net.MatchmakingTest;
import com.hiddenswitch.proto3.net.client.models.MatchmakingDeck;
import com.hiddenswitch.proto3.net.impl.ServiceTest;
import com.hiddenswitch.proto3.net.models.MatchmakingRequest;
import com.hiddenswitch.proto3.net.models.MatchmakingResponse;
import com.hiddenswitch.proto3.net.impl.BotsImpl;
import com.hiddenswitch.proto3.net.impl.GamesImpl;
import com.hiddenswitch.proto3.net.impl.MatchmakingImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFactory;
import net.demilich.metastone.utils.Tuple;

import java.util.stream.Collectors;

import static net.demilich.metastone.game.GameContext.PLAYER_1;
import static net.demilich.metastone.game.GameContext.PLAYER_2;

/**
 * Created by bberman on 12/16/16.
 */
public abstract class AbstractMatchmakingTest extends ServiceTest<MatchmakingImpl> {
	protected GamesImpl gameSessions;
	private Logger logger = LoggerFactory.getLogger(MatchmakingTest.class);
	private BotsImpl bots;

	@Suspendable
	protected String createTwoPlayersAndMatchmake() throws SuspendExecution, InterruptedException {
		logger.info("Starting matchmaking...");
		String userId1 = "player1";
		String userId2 = "player2";

		// Assume player 1's identity
		MatchmakingRequest request1 = new MatchmakingRequest();
		request1.setUserId(userId1);
		final Tuple<MatchmakingDeck, Deck> deckTuple1 = createDeckForMatchmaking(PLAYER_1);
		MatchmakingDeck deck1 = deckTuple1.getFirst();
		request1.setDeck(deck1);
		MatchmakingResponse response1 = null;

		response1 = service.matchmakeAndJoin(request1);
		getContext().assertNotNull(response1.getRetry());
		getContext().assertNull(response1.getConnection());
		getContext().assertNull(response1.getRetry().getDeck());
		logger.info("Matchmaking for player1 entered.");

		// Assume player 2's identity
		MatchmakingRequest request2 = new MatchmakingRequest();
		request2.setUserId(userId2);
		final Tuple<MatchmakingDeck, Deck> deckTuple2 = createDeckForMatchmaking(PLAYER_2);
		MatchmakingDeck deck2 = deckTuple2.getFirst();
		request2.setDeck(deck2);
		MatchmakingResponse response2 = null;

		response2 = service.matchmakeAndJoin(request2);
		getContext().assertNull(response2.getRetry());
		getContext().assertNotNull(response2.getConnection());
		logger.info("Matchmaking for player2 entered.");

		// Assume player 1's identity, poll for matchmaking again and receive the new game information
		request1 = response1.getRetry();

		response1 = service.matchmakeAndJoin(request1);
		getContext().assertNull(response1.getRetry());
		getContext().assertNotNull(response1.getConnection());
		logger.info("Matchmaking for player1 entered, 2nd time.");

		// Now try connecting
		TwoClients twoClients = new TwoClients().invoke(response1, deckTuple1.getSecond(), response2, deckTuple2.getSecond(), response1.getConnection().getFirstMessage().getGameId(), gameSessions);
		twoClients.play();
		float time = 0f;
		while (time < 60f && !twoClients.gameDecided()) {
			Strand.sleep(1000);
			time += 1.0f;
		}
		twoClients.assertGameOver();
		return response1.getConnection().getFirstMessage().getGameId();
	}

	protected Tuple<MatchmakingDeck, Deck> createDeckForMatchmaking(int playerId) {
		Deck gameDeck = DeckFactory.getRandomDeck();
		return getTuple(gameDeck);
	}

	protected static Tuple<MatchmakingDeck, Deck> getTuple(Deck gameDeck) {
		final MatchmakingDeck matchmakingDeck = new MatchmakingDeck()
				.cards(gameDeck.getCards().toList().stream().map(Card::getCardId).collect(Collectors.toList()))
				.heroClass(gameDeck.getHeroClass().toString());
		return new Tuple<>(matchmakingDeck, gameDeck);
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<MatchmakingImpl>> done) {
		logger.info("Deploying services...");
		gameSessions = new GamesImpl();
		bots = new BotsImpl();
		MatchmakingImpl instance = new MatchmakingImpl();
		vertx.deployVerticle(gameSessions, then -> {
			if (then.failed()) {
				throw new AssertionError("failed to deploy game sessions: " + then.cause().getMessage());
			}
			vertx.deployVerticle(bots, then2 -> {
				vertx.deployVerticle(instance, then3 -> {
					logger.info("Services deployed.");
					done.handle(new Result<>(instance));
				});
			});
		});
	}
}
