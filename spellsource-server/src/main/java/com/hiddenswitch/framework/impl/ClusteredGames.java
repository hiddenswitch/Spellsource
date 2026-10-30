package com.hiddenswitch.framework.impl;

import com.google.common.collect.Sets;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import com.hiddenswitch.spellsource.rpc.Spellsource.ClientToServerMessage;
import com.hiddenswitch.spellsource.rpc.Spellsource.ServerToClientMessage;
import io.github.jklingsporn.vertx.jooq.shared.postgres.JSONToJsonObjectConverter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.decks.CollectionDeck;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.logic.GameStatus;
import net.demilich.metastone.game.logic.Trace;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.hiddenswitch.framework.schema.keycloak.Keycloak.KEYCLOAK;
import static com.hiddenswitch.framework.schema.spellsource.Tables.GAMES;
import static com.hiddenswitch.framework.schema.spellsource.Tables.GAME_USERS;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.vertx.await.Async.await;
import static io.vertx.await.Async.join;
import static io.vertx.await.Async.openScope;
import static io.vertx.await.Async.runAll;

public class ClusteredGames extends AbstractVirtualThreadVerticle {
	private static final Counter GAMES_CREATED = Counter.builder("games.created")
			.description("The number of games created.")
			.baseUnit(BaseUnits.EVENTS)
			.register(globalRegistry);
	private static final Logger LOGGER = LoggerFactory.getLogger(ClusteredGames.class);
	private final Map<String, ServerGameContext> contexts = new ConcurrentHashMap<>();
	private SqlCachedCardCatalogue cardCatalogue;
	private MessageConsumer<?> registration;
	private MessageConsumer<?> restoreRegistration;

	@Override
	public void startVirtual() throws Exception {
		CodecRegistration.register(ServerToClientMessage.getDefaultInstance())
				.andRegister(ClientToServerMessage.getDefaultInstance())
				.andRegister(CreateGameSessionResponse.class)
				.andRegister(ConfigurationRequest.class);
		var eb = Vertx.currentContext().owner().eventBus();
		registration = eb.<ConfigurationRequest>consumer(Games.GAMES_CREATE_GAME_SESSION,
				request -> request.reply(createGameSession(request.body())));
		restoreRegistration = eb.<String>consumer(Games.GAMES_RESTORE_GAME,
				request -> request.reply(restoreGame(request.body())));

		this.cardCatalogue = SqlCachedCardCatalogue.forVertx();
		this.cardCatalogue.ensureInitialized();

		runAll(
				() -> await(registration.completion()),
				() -> await(restoreRegistration.completion())
		);
	}

	public void registerContext(@NotNull String gameId, @NotNull ServerGameContext context) {
		contexts.put(gameId, context);
	}

	public int getHostedGameCount() {
		return contexts.size();
	}

