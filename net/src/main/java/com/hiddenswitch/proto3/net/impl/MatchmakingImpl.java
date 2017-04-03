package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.*;
import com.hiddenswitch.proto3.net.client.models.MatchmakingDeck;
import com.hiddenswitch.proto3.net.common.ClientConnectionConfiguration;
import com.hiddenswitch.proto3.net.impl.server.GameSession;
import com.hiddenswitch.proto3.net.impl.server.PregamePlayerConfiguration;
import net.demilich.metastone.game.decks.DeckWithId;
import com.hiddenswitch.proto3.net.impl.util.Matchmaker;
import com.hiddenswitch.proto3.net.impl.util.QueueEntry;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFactory;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.Map;

public class MatchmakingImpl extends Service<MatchmakingImpl> implements Matchmaking {
	private ServiceProxy<Games> gameSessions;
	private ServiceProxy<Logic> logic;

	private Matchmaker matchmaker = new Matchmaker();
	private Map<String, ClientConnectionConfiguration> connections = new HashMap<>();

	@Override
	public void start() throws SuspendExecution {
		super.start();
		Broker.of(this, Matchmaking.class, vertx.eventBus());
		gameSessions = Broker.proxy(Games.class, vertx.eventBus());
		logic = Broker.proxy(Logic.class, vertx.eventBus());
	}

	@Override
	public void stop() throws Exception {
		super.stop();
	}

	@Override
	public MatchCancelResponse cancel(MatchCancelRequest matchCancelRequest) {
		final String userId = matchCancelRequest.getUserId();
		final Matchmaker.Match match = matchmaker.remove(userId);
		if (match != null) {
			return new MatchCancelResponse(true, match.gameId, match.entry1.userId.equals(userId) ? 0 : 1);
		}
		return new MatchCancelResponse(true, null, -1);
	}

	@Override
	@Suspendable
	public MatchmakingResponse matchmakeAndJoin(MatchmakingRequest matchmakingRequest) throws SuspendExecution, InterruptedException {
		final String userId = matchmakingRequest.getUserId();
		MatchmakingResponse response = new MatchmakingResponse();

		final boolean isRetry = matchmaker.contains(userId);
		final boolean isWaitingTooLong = matchmakingRequest.isAllowBots()
				&& isRetry
				&& matchmaker.get(userId).createdAt + (long) 25e9 < System.nanoTime();

		// TODO: Deal with reconnecting to AI game

		// Setup a user with a game against an AI if they've been waiting more than 10 seconds
		if (isWaitingTooLong
				|| matchmakingRequest.isBotMatch()) {
			QueueEntry entry = matchmaker.get(userId);
			String gameId = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
			String aiUserId = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
			// The player has been waiting too long. Match to an AI.
			final CreateGameSessionRequest request = new CreateGameSessionRequest()
					.withPregame1(new PregamePlayerConfiguration(entry.deck, entry.userId))
					// TODO: Pick a better deck for the AI
					.withPregame2(new PregamePlayerConfiguration(DeckFactory.getRandomDeck(), aiUserId)
							.withAI(true))
					.withGameId(gameId);

			CreateGameSessionResponse createGameSessionResponse = gameSessions.sync().createGameSession(request);

			GameSession session = createGameSessionResponse.toSession();
			final ClientConnectionConfiguration connection = session.getConfigurationForPlayer1();
			connections.put(userId, connection);
			matchmaker.remove(userId);
			response.setConnection(connection);
			return response;
		}

		Deck deck = null;
		if (matchmakingRequest.getDeck() != null) {
			final MatchmakingDeck incomingDeck = matchmakingRequest.getDeck();

			if (incomingDeck != null) {
				deck = new Deck(HeroClass.valueOf(incomingDeck.getHeroClass()));
				final CardCollection cards = deck.getCards();
				incomingDeck.getCards().forEach(cardId -> {
					cards.add(CardCatalogue.getCardById(cardId));
				});
			}
		} else if (matchmakingRequest.getDeckId() != null) {
			deck = new DeckWithId(matchmakingRequest.getDeckId());
		}

		Matchmaker.Match match = matchmaker.match(userId, deck);

		if (match == null) {
			response.setRetry(new MatchmakingRequest(matchmakingRequest).withDeck(null).withDeckId(null).withUserId(userId));
			return response;
		}

		final ContainsGameSessionResponse contains = gameSessions.sync().containsGameSession(new ContainsGameSessionRequest(match.gameId));

		if (!contains.result) {
			// Create a game session.
			Deck deck1 = match.entry1.deck;
			Deck deck2 = match.entry2.deck;

			// To run in alliance mode, both decks need to be references
			if (match.isAllianceMatch()) {
				StartGameResponse startGameResponse = logic.sync().startGame(new StartGameRequest()
						.withGameId(match.gameId)
						.withPlayers(new StartGameRequest.Player()
										.withId(0)
										.withUserId(match.entry1.userId)
										.withDeckId(((DeckWithId) deck1).getDeckId()),
								new StartGameRequest.Player()
										.withId(1)
										.withUserId(match.entry2.userId)
										.withDeckId(((DeckWithId) deck2).getDeckId())));

				deck1 = startGameResponse.getPlayers().get(0).getDeck();
				deck2 = startGameResponse.getPlayers().get(1).getDeck();
			} else if (deck1 instanceof DeckWithId
					|| deck2 instanceof DeckWithId) {
				// We can't run in alliance mode with only one of the decks references by ID.
				throw new RuntimeException();
			}

			final CreateGameSessionRequest request = new CreateGameSessionRequest()
					.withPregame1(new PregamePlayerConfiguration(deck1, match.entry1.userId))
					.withPregame2(new PregamePlayerConfiguration(deck2, match.entry2.userId))
					.withGameId(match.gameId);
			CreateGameSessionResponse createGameSessionResponse = gameSessions.sync().createGameSession(request);
			GameSession session = createGameSessionResponse.toSession();
			connections.put(match.entry1.userId, session.getConfigurationForPlayer1());
			connections.put(match.entry2.userId, session.getConfigurationForPlayer2());
		}

		response.setConnection(connections.get(userId));
		return response;
	}

	@Override
	public CurrentMatchResponse getCurrentMatch(CurrentMatchRequest request) throws SuspendExecution, InterruptedException {
		if (matchmaker.contains(request.getUserId())) {
			return new CurrentMatchResponse(matchmaker.indexedByUserIds().get(request.getUserId()).gameId);
		} else {
			return new CurrentMatchResponse(null);
		}
	}

	@Override
	public MatchExpireResponse expireOrEndMatch(MatchExpireRequest request) throws SuspendExecution, InterruptedException {
		// TODO: Clear out old connections from AI games
		final MatchExpireResponse response = new MatchExpireResponse();
		final Matchmaker.Match match = matchmaker.indexedByGameIds().get(request.gameId);

		if (match == null) {
			response.matchNotFoundOrAlreadyExpired = true;
			return response;
		}

		connections.remove(match.entry1.userId);
		connections.remove(match.entry2.userId);

		response.expired = matchmaker.expire(request.gameId);

		if (match.isAllianceMatch()) {
			// End the game in alliance mode
			logic.sync().endGame(new EndGameRequest()
					.withPlayers(new EndGameRequest.Player()
									.withDeckId(((DeckWithId) match.entry1.deck).getDeckId()),
							new EndGameRequest.Player()
									.withDeckId(((DeckWithId) match.entry2.deck).getDeckId())));
		}

		return response;
	}
}
