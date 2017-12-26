package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.client.models.MatchmakingDeck;
import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;
import com.hiddenswitch.spellsource.impl.server.PregamePlayerConfiguration;
import com.hiddenswitch.spellsource.impl.util.Matchmaker;
import com.hiddenswitch.spellsource.impl.util.QueueEntry;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.ext.mongo.UpdateOptions;
import net.demilich.metastone.game.decks.DeckWithId;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.hiddenswitch.spellsource.util.QuickJson.json;

public class MatchmakingImpl extends AbstractService<MatchmakingImpl> implements Matchmaking {
	private RpcClient<Games> gameSessions;
	private RpcClient<Logic> logic;
	private RpcClient<Bots> bots;

	private Matchmaker matchmaker = Matchmaker.local();
	private Map<String, ClientConnectionConfiguration> connections = new HashMap<>();
	private Registration registration;
	private Set<String> processing = Collections.synchronizedSet(new HashSet<>());
	private Map<GameId, CreateGameSessionResponse> responses;

	@Override
	public void start() throws SuspendExecution {
		super.start();

		responses = Games.getConnections(vertx);
		gameSessions = Rpc.connect(Games.class, vertx.eventBus());
		logic = Rpc.connect(Logic.class, vertx.eventBus());
		bots = Rpc.connect(Bots.class, vertx.eventBus());

		// Check if we already have a matchmaking service online. If so, we shouldn't start.
		if (noInstancesYet()) {
			registration = Rpc.register(this, Matchmaking.class, vertx.eventBus());
		}

	}

	@Override
	public MatchCancelResponse cancel(MatchCancelRequest matchCancelRequest) throws SuspendExecution, InterruptedException {
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

		final CreateGameSessionRequest createGameSessionRequest = new CreateGameSessionRequest()
				.withPregame1(new PregamePlayerConfiguration(deck1, userId1)
						.withAI(request.isBot1())
						.withAttributes(startGameResponse.getPregamePlayerConfiguration1().getAttributes()))
				.withPregame2(new PregamePlayerConfiguration(deck2, userId2)
						.withAI(request.isBot2())
						.withAttributes(startGameResponse.getPregamePlayerConfiguration2().getAttributes()))
				.withGameId(gameId);
		CreateGameSessionResponse createGameSessionResponse = gameSessions.sync().createGameSession(createGameSessionRequest);
		connections.put(userId1, createGameSessionResponse.getConfigurationForPlayer1());
		connections.put(userId2, createGameSessionResponse.getConfigurationForPlayer2());
		return new MatchCreateResponse(createGameSessionResponse);
	}

	@Override
	@Suspendable
	public MatchmakingResponse matchmakeAndJoin(MatchmakingRequest matchmakingRequest) throws SuspendExecution, InterruptedException {
		final String userId = matchmakingRequest.getUserId();
		if (processing.contains(userId)) {
			Strand.sleep(250);
			return new MatchmakingResponse().withRetry(new MatchmakingRequest(matchmakingRequest).withDeck(null).withDeckId(matchmakingRequest.getDeckId()).withUserId(userId));
		}

		processing.add(userId);
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
			processing.remove(userId);
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
			processing.remove(matchmakingRequest.getUserId());
			return response;
		}

		processing.add(match.entry1.userId);
		processing.add(match.entry2.userId);
		final GameId key = new GameId(match.gameId);
		final CreateGameSessionResponse existingGame = responses.get(key);

		if (existingGame == null) {
			// Create a game session.
			MatchCreateResponse createMatchResponse = createMatch(new MatchCreateRequest(match));
			if (createMatchResponse.getCreateGameSessionResponse().isPending()) {
				// Wait 500ms up to 4 times to see if the match successfully deployed
				int i = 0;
				final int retries = 4;
				final int retryDelay = 500;
				for (; i < retries; i++) {
					Strand.sleep(retryDelay);

					if (!responses.get(key).isPending()) {
						break;
					}
				}

				if (i >= retries) {
					throw new NullPointerException("Timed out while waiting for a match to be created for this user.");
				}
			}
		}

		response.setConnection(connections.get(userId));
		processing.remove(match.entry1.userId);
		processing.remove(match.entry2.userId);
		return response;
	}

	@Override
	public CurrentMatchResponse getCurrentMatch(CurrentMatchRequest request) throws SuspendExecution, InterruptedException {
		if (matchmaker.indexedByUserIds().containsKey(request.getUserId())) {
			final String gameId = matchmaker.indexedByUserIds().get(request.getUserId()).gameId;
			return new CurrentMatchResponse(gameId);
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

		final String userId1 = match.entry1.userId;
		final String userId2 = match.entry2.userId;
		connections.remove(userId1);
		connections.remove(userId2);

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

		if (registration != null) {
			Rpc.unregister(registration);
			freeSingleton();
		}

	}
}
