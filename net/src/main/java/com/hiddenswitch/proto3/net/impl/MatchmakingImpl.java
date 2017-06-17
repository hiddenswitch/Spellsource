package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.*;
import com.hiddenswitch.proto3.net.client.models.MatchmakingDeck;
import com.hiddenswitch.proto3.net.common.ClientConnectionConfiguration;
import com.hiddenswitch.proto3.net.impl.server.PregamePlayerConfiguration;
import com.hiddenswitch.proto3.net.util.Registration;
import net.demilich.metastone.game.decks.DeckWithId;
import com.hiddenswitch.proto3.net.impl.util.Matchmaker;
import com.hiddenswitch.proto3.net.impl.util.QueueEntry;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.RPC;
import com.hiddenswitch.proto3.net.util.RpcClient;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.HashMap;
import java.util.Map;

public class MatchmakingImpl extends AbstractService<MatchmakingImpl> implements Matchmaking {
	private RpcClient<Games> gameSessions;
	private RpcClient<Logic> logic;
	private RpcClient<Bots> bots;

	private Matchmaker matchmaker = Matchmaker.local();
	private Map<String, ClientConnectionConfiguration> connections = new HashMap<>();
	private Registration registration;

	@Override
	public void start() throws SuspendExecution {
		super.start();
		gameSessions = RPC.connect(Games.class, vertx.eventBus());
		logic = RPC.connect(Logic.class, vertx.eventBus());
		bots = RPC.connect(Bots.class, vertx.eventBus());
		registration = RPC.register(this, Matchmaking.class, vertx.eventBus());
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
	public MatchCreateResponse createMatch(MatchCreateRequest request) throws SuspendExecution, InterruptedException {
		final String deckId1 = request.getDeckId1();
		final String deckId2 = request.getDeckId2();
		final String userId1 = request.getUserId1();
		final String userId2 = request.getUserId2();
		final String gameId = request.getGameId();

		StartGameResponse startGameResponse = logic.sync().startGame(new StartGameRequest()
				.withGameId(gameId)
				.withPlayers(new StartGameRequest.Player()
								.withId(0)
								.withUserId(userId1)
								.withDeckId(deckId1),
						new StartGameRequest.Player()
								.withId(1)
								.withUserId(userId2)
								.withDeckId(deckId2)));

		final Deck deck1 = startGameResponse.getPlayers().get(0).getDeck();
		final Deck deck2 = startGameResponse.getPlayers().get(1).getDeck();

		final CreateGameSessionRequest request2 = new CreateGameSessionRequest()
				.withPregame1(new PregamePlayerConfiguration(deck1, userId1)
						.withAI(request.isBot1())
						.withAttributes(startGameResponse.getPregamePlayerConfiguration1().getAttributes()))
				.withPregame2(new PregamePlayerConfiguration(deck2, userId2)
						.withAI(request.isBot2())
						.withAttributes(startGameResponse.getPregamePlayerConfiguration2().getAttributes()))
				.withGameId(gameId);
		CreateGameSessionResponse createGameSessionResponse = gameSessions.sync().createGameSession(request2);
		connections.put(userId1, createGameSessionResponse.getConfigurationForPlayer1());
		connections.put(userId2, createGameSessionResponse.getConfigurationForPlayer2());
		return new MatchCreateResponse(createGameSessionResponse);
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
			final BotsStartGameRequest request;
			if (entry != null) {
				matchmaker.remove(entry);
				request = new BotsStartGameRequest(entry.userId, ((DeckWithId) entry.deck).getDeckId());
			} else {
				request = new BotsStartGameRequest(matchmakingRequest.getUserId(), matchmakingRequest.getDeckId());
			}

			BotsStartGameResponse response1 = bots.sync().startGame(request);
			matchmaker.match(response1.getGameId(), matchmakingRequest.getUserId(), response1.getBotUserId(), matchmakingRequest.getDeckId(), response1.getBotDeckId());
			final ClientConnectionConfiguration connection = response1.getPlayerConnection();
			connections.put(userId, connection);
			response.setConnection(connection);
			return response;
		}

		Deck deck = null;
		if (matchmakingRequest.getDeck() != null) {
			final MatchmakingDeck incomingDeck = matchmakingRequest.getDeck();

			if (incomingDeck != null) {
				deck = new Deck(HeroClass.valueOf(incomingDeck.getHeroClass()));
				final CardList cards = deck.getCards();
				incomingDeck.getCards().forEach(cardId -> {
					cards.addCard(CardCatalogue.getCardById(cardId));
				});
			}
		} else if (matchmakingRequest.getDeckId() != null) {
			deck = new DeckWithId(matchmakingRequest.getDeckId());
		}

		Matchmaker.Match match = matchmaker.match(userId, deck);

		if (match == null) {
			response.setRetry(new MatchmakingRequest(matchmakingRequest).withDeck(null).withDeckId(matchmakingRequest.getDeckId()).withUserId(userId));
			return response;
		}

		final ContainsGameSessionResponse contains = gameSessions.sync().containsGameSession(new ContainsGameSessionRequest(match.gameId));

		if (!contains.result) {
			// Create a game session.
			Deck deck1 = match.entry1.deck;
			Deck deck2 = match.entry2.deck;

			// To run in alliance mode, both decks need to be references
			if (match.isAllianceMatch()) {
				createMatch(new MatchCreateRequest(match));
			} else {
				legacyCreateGame(match, deck1, deck2);
			}
		}

		response.setConnection(connections.get(userId));
		return response;
	}

	private void legacyCreateGame(Matchmaker.Match match, Deck deck1, Deck deck2) throws SuspendExecution, InterruptedException {
		final CreateGameSessionRequest request = new CreateGameSessionRequest()
				.withPregame1(new PregamePlayerConfiguration(deck1, match.entry1.userId))
				.withPregame2(new PregamePlayerConfiguration(deck2, match.entry2.userId))
				.withGameId(match.gameId);
		CreateGameSessionResponse createGameSessionResponse = gameSessions.sync().createGameSession(request);
		connections.put(match.entry1.userId, createGameSessionResponse.getConfigurationForPlayer1());
		connections.put(match.entry2.userId, createGameSessionResponse.getConfigurationForPlayer2());
	}

	@Override
	public CurrentMatchResponse getCurrentMatch(CurrentMatchRequest request) throws SuspendExecution, InterruptedException {
		if (matchmaker.indexedByUserIds().containsKey(request.getUserId())) {
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

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		RPC.unregister(registration);
	}
}