	public CreateGameSessionResponse createGameSession(ConfigurationRequest request) {
		LOGGER.trace("createGameSession: creating game session for request {}", request);
		Objects.requireNonNull(request.getGameId(), "Cannot create a game session without specifying a gameId.");

		var resumable = request.getConfigurations().stream().filter(Configuration::isBot).count() == 1
				&& request.getConfigurations().size() == 2;
		var gameIdLong = Long.parseLong(request.getGameId());
		try (var scope = openScope(vertx)) {
			for (var configuration : request.getConfigurations()) {
				if (resumable && !configuration.isBot()) {
					configuration.setNoActivityTimeout(0L);
				}
				var playerAttributes = new AttributeMap();
				var deckId = configuration.getDeck().getDeckId();
				var userId = configuration.getUserId();
				playerAttributes.put(Attribute.USER_ID, userId);
				playerAttributes.put(Attribute.DECK_ID, deckId);
				configuration.setPlayerAttributes(playerAttributes);

				if (configuration.getDeck() instanceof CollectionDeck) {
					scope.fork(() -> {
						var deckCollection = await(Legacy.getDeck(cardCatalogue, deckId, userId));
						configuration.setDeck(ModelConversions.getGameDeck(userId, deckCollection, cardCatalogue));
						deckCollection.getCollection().getPlayerEntityAttributesList().forEach(t -> playerAttributes.put(Attribute.valueOf(t.getAttribute().name()), t.getStringValue()));
						return null;
					});
				}

				scope.fork(() -> {
					var usernameRow = await(Environment.withExecutor(queryExecutor -> queryExecutor.findOneRow(dsl -> dsl
							.select(KEYCLOAK.USER_ENTITY.USERNAME)
							.from(KEYCLOAK.USER_ENTITY)
							.where(KEYCLOAK.USER_ENTITY.ID.eq(userId)))));
					var username = usernameRow.getString(0);
					configuration.setName(username);
					playerAttributes.put(Attribute.NAME, username);
					return null;
				});

				scope.fork(() -> await(RogueManager.handleGameStart(deckId, gameIdLong)));
			}
			join(scope);
		}

		var serverGameContext = new ServerGameContext(
				request.getGameId(),
				request.getConfigurations(),
				cardCatalogue,
				this);
		await(vertx.deployVerticle(serverGameContext, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)));
		GAMES_CREATED.increment();
		return CreateGameSessionResponse.session(deploymentID(), serverGameContext);
	}

	/** Finds and reconstructs the latest checkpointed one-human/one-bot game for a returning player. */
	private String restoreGame(String userId) {
		var rows = await(Environment.query(dsl -> dsl.select(GAMES.ID, GAMES.TRACE, GAME_USERS.PLAYER_INDEX, GAME_USERS.DECK_ID)
				.from(GAMES).join(GAME_USERS).on(GAME_USERS.GAME_ID.eq(GAMES.ID))
				.where(GAME_USERS.USER_ID.eq(userId))
				.and(GAMES.STATUS.eq(GameStateEnum.STARTED))
				.and(GAMES.TRACE.isNotNull())
				.and(GAME_USERS.VICTORY_STATUS.eq(com.hiddenswitch.framework.schema.spellsource.enums.GameUserVictoryEnum.UNKNOWN))
				.and(GAME_USERS.GAME_ID.in(dsl.select(GAME_USERS.GAME_ID).from(GAME_USERS)
						.groupBy(GAME_USERS.GAME_ID).having(org.jooq.impl.DSL.count().eq(1))))
				.orderBy(GAMES.CREATED_AT.desc()).limit(1)));
		var iterator = rows.iterator();
		if (!iterator.hasNext()) {
			return null;
		}
		var row = iterator.next();
		var id = row.getLong("id");
		try {
			var trace = Objects.requireNonNull(Trace.load(row.getJsonObject("trace").encode()));
			Objects.requireNonNull(trace.getMulligans(), "trace has no completed mulligans");
			return createResumedSession(id.toString(), userId, row.getShort("player_index"), row.getString("deck_id"), trace).getGameId();
		} catch (Throwable cause) {
			LOGGER.warn("Cannot restore persisted game {} for {}; abandoning it", id, userId, cause);
			abandonUnrestorableGame(id, userId);
			return null;
		}
	}

	private void abandonUnrestorableGame(long gameId, String userId) {
		runAll(
				() -> await(Environment.withDslContext(dsl -> dsl.update(GAME_USERS)
						.set(GAME_USERS.VICTORY_STATUS, com.hiddenswitch.framework.schema.spellsource.enums.GameUserVictoryEnum.LOST)
						.where(GAME_USERS.GAME_ID.eq(gameId)).and(GAME_USERS.USER_ID.eq(userId)))),
				() -> await(Environment.withDslContext(dsl -> dsl.update(GAMES)
						.set(GAMES.STATUS, com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum.FINISHED)
						.where(GAMES.ID.eq(gameId)))),
				() -> await(RogueManager.handleGameEnd(gameId, "abandoned-bot-" + gameId))
		);
	}

	private CreateGameSessionResponse createResumedSession(String gameId, String userId, short humanIndex, String humanDeckId, Trace trace) {
		var configurations = new ArrayList<Configuration>();
		for (var index = 0; index < 2; index++) {
			var deck = new GameDeck(cardCatalogue, trace.getHeroClasses().get(index), trace.getDeckCardIds().get(index).getCardIds());
			if (index == humanIndex) deck.setDeckId(humanDeckId);
			configurations.add(new Configuration().setPlayerId(index)
					.setUserId(index == humanIndex ? userId : "resumed-bot-" + gameId)
					.setBot(index != humanIndex).setNoActivityTimeout(index == humanIndex ? 0L : Games.getDefaultNoActivityTimeout())
					.setDeck(deck));
		}
		var serverGameContext = new ServerGameContext(gameId, configurations, cardCatalogue, this);
		serverGameContext.restoreFromTrace(trace);
		await(vertx.deployVerticle(serverGameContext, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)));
		return CreateGameSessionResponse.session(deploymentID(), serverGameContext);
	}

	public void removeGameAndRecordReplay(@NotNull String gameId) {
		Objects.requireNonNull(gameId);

		var gameIdLong = Long.parseLong(gameId);
		var gameContext = contexts.remove(gameId);
		if (gameContext == null) {
			return;
		}

		gameContext.updateAndGetGameOver();
		String winner = null;
		if (gameContext.getWinner() != null && gameContext.getWinner().getUserId() != null) {
			winner = gameContext.getWinner().getUserId();
		}
		if (winner != null) {
			var userIdLoser = gameContext.getOpponent(gameContext.getWinner()).getUserId();
			var pTrace = JSONToJsonObjectConverter.getInstance().to(gameContext.getTrace().toJson());
			await(Environment.callRoutine(Routines.clusteredGamesUpdateGameAndUsers(winner, userIdLoser, gameIdLong, pTrace)).execute());
		} else {
			await(Environment.withDslContext(dsl -> dsl.deleteFrom(GAMES).where(GAMES.ID.eq(gameIdLong))).otherwiseEmpty());
		}
	}

	@Override
	public void stopVirtual() {
		var keys = Sets.newCopyOnWriteArraySet(contexts.keySet());
		LOGGER.trace("stop: Stopping the ClusteredGames, hosting contexts: {}", keys.stream().map(String::toString).reduce((s1, s2) -> s1 + ", " + s2).orElseGet(() -> "none"));

		runAll(
				() -> await(registration.unregister().otherwiseEmpty()),
				() -> await(restoreRegistration.unregister().otherwiseEmpty())
		);
		for (var entry : new ArrayList<>(contexts.entrySet())) {
			var gameId = entry.getKey();
			var context = entry.getValue();
			if (context.getPlayerConfigurations().stream().filter(Configuration::isBot).count() == 1) {
				context.checkpoint();
				contexts.remove(gameId);
			} else {
				removeGameAndRecordReplay(gameId);
			}
			var thread = context.getThread();
			if (thread != null) {
				thread.interrupt();
			}
			context.close();
		}
		LOGGER.trace("stop: Unregistered");
	}
}
