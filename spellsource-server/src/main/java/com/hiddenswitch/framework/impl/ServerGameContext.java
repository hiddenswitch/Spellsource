package com.hiddenswitch.framework.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.spellsource.Tables;
import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.spellsource.common.GameState;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.opentracing.log.Fields;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.*;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.events.TouchingNotification;
import net.demilich.metastone.game.events.TriggerFired;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.TurnState;
import net.demilich.metastone.game.logic.Trace;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.hiddenswitch.framework.Environment.*;
import static com.hiddenswitch.framework.Games.ADDRESS_CONCEDE_GAME;
import static com.hiddenswitch.framework.Games.ADDRESS_IS_IN_GAME;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.vertx.await.Async.await;
import static io.vertx.core.Future.join;
import static java.util.stream.Collectors.toList;

/**
 * A networked game context from the server's point of view, deployed directly as a virtual-thread {@link Verticle}.
 * <p>
 * In addition to storing game state, this class deploys and coordinates {@link UnityClientBehaviour} player verticles
 * that receive game state updates and respond to {@link Behaviour#requestAction(GameContext, Player, List)} and
 * {@link Behaviour#mulligan(GameContext, Player, List)} over the EventBus.
 */
public class ServerGameContext extends GameContext implements Server, Verticle {
	public static final String WRITER_ADDRESS_PREFIX = Games.WRITER_ADDRESS_PREFIX;
	public static final String READER_ADDRESS_PREFIX = Games.READER_ADDRESS_PREFIX;
	private static final Counter GAMES_FINISHED = Counter.builder("games.finished")
			.description("The number of games finished.")
			.baseUnit(BaseUnits.EVENTS)
			.register(globalRegistry);
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerGameContext.class);
	private final transient Lock lock = new ReentrantLock();
	private final transient Map<Integer, Promise<Client>> clientsReady = new ConcurrentHashMap<>();
	private final transient List<Client> clients = new CopyOnWriteArrayList<>();
	private final List<MessageConsumer<?>> gameLifecycleConsumers = new CopyOnWriteArrayList<>();
	private final List<Future<String>> userVerticleDeployments = new CopyOnWriteArrayList<>();
	private final ClusteredGames cluster;
	private final List<Configuration> playerConfigurations = new ArrayList<>();
	private final String gameId;
	private final AtomicInteger notificationCounter = new AtomicInteger(0);
	private final AtomicBoolean closed = new AtomicBoolean();
	private final AtomicBoolean endGameCalled = new AtomicBoolean();
	private Vertx vertx;
	private Context context;
	private transient Long turnTimerId;
	private boolean isRunning = false;
	private Long timerStartTimeMillis;
	private Long timerLengthMillis;
	private boolean interrupted;
	private boolean restored;
	private volatile boolean gameReady;

	public ServerGameContext(@NotNull String gameId,
	                         @NotNull List<Configuration> playerConfigurations,
	                         CardCatalogue cardCatalogue,
	                         ClusteredGames cluster) {
		super();
		this.cluster = cluster;
		if (cardCatalogue != null) {
			setCardCatalogue(cardCatalogue);
		}
		var tracer = GlobalTracer.get();
		var spanBuilder = tracer.buildSpan("ServerGameContext/init")
				.withTag("gameId", gameId);
		for (var i = 0; i < playerConfigurations.size(); i++) {
			spanBuilder.withTag("playerConfigurations." + i + ".userId", playerConfigurations.get(i).getUserId());
			spanBuilder.withTag("playerConfigurations." + i + ".deckId", playerConfigurations.get(i).getDeck().getDeckId());
			spanBuilder.withTag("playerConfigurations." + i + ".isBot", playerConfigurations.get(i).isBot());
		}
		var span = spanBuilder.start();
		try (var s1 = tracer.activateSpan(span)) {
			if (playerConfigurations.size() != 2) {
				throw new IllegalArgumentException("playerConfigurations.size must be 2");
			}

			this.gameId = gameId;
			this.playerConfigurations.addAll(playerConfigurations);

			setDeckFormat(getCardCatalogue().getSmallestSupersetFormat(playerConfigurations
					.stream()
					.map(Configuration::getDeck)
					.map(deck -> (GameDeck) deck)
					.collect(toList())));

			for (var configuration : getPlayerConfigurations()) {
				var userId = configuration.getUserId();
				var deck = (GameDeck) configuration.getDeck().clone();
				var player = Player.forUser(userId, configuration.getPlayerId(), deck);
				setPlayer(configuration.getPlayerId(), player);
				for (var kv : configuration.getPlayerAttributes().entrySet()) {
					player.getAttributes().put(kv.getKey(), kv.getValue());
				}

				if (configuration.isBot()) {
					player.getAttributes().put(Attribute.AI_OPPONENT, true);
					var behaviour = new GameStateValueBehaviour() {
						@Override
						public @Nullable GameAction requestAction(@NotNull GameContext gameContext, @NotNull Player botPlayer, @NotNull List<GameAction> validActions) {
							Future<GameAction> nextAction = context.executeBlocking(() -> super.requestAction(gameContext, botPlayer, validActions), false);
							return await(nextAction);
						}
					};
					behaviour.setParallel(false)
							.setMaxDepth(2)
							.setLethalTimeout(15000L)
							.setTimeout(320L)
							.setThrowsExceptions(false)
							.setThrowOnInvalidPlan(false);
					setBehaviour(configuration.getPlayerId(), behaviour);
				} else {
					var client = new UnityClientBehaviour(
							this,
							userId,
							configuration.getPlayerId(),
							configuration.getNoActivityTimeout());
					setBehaviour(configuration.getPlayerId(), client);
					clients.add(client);
					clientsReady.put(configuration.getPlayerId(), Promise.promise());
				}
			}
		} finally {
			span.finish();
		}
	}

	@Override
	public Vertx getVertx() {
		return vertx;
	}

	@Override
	public void init(Vertx vertx, Context context) {
		this.vertx = vertx;
		this.context = context;
	}

	public String deploymentID() {
		return context != null ? context.deploymentID() : null;
	}

	@Override
	public final Future<?> deploy(Context context) {
		init(context.owner(), context);
		Promise<Void> startPromise = Promise.promise();
		start(startPromise);
		return startPromise.future();
	}

	@Override
	public final Future<?> undeploy(Context context) {
		Promise<Void> stopPromise = Promise.promise();
		stop(stopPromise);
		return stopPromise.future();
	}

	@Override
	public void start(Promise<Void> startPromise) {
		var startingContext = Vertx.currentContext();
		context.runOnContext(v -> {
			try {
				startVirtual();
				startingContext.runOnContext(v2 -> startPromise.complete());
			} catch (Throwable t) {
				startingContext.runOnContext(v2 -> startPromise.fail(t));
			}
		});
	}

	@Override
	public void stop(Promise<Void> stopPromise) {
		var stoppingContext = Vertx.currentContext();
		context.runOnContext(v -> {
			try {
				stopVirtual();
				stoppingContext.runOnContext(v2 -> stopPromise.complete());
			} catch (Throwable t) {
				stoppingContext.runOnContext(v2 -> stopPromise.fail(t));
			}
		});
	}

	public void startVirtual() {
		var readinessFutures = new ArrayList<Future<?>>();
		for (var configuration : playerConfigurations) {
			var userId = configuration.getUserId();
			var inGameConsumer = registerInGame(gameId, userId);
			gameLifecycleConsumers.add(inGameConsumer);
			readinessFutures.add(inGameConsumer.completion());

			if (!configuration.isBot()) {
				var concedeConsumer = vertx.eventBus()
						.<String>consumer(ADDRESS_CONCEDE_GAME + userId, request -> {
							try {
								concede(configuration.getPlayerId());
								request.reply(true);
							} catch (Throwable cause) {
								request.fail(-1, cause.getMessage());
							}
						});
				gameLifecycleConsumers.add(concedeConsumer);
				readinessFutures.add(concedeConsumer.completion());
			}
		}
		for (var client : clients) {
			var deployFut = vertx.deployVerticle(client, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD));
			userVerticleDeployments.add(deployFut);
			readinessFutures.add(deployFut);
		}
		await(Future.all(readinessFutures));
		if (cluster != null) {
			cluster.registerContext(gameId, this);
		}
		play(true);
	}

	public void stopVirtual() {
		var thread = getThread();
		if (thread != null && thread.isAlive() && !thread.isInterrupted()) {
			thread.interrupt();
		}
	}

	static @NotNull MessageConsumer<String> registerInGame(@NotNull String thisGameId, @NotNull String userId) {
		var eb = Vertx.currentContext().owner().eventBus();
		return eb.consumer(ADDRESS_IS_IN_GAME + userId, (Message<String> req) -> req.reply(thisGameId));
	}

	@Override
	public void init() {
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("ServerGameContext/init")
				.asChildOf(tracer.activeSpan())
				.start();
		var scope = tracer.activateSpan(span);
		try {
			LOGGER.trace("init {}: Game starts {} {} vs {} {}", getGameId(), getPlayer1().getName(), getPlayer1().getUserId(), getPlayer2().getName(), getPlayer2().getUserId());
			startTrace();
			var startingPlayerId = getLogic().determineBeginner(PLAYER_1, PLAYER_2);
			getEnvironment().put(Environment.STARTING_PLAYER, startingPlayerId);
			setActivePlayerId(startingPlayerId);

			getLogic().initializePlayerAndMoveMulliganToSetAside(PLAYER_1, startingPlayerId == PLAYER_1);
			getLogic().initializePlayerAndMoveMulliganToSetAside(PLAYER_2, startingPlayerId == PLAYER_2);

			Future<?> bothClientsReady;
			var timeout = Math.min(Games.getDefaultNoActivityTimeout(), Games.getDefaultConnectionTime());
			if (!clientsReady.values().stream().allMatch(fut -> fut.future().isComplete())) {
				try {
					bothClientsReady = Future.all(clientsReady.values().stream().map(Promise::future).collect(toList()));
					var res = await(Future.any(sleep(timeout), bothClientsReady));
					if (res.isComplete(0)) {
						bothClientsReady = Future.failedFuture(new TimeoutException("timed out waiting for user"));
					} else if (!res.isComplete(1)) {
						bothClientsReady = Future.failedFuture(bothClientsReady.cause());
					}
				} catch (Throwable t) {
					if (Throwables.getRootCause(t) instanceof InterruptedException) {
						throw t;
					}
					bothClientsReady = Future.failedFuture(t);
				}
			} else {
				bothClientsReady = Future.succeededFuture();
			}

			if (bothClientsReady.failed()) {
				for (var entry : clientsReady.entrySet()) {
					if (!entry.getValue().future().isComplete()) {
						LOGGER.error("init {}: Game prematurely ended because {} (player id={} did not connect in {}ms)", bothClientsReady.cause(), getGameId(), entry.getKey(), timeout);
						getLogic().concede(entry.getKey());
					}
				}
				isRunning = false;
				return;
			}

			await(withDslContext(dsl -> dsl.update(Tables.GAMES)
					.set(Tables.GAMES.STATUS, GameStateEnum.STARTED)
					.where(Tables.GAMES.ID.eq(Long.valueOf(gameId)))));

			clientsReady.clear();
			getLogic().contextReady();

			for (var client : getClients()) {
				client.onConnectionStarted(getActivePlayer());
			}

			getPlayers().forEach(p -> p.getAttributes().put(Attribute.GAME_START_TIME_MILLIS, (int) (System.currentTimeMillis() % Integer.MAX_VALUE)));

			final Long mulliganTimerId;
			if (getBehaviours().stream().allMatch(Behaviour::isHuman)) {
				timerLengthMillis = getLogic().getMulliganTimeMillis();
				timerStartTimeMillis = System.currentTimeMillis();
				mulliganTimerId = vertx.setTimer(timerLengthMillis, this::endMulligans);
			} else {
				LOGGER.trace("init {}: No mulligan timer set for game because all players are not human", getGameId());
				timerLengthMillis = null;
				timerStartTimeMillis = null;
				mulliganTimerId = null;
			}

			updateClientsWithGameState();
			gameReady = true;

			Promise<List<Card>> mulligansActive = Promise.promise();
			Promise<List<Card>> mulligansNonActive = Promise.promise();

			var firstHandActive = getActivePlayer().getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
			var firstHandNonActive = getNonActivePlayer().getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
			if (firstHandActive.isEmpty()) {
				mulligansActive.complete(Collections.emptyList());
			} else {
				getBehaviours().get(getActivePlayerId()).mulliganAsync(this, getActivePlayer(), firstHandActive, res -> {
					lock.lock();
					try {
						getActivePlayer().setAttribute(Attribute.GAME_STARTED);
						updateClientWithGameState(getActivePlayerId());
					} finally {
						lock.unlock();
					}
					mulligansActive.complete(res);
				});
			}

			if (firstHandNonActive.isEmpty()) {
				mulligansNonActive.complete(Collections.emptyList());
			} else {
				getBehaviours().get(getNonActivePlayerId()).mulliganAsync(this, getNonActivePlayer(), firstHandNonActive, res -> {
					lock.lock();
					try {
						getNonActivePlayer().setAttribute(Attribute.GAME_STARTED);
						updateClientWithGameState(getNonActivePlayerId());
					} finally {
						lock.unlock();
					}
					mulligansNonActive.complete(res);
				});
			}

			var simultaneousMulligans = await(join(mulligansActive.future(), mulligansNonActive.future()));

			if (mulliganTimerId != null) {
				vertx.cancelTimer(mulliganTimerId);
			}

			if (simultaneousMulligans == null || simultaneousMulligans.failed()) {
				throw new VertxException(simultaneousMulligans == null ? new TimeoutException() : simultaneousMulligans.cause());
			}

			var discardedCardsActive = mulligansActive.future().result();
			var discardedCardsNonActive = mulligansNonActive.future().result();
			getLogic().handleMulligan(getActivePlayer(), true, discardedCardsActive);
			getLogic().handleMulligan(getNonActivePlayer(), false, discardedCardsNonActive);

			traceMulligans(discardedCardsActive, discardedCardsNonActive);
			checkpoint();

			try {
				startGame();
			} catch (NullPointerException | IndexOutOfBoundsException playerNull) {
				Tracing.error(playerNull);
			}
		} finally {
			span.finish();
			scope.close();
		}
	}

	public void play(boolean fork) {
		isRunning = true;
		if (fork) {
			if (getThread() != null) {
				throw new UnsupportedOperationException("Cannot play with a fork twice!");
			}
			context.runOnContext(v -> {
				var thread = Thread.currentThread();
				setThread(thread);
				if (!thread.isVirtual()) {
					throw new IllegalStateException("should be running on virtual thread loop context");
				}

				thread.setUncaughtExceptionHandler((t, e) -> {
					if (context.exceptionHandler() != null) {
						context.exceptionHandler().handle(e);
					}
				});

				thread.setName(String.format("ServerGameContext/play{gameId=%s}", getGameId()));

				var tracer = GlobalTracer.get();
				var span = tracer.buildSpan("ServerGameContext/play")
						.asChildOf(getSpanContext())
						.start();
				var scope = tracer.activateSpan(span);
				var carrier = new BinaryCarrier();
				tracer.inject(span.context(), Format.Builtin.BINARY, carrier);
				this.interrupted = false;
				try {
					LOGGER.trace("play: Starting forked game");
					if (restored) {
						var timeout = Math.min(Games.getDefaultNoActivityTimeout(), Games.getDefaultConnectionTime());
						if (!clientsReady.values().stream().allMatch(fut -> fut.future().isComplete())) {
							var allReady = Future.all(clientsReady.values().stream().map(Promise::future).toList());
							var res = await(Future.any(sleep(timeout), allReady));
							if (res.isComplete(0)) {
								for (var entry : clientsReady.entrySet()) {
									if (!entry.getValue().future().isComplete()) {
										getLogic().concede(entry.getKey());
									}
								}
								isRunning = false;
							}
						}
						clientsReady.clear();
						gameReady = true;
						resume();
					} else {
						super.play(false);
					}
				} catch (Throwable throwable) {
					var rootCause = Throwables.getRootCause(throwable);
					if (rootCause instanceof InterruptedException) {
						interrupted = true;
						span.log(ImmutableMap.of(
								Fields.EVENT, "interrupt",
								"graceful", true
						));
					} else {
						getTrace().setTraceErrors(true);
						LOGGER.warn("play error", throwable);
						try {
							endGame();
						} catch (Throwable endGameError) {
							Tracing.error(endGameError, span, false);
						}
						Tracing.error(throwable);
					}
				} finally {
					interrupted = this.interrupted || Thread.interrupted();
					close();
					if (interrupted) {
						Thread.currentThread().interrupt();
					}
					span.setTag("trace", getTrace().dump());
					span.finish();
					scope.close();
				}
			});
		} else {
			super.play();
		}
	}

	@Override
	public void startTurn(int playerId) {
		lock.lock();
		try {
			for (var client : getClients()) {
				client.setElapsed(false);
			}

			if (turnTimerId != null) {
				if (vertx.cancelTimer(turnTimerId)) {
					turnTimerId = null;
				} else {
					LOGGER.error("unexpectedly could not cancel turn timer");
				}
			}

			if (getBehaviours().stream().allMatch(Behaviour::isHuman)) {
				var client = getClient(playerId);
				Objects.requireNonNull(client, "client unexpectedly null");

				timerLengthMillis = (long) getTurnTimeForPlayer(getActivePlayerId());
				timerStartTimeMillis = System.currentTimeMillis();

				if (timerLengthMillis > 0) {
					turnTimerId = vertx.setTimer(timerLengthMillis, v -> client.elapseAwaitingRequests());
				}
			} else {
				timerLengthMillis = null;
				timerStartTimeMillis = null;
			}

			super.startTurn(playerId);
			var state = new GameState(this, TurnState.TURN_IN_PROGRESS, false);
			for (var updateClient : getClients()) {
				updateClient.onUpdate(state);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void endTurn() {
		lock.lock();
		try {
			if (turnTimerId != null) {
				vertx.cancelTimer(turnTimerId);
				turnTimerId = null;
			}
			super.endTurn();
		} finally {
			lock.unlock();
		}
	}

	private void endMulligans(long ignored) {
		LOGGER.trace("did end mulligans early");
		for (var client : getClients()) {
			client.elapseAwaitingRequests();
		}
	}

	@Override
	public void resume() {
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("ServerGameContext/resume")
				.asChildOf(tracer.activeSpan())
				.start();
		var scope = tracer.activateSpan(span);
		try {
			currentContext.set(this);
			if (!isRunning()) {
				endGame();
			}
			var resumeInProgressTurn = restored && getTurnState() == TurnState.TURN_IN_PROGRESS;
			while (!updateAndGetGameOver()) {
				span.log(ImmutableMap.of("turn", getTurn(), "activePlayerId", getActivePlayerId()));
				if (!isRunning()) {
					break;
				}
				if (resumeInProgressTurn) {
					resumeInProgressTurn = false;
					lock.lock();
					try {
						for (var client : getClients()) {
							client.onConnectionStarted(getActivePlayer());
						}
						if (getBehaviours().stream().allMatch(Behaviour::isHuman)) {
							var client = getClient(getActivePlayerId());
							if (client != null) {
								timerLengthMillis = (long) getTurnTimeForPlayer(getActivePlayerId());
								timerStartTimeMillis = System.currentTimeMillis();
								if (timerLengthMillis > 0) {
									turnTimerId = vertx.setTimer(timerLengthMillis, v -> client.elapseAwaitingRequests());
								}
							}
						}
						var state = new GameState(this, TurnState.TURN_IN_PROGRESS, false);
						for (var updateClient : getClients()) {
							updateClient.onUpdate(state);
						}
					} finally {
						lock.unlock();
					}
				} else {
					startTurn(getActivePlayerId());
				}
				while (takeActionInTurn()) {
					span.log(ImmutableMap.of("turn", getTurn(), "activePlayerId", getActivePlayerId(), "actionsThisTurn", getActionsThisTurn()));
					if (!isRunning()) {
						break;
					}
				}
				if (getTurn() > GameLogic.TURN_LIMIT) {
					break;
				}
			}
			endGame();
		} finally {
			span.finish();
			scope.close();
		}
	}

	private int getTurnTimeForPlayer(int activePlayerId) {
		return getLogic().getTurnTimeMillis(activePlayerId);
	}

	private void updateClientsWithGameState() {
		var state = getGameStateCopy();
		for (var client : getClients()) {
			client.onUpdate(state);
		}
	}

	private void updateClientWithGameState(int playerId) {
		var state = getGameStateCopy();
		for (var client : getClients()) {
			if (client.getPlayerId() == playerId) {
				client.onUpdate(state);
			}
		}
	}

	@Override
	public void onNotificationWillFire(Notification event) {
		super.onNotificationWillFire(event);
		notificationCounter.incrementAndGet();
		if (event.isClientInterested()) {
			var gameStateCopy = getGameStateCopy();
			for (var client : getClients()) {
				client.sendNotification(event, gameStateCopy);
			}
		}
	}

	@Override
	public void onNotificationDidFire(Notification event) {
		super.onNotificationDidFire(event);
		if (notificationCounter.decrementAndGet() == 0) {
			for (var client : getClients()) {
				client.lastEvent();
			}
		}
	}

	@Override
	public void onEnchantmentFired(Enchantment trigger) {
		super.onEnchantmentFired(trigger);

		var triggerFired = new TriggerFired(this, trigger);
		final var gameStateCopy = getGameStateCopy();

		if (trigger.getHostReference() != null) {
			var host = getEntities()
					.filter(e -> e.getId() == trigger.getHostReference().getId())
					.findFirst()
					.orElse(null);

			if (host != null && GameLogic.PRIVATE.contains(host.getZone()) && host.getSourceCard() != null && !host.getSourceCard().getDesc().revealsSelf()) {
				var owner = host.getOwner();
				var client = getClient(owner);
				if (client != null) {
					client.sendNotification(triggerFired, gameStateCopy);
				}
				return;
			}
		}

		for (var client : getClients()) {
			client.sendNotification(triggerFired, gameStateCopy);
		}
	}

	@Override
	public void onWillPerformGameAction(int playerId, GameAction action) {
		super.onWillPerformGameAction(playerId, action);
		var gameStateCopy = getGameStateCopy();
		for (var client : getClients()) {
			client.sendNotification(action, gameStateCopy);
		}
	}

	@Override
	public void onDidPerformGameAction(int playerId, GameAction action) {
		super.onDidPerformGameAction(playerId, action);
		checkpoint();
	}

	/** Installs an engine state rebuilt from a persisted deterministic trace. */
	public void restoreFromTrace(@NotNull Trace trace) {
		var replayed = trace.replayContext(false, null, getCardCatalogue());
		if (replayed.updateAndGetGameOver()) {
			throw new IllegalStateException("persisted trace already represents a finished game");
		}
		setGameState(replayed.getGameStateCopy());
		var clonedLogic = replayed.getLogic().clone();
		clonedLogic.setContext(this);
		setLogic(clonedLogic);
		setActionsThisTurn(replayed.getActionsThisTurn());
		setTrace(trace);
		for (var configuration : playerConfigurations) {
			var player = getPlayer(configuration.getPlayerId());
			player.getAttributes().put(Attribute.USER_ID, configuration.getUserId());
			player.getAttributes().put(Attribute.DECK_ID, configuration.getDeck().getDeckId());
		}
		restored = true;
	}

	private boolean isResumableBotMatch() {
		return playerConfigurations.size() == 2
				&& playerConfigurations.stream().filter(Configuration::isBot).count() == 1;
	}

	/** Trace writes are intentionally synchronous with game mutation, so a shutdown cannot outrun a checkpoint. */
	public void checkpoint() {
		if (!isResumableBotMatch() || getTrace().getMulligans() == null) {
			return;
		}
		await(withDslContext(dsl -> dsl.update(Tables.GAMES)
				.set(Tables.GAMES.TRACE, getTrace().toJson())
				.where(Tables.GAMES.ID.eq(Long.valueOf(gameId)))));
	}

	@Override
	public void concede(int playerId) {
		lock.lock();
		try {
			super.concede(playerId);
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected void notifyPlayersGameOver() {
		var winningPlayerId = getWinner() == null ? -1 : getWinner().getId();
		for (var client : getClients()) {
			var ready = clientsReady.get(client.getPlayerId());
			if (ready != null && !ready.future().isComplete()) {
				continue;
			}
			LOGGER.trace("notifyPlayersGameOver: notifying {}", client.getUserId());
			client.onGameOver(this, client.getPlayerId(), winningPlayerId);
		}
	}

	@Override
	public String toString() {
		return String.format("[ServerGameContext gameId=%s turn=%d]", getGameId(), getTurn());
	}

	@Override
	public String getGameId() {
		return gameId;
	}

	@Override
	protected void endGame() {
		lock.lock();
		try {
			if (!endGameCalled.compareAndSet(false, true)) {
				return;
			}

			LOGGER.debug("endGame {}: calling end game", gameId);

			if (turnTimerId != null && vertx != null) {
				vertx.cancelTimer(turnTimerId);
				turnTimerId = null;
			}

			isRunning = false;
			for (var client : getClients()) {
				client.closeInboundMessages();
			}

			if (didCallEndGame()) {
				return;
			}

			GAMES_FINISHED.increment();

			var unregistrations = new ArrayList<Future<?>>();
			for (var consumer : gameLifecycleConsumers) {
				unregistrations.add(consumer.unregister().otherwiseEmpty());
			}
			for (var client : getClients()) {
				unregistrations.add(client.unregisterConsumer());
			}
			if (Thread.currentThread().isVirtual() && !unregistrations.isEmpty()) {
				await(Future.all(unregistrations));
			}

			updateAndGetGameOver();
			RogueManager.handleGameEnd(Long.parseLong(gameId), getWinner() == null ? null : getWinner().getUserId());

			try {
				if (this.cluster != null) {
					this.cluster.removeGameAndRecordReplay(this.gameId);
				}
			} catch (Throwable t) {
				LOGGER.warn("could not record game because", t);
			}

			super.endGame();
			LOGGER.trace("endGame {}: called super.endGame", gameId);

			var flushes = new ArrayList<Future<?>>();
			for (var client : getClients()) {
				var ready = clientsReady.get(client.getPlayerId());
				if (ready != null && !ready.future().isComplete()) {
					continue;
				}
				flushes.add(com.hiddenswitch.framework.Environment.timeout(client.lastWriteFuture(), 5000L, vertx).otherwiseEmpty());
			}
			if (Thread.currentThread().isVirtual() && !flushes.isEmpty()) {
				await(Future.all(flushes));
			}
		} finally {
			close();
			lock.unlock();
		}
	}

	@Override
	public void close() {
		if (!this.closed.compareAndSet(false, true)) {
			return;
		}
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("ServerGameContext/dispose")
				.asChildOf(getSpanContext())
				.withTag("gameId", getGameId())
				.start();
		try {
			var unregistrations = new ArrayList<Future<?>>();
			for (var consumer : gameLifecycleConsumers) {
				unregistrations.add(consumer.unregister().otherwiseEmpty());
			}
			for (var deployFut : userVerticleDeployments) {
				if (deployFut.succeeded() && deployFut.result() != null && vertx != null) {
					unregistrations.add(vertx.undeploy(deployFut.result()).otherwiseEmpty());
				} else if (vertx != null) {
					deployFut.onSuccess(id -> vertx.undeploy(id).otherwiseEmpty());
				}
			}
			userVerticleDeployments.clear();
			if (Thread.currentThread().isVirtual() && !unregistrations.isEmpty() && !Thread.currentThread().isInterrupted()) {
				try {
					await(Future.all(unregistrations));
				} catch (Throwable ignored) {
				}
			}
			if (vertx != null && deploymentID() != null && vertx.deploymentIDs().contains(deploymentID())) {
				vertx.undeploy(deploymentID()).otherwiseEmpty();
			}
			super.close();
		} finally {
			span.finish();
		}
	}

	@Override
	public void onEmote(Client sender, int entityId, String message) {
		for (var client : getClients()) {
			client.sendEmote(entityId, message);
		}
	}

	@Override
	public void onConcede(Client sender) {
		concede(sender.getPlayerId());
	}

	@Override
	public void onTouch(Client sender, int entityId) {
		touch(sender, entityId, true);
	}

	@Override
	public void onUntouch(Client sender, int entityId) {
		touch(sender, entityId, false);
	}

	private void touch(Client sender, int entityId, boolean touching) {
		var touch = new TouchingNotification(sender.getPlayerId(), entityId, touching);
		for (var client : getClients()) {
			if (client.getPlayerId() != sender.getPlayerId()) {
				client.sendNotification(touch, null);
			}
		}
	}

	@Override
	public boolean isGameReady() {
		return gameReady && clientsReady.values().stream().allMatch(promise -> promise.future().succeeded());
	}

	@Override
	public Random getRandom() {
		return getLogic().getRandom();
	}

	@Override
	public Long getMillisRemaining() {
		if (timerStartTimeMillis == null || timerLengthMillis == null) {
			return null;
		}
		return Math.max(0, timerLengthMillis - (System.currentTimeMillis() - timerStartTimeMillis));
	}

	@Override
	public boolean isGameOver() {
		return updateAndGetGameOver();
	}

	@Override
	public GameDeck getDeck(Player player, String name) {
		var allDecks = await(Legacy.getAllDecks(getCardCatalogue(), player.getUserId()));
		var deck = allDecks.getDecksList().stream().filter(get -> get.getCollection().getName().equalsIgnoreCase(name)).findAny();
		return deck.map(decksGetResponse -> ModelConversions.getGameDeck(player.getUserId(), decksGetResponse, getCardCatalogue())).orElse(null);
	}

	@Override
	public void onPlayerReady(Client client) {
		var ready = clientsReady.get(client.getPlayerId());
		if (ready != null) {
			ready.tryComplete(client);
		}
	}

	@Override
	public void onPlayerReconnected(Client client) {
		lock.lock();
		try {
			client.onConnectionStarted(getActivePlayer());
			onPlayerReady(client);
			client.onUpdate(getGameStateCopy());
		} finally {
			lock.unlock();
		}
	}

	public List<Client> getClients() {
		return clients;
	}

	public Client getClient(int playerId) {
		for (var client : getClients()) {
			if (client.getPlayerId() == playerId) {
				return client;
			}
		}
		return null;
	}

	public Collection<Configuration> getPlayerConfigurations() {
		return playerConfigurations;
	}

	public boolean isRunning() {
		return isRunning && !this.interrupted;
	}

	public void loseBothPlayers() {
		currentContext.set(this);
		getLogic().loseBothPlayers();
		endGame();
	}
}
