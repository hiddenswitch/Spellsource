package com.hiddenswitch.framework.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.spellsource.Tables;
import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.opentracing.log.Fields;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import io.vertx.await.Async;
import io.vertx.core.Future;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.streams.WriteStream;
import io.vertx.grpc.server.GrpcServerRequest;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.AbstractBehaviour;
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
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static com.hiddenswitch.framework.Environment.*;
import static com.hiddenswitch.framework.Games.ADDRESS_IS_IN_GAME;
import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static io.vertx.await.Async.await;
import static io.vertx.core.CompositeFuture.all;
import static java.util.stream.Collectors.toList;

/**
 * A networked game context from the server's point of view.
 * <p>
 * In addition to storing game state, this class also stores references to {@link Client} objects that (1) get notified
 * when game state changes and how, and (2) allow this class to
 * {@link Behaviour#requestAction(GameContext, Player, List)} and {@link Behaviour#mulligan(GameContext, Player, List)}
 * over a network.
 * <p>
 */
public class ServerGameContext extends GameContext implements Server {
    public static final String WRITER_ADDRESS_PREFIX = "games:writer:";
    public static final String READER_ADDRESS_PREFIX = "games:reader:";
    private static final long CLOSE_TIMEOUT_MILLIS = 4000L;
	  private static final Counter GAMES_FINISHED = Counter.builder("games.finished")
            .description("The number of games finished.")
            .baseUnit(BaseUnits.EVENTS)
            .register(globalRegistry);
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerGameContext.class);
    private final transient Lock lock = new ReentrantLock();
    private final transient Map<Integer, Promise<Client>> clientsReady = new ConcurrentHashMap<>();
    private final transient List<Client> clients = new CopyOnWriteArrayList<>();
    private final transient List<Promise> registrationsReady = new CopyOnWriteArrayList<>();
    private final transient Promise<Void> initialization = Promise.promise();
    private final Context context;
    private final List<MessageConsumer<String>> inGameConsumers = new ArrayList<>();
    private final ClusteredGames cluster;
    private final AbstractVirtualThreadVerticle verticle;
    private final List<Configuration> playerConfigurations = new ArrayList<>();
    private final String gameId;
    private final Deque<Trigger> gameTriggers = new ConcurrentLinkedDeque<>();
    private final Scheduler scheduler;
    private final AtomicInteger notificationCounter = new AtomicInteger(0);
    private transient Long turnTimerId;
    private boolean isRunning = false;
    private Long timerStartTimeMillis;
    private Long timerLengthMillis;
    private boolean interrupted;

    /**
     * {@inheritDoc}
     * <p>
     *
     * @param gameId               The game ID that corresponds to this game context.
     * @param scheduler            The {@link Scheduler} instance to use for scheduling game events.
     * @param playerConfigurations The information about the players who will be connecting / playing this game context
     * @param cluster
     * @param verticle
     */
    public ServerGameContext(@NotNull String gameId, @NotNull Scheduler scheduler, @NotNull List<Configuration> playerConfigurations, CardCatalogue cardCatalogue, ClusteredGames cluster, AbstractVirtualThreadVerticle verticle) {
        super();
        this.cluster = cluster;
        this.verticle = verticle;
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
            this.scheduler = scheduler;
            // Save the information used to create this game
            this.playerConfigurations.addAll(playerConfigurations);
            this.context = Vertx.currentContext();
            Function<Callable<GameAction>, Future<GameAction>> execBlockingOnContext = gameActionCallable -> context.executeBlocking(promise -> {
                try {
                    promise.tryComplete(gameActionCallable.call());
                } catch (Throwable t) {
                    promise.tryFail(t);
                }
            }, false);

            // The deck format will be the smallest one that can contain all the cards in the decks.
            setDeckFormat(getCardCatalogue().getSmallestSupersetFormat(playerConfigurations
                    .stream()
                    .map(Configuration::getDeck)
                    // These must be game decks at this point
                    .map(deck -> (GameDeck) deck)
                    .collect(toList())));

            // Persistence effects mean cards that remember things that have happened to them in other games
            enablePersistenceEffects();
            enableTriggers();

            // Each configuration corresponds to a human or bot player
            // To accommodate spectators or multiple-controllers-per-game-player, use additional Client objects
            for (var configuration : getPlayerConfigurations()) {
                var userId = configuration.getUserId();

                // Initialize the player objects
                // This will create an actual valid deck and player object
                var deck = (GameDeck) configuration.getDeck().clone();

                var player = Player.forUser(userId, configuration.getPlayerId(), deck);
                setPlayer(configuration.getPlayerId(), player);
                // ...and import all the attributes that might be specific to the queue and its rules (typically just the
                // DECK_ID and USER_ID attributes at the moment.
                for (var kv : configuration.getPlayerAttributes().entrySet()) {
                    player.getAttributes().put(kv.getKey(), kv.getValue());
                }

                // Register that the user is in this game
                var inGameConsumer = registerInGame(gameId, userId);
                // Undeploying the verticle this is constructed in will automatically unregister the consumer
                Promise<Void> inGameRegistration = Promise.promise();
                inGameConsumer.completionHandler(inGameRegistration);
                registrationsReady.add(inGameRegistration);

                // When the game ends remove the fact that the user is in this game
                this.inGameConsumers.add(inGameConsumer);

                // Bots simply forward their requests to a bot service provider, that executes the bot logic on a worker thread
                if (configuration.isBot()) {
                    player.getAttributes().put(Attribute.AI_OPPONENT, true);
                    var behaviour = new GameStateValueBehaviour() {
                        @Override
                        public @Nullable GameAction requestAction(@NotNull GameContext context, @NotNull Player player, @NotNull List<GameAction> validActions) {
                            Future<GameAction> nextAction = execBlockingOnContext.apply(() -> super.requestAction(context, player, validActions));
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
                    // Does not have a client representing it
                } else {
                    // Connect to the GRPC stream representing this user by connecting to its handler advertised on the event bus
                    var serverGameContext = this;
                    // Improve robustness of the user's event bus handlers and interaction with the game context by giving it a
                    // distinct task queue from the game and/or other users.
                    var userVerticle = new AbstractVirtualThreadVerticle() {
                        private UnityClientBehaviour client;
                        private MessageProducer<Spellsource.ServerToClientMessage> producer;
                        private MessageConsumer<Spellsource.ClientToServerMessage> consumer;

                        public void startVirtual() {
                            var bus = Vertx.currentContext().owner().eventBus();
                            this.consumer = bus.<Spellsource.ClientToServerMessage>consumer(getMessagesFromClientAddress(userId));
                            consumer.setMaxBufferedMessages(Integer.MAX_VALUE);
                            // By using a publisher, we do not require that there be a working connection while sending
                            this.producer = bus.<Spellsource.ServerToClientMessage>publisher(getMessagesFromServerAddress(userId));
                            // The event bus

                            Promise<Void> registration = Promise.promise();
                            consumer.completionHandler(registration);
                            registrationsReady.add(registration);

                            // Create a client that handles game events and action/mulligan requests
                            this.client = new UnityClientBehaviour(serverGameContext,
                                    new VertxScheduler(),
                                    consumer.bodyStream(),
                                    producer,
                                    userId,
                                    configuration.getPlayerId(),
                                    configuration.getNoActivityTimeout());
                            // The client implements the behaviour interface since it is supposed to be able to respond to requestAction
                            // and mulligan calls
                            setBehaviour(configuration.getPlayerId(), client);
                            // However, unlike a behaviour, there can be multiple clients per player ID. This will facilitate spectating.
                            getClients().add(client);
                        }

                        @Override
                        public void stopVirtual() {
                            client.close(Promise.promise());
                            await(consumer.unregister());
                            await(producer.close());
                        }
                    };

                    var vertx = Vertx.currentContext().owner();
                    // for now create a stub behavior
                    setBehaviour(configuration.getPlayerId(), new AbstractBehaviour() {
                        @Override
                        public String getName() {
                            return "(Unexpected)";
                        }

                        @Override
                        public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
                            throw new UnsupportedOperationException("unexpectedly requested mulligan from undeployed behavior");
                        }

                        @Override
                        public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
                            throw new UnsupportedOperationException("unexpectedly requested action from undeployed behavior");
                        }
                    });

                    // todo: fix verticle loading
                    // The verticle takes time to deploy, which can interfere with the expectation that once a ServerGameContext
                    // is created, messages can be sent. handlersReady() should already be dealing with this, but it doesn't
                    // apparently, so the SubscribeGame method is configured to retry sending for now.
                    vertx.deployVerticle(userVerticle);
                    Promise<Client> fut = Promise.promise();
                    clientsReady.put(configuration.getPlayerId(), fut);
                }
            }
        } finally {
            span.finish();
        }
    }

    public static void subscribeGame(String userId, GrpcServerRequest<Spellsource.ClientToServerMessage, Spellsource.ServerToClientMessage> request, WriteStream<Spellsource.ServerToClientMessage> response) {
        var context = Vertx.currentContext();
        var vertx = context.owner();
        var eventBus = vertx.eventBus();
        var keepAliveManager = com.hiddenswitch.framework.Environment.keepAliveManager(request.connection(), v -> {
            try {
                request.end();
            } catch (IllegalStateException ignored) {
            }
        }, true);
        keepAliveManager.onTransportStarted();
        // Make the game subscription robust against a ServerGameContext not yet being ready to receive messages from this
        // client. Messages are buffered and dequeued. A NO_HANDLERS exception indicates the ServerGameContext isn't ready
        // yet. We retry, up to ten times, with a 1-second interval, to send, since most games are ready within 10s.
        var publisher = new RetryMessageProducer<>(eventBus.<Spellsource.ClientToServerMessage>publisher(getMessagesFromClientAddress(userId)), 10, 1000);
        request.handler(body -> {
            keepAliveManager.onDataReceived();
            publisher.write(body);
        });

        var consumer = eventBus.<Spellsource.ServerToClientMessage>consumer(getMessagesFromServerAddress(userId));
        request.endHandler(v -> {
            keepAliveManager.onTransportTermination();
            consumer.unregister();
        });
        request.exceptionHandler(t -> {
            keepAliveManager.onTransportTermination();
            consumer.unregister();
        });
        consumer.pause();
        consumer.setMaxBufferedMessages(Integer.MAX_VALUE);
        consumer.bodyStream().pipeTo(response);
        consumer.completionHandler(v -> consumer.resume());
        request.resume();
    }

    /**
     * Represents the address on the event bus to which the client sends its outgoing messages.
     * <p>
     * Messages <b>consumed</b> from this address will be arriving <b>from</b> the web socket and <b>received</b> by the
     * server infrastructure (typically).
     *
     * @param userId The user from whom messages will arrive
     * @return
     */
    @NotNull
    public static String getMessagesFromClientAddress(String userId) {
        return READER_ADDRESS_PREFIX + userId;
    }

    /**
     * Represents the address on the event bus to which the client receives incoming messages.
     * <p>
     * Messages <b>sent</b> to this address will be written <b>to</b> the web socket and <b>received</b> by the Unity
     * client.
     *
     * @param userId The user to whom messages should be delivered
     * @return
     */
    @NotNull
    public static String getMessagesFromServerAddress(String userId) {
        return WRITER_ADDRESS_PREFIX + userId;
    }

    /**
     * Register that the specified user is now in a game
     *
     * @param thisGameId
     * @param userId
     * @return
     */
    static @NotNull MessageConsumer<String> registerInGame(@NotNull String thisGameId, @NotNull String userId) {
        var eb = Vertx.currentContext().owner().eventBus();
        return eb.consumer(ADDRESS_IS_IN_GAME + userId, (Message<String> req) -> req.reply(thisGameId));
    }


    /**
     * Enables this match to track persistence effects.
     */
    private void enablePersistenceEffects() {
//		getGameTriggers().add(new PersistenceTrigger(this, this.gameId));
    }

    /**
     * Enables this match to use custom networked triggers
     */
    private void enableTriggers() {
		/*
		for (var trigger : Spellsource.spellsource().getGameTriggers().values()) {
			Map<SpellArg, Object> arguments = new SpellDesc(DelegateSpell.class);
			arguments.put(SpellArg.NAME, trigger.getSpellId());
			var spell = new SpellDesc(arguments);
			var enchantment = new Enchantment();
			enchantment.getTriggers().add(trigger.getEventTriggerDesc().create());
			enchantment.setSpell(spell);
			enchantment.setOwner(0);
			getGameTriggers().add(enchantment);
		}
		*/
    }

    /**
     * {@inheritDoc}
     * <p>
     * Simultaneously processes mulligans and awaits until all human players have sent a "FIRST_MESSAGE."
     */
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

            // Make sure the players are initialized before sending the original player updates.
            getLogic().initializePlayerAndMoveMulliganToSetAside(PLAYER_1, startingPlayerId == PLAYER_1);
            getLogic().initializePlayerAndMoveMulliganToSetAside(PLAYER_2, startingPlayerId == PLAYER_2);

            initialization.complete();
            Future bothClientsReady;
            // todo: this should be configurable
            var timeout = Math.min(Games.getDefaultNoActivityTimeout(), Games.getDefaultConnectionTime());
            if (!clientsReady.values().stream().allMatch(fut -> fut.future().isComplete())) {
                // If this is interrupted, it will bubble up to the general interrupt handler
                try {
                    bothClientsReady = all(clientsReady.values().stream().map(Promise::future).collect(toList()));
                    var res = await(CompositeFuture.any(sleep(timeout), bothClientsReady));

                    if (res.isComplete(0)) {
                        //timed out
                        bothClientsReady = Future.failedFuture(new TimeoutException("timed out waiting for user"));
                    } else if (res.isComplete(1)) {
                        // succeeded
                    } else {
                        bothClientsReady = Future.failedFuture("some other issue");
                    }
                } catch (Throwable t) {
                    bothClientsReady = Future.failedFuture(t);
                }
            } else {
                bothClientsReady = Future.succeededFuture();
            }
            // One of the two clients did not connect in time, log a win for the player that connected
            if (bothClientsReady.failed()) {
                // Mark the players that have not connected in time as destroyed, which in updateAndGetGameOver will eventually
                // lead to a double loss
                for (var entry : clientsReady.entrySet()) {
                    if (!entry.getValue().future().isComplete()) {
                        LOGGER.debug("init {}: Game prematurely ended because player id={} did not connect in {}ms", getGameId(), entry.getKey(), timeout);
                        getLogic().concede(entry.getKey());
                    }
                }
                isRunning = false;
                // resume() will check if the game is over
                return;
            }

            // update game status
            await(withDslContext(dsl -> dsl.update(Tables.GAMES)
                    .set(Tables.GAMES.STATUS, GameStateEnum.STARTED)
                    .where(Tables.GAMES.ID.eq(Long.valueOf(gameId)))));

            // When players reconnect, we don't want them to trigger these futures anymore
            clientsReady.clear();

            // Signal to the game context has made everything have valid IDs.
            getLogic().contextReady();

            for (var client : getClients()) {
                client.onConnectionStarted(getActivePlayer());
            }

            // Record the time that we started the game in system milliseconds, in case a card wants to use this for an event-based thing.
            getPlayers().forEach(p -> p.getAttributes().put(Attribute.GAME_START_TIME_MILLIS, (int) (System.currentTimeMillis() % Integer.MAX_VALUE)));

            // Set the mulligan timer
            final Long mulliganTimerId;
            if (getBehaviours().stream().allMatch(Behaviour::isHuman)) {
                // Only two human players will get timers
                timerLengthMillis = getLogic().getMulliganTimeMillis();
                timerStartTimeMillis = System.currentTimeMillis();
                mulliganTimerId = scheduler.setTimer(timerLengthMillis, this::endMulligans);
            } else {
                LOGGER.trace("init {}: No mulligan timer set for game because all players are not human", getGameId());
                timerLengthMillis = null;
                timerStartTimeMillis = null;
                mulliganTimerId = null;
            }

            // Send the clients the current game state
            // This is the first time the client should be receiving data
            updateClientsWithGameState();

            // Simultaneous mulligans now
            Promise<List<Card>> mulligansActive = Promise.promise();
            Promise<List<Card>> mulligansNonActive = Promise.promise();

            var firstHandActive = getActivePlayer().getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
            var firstHandNonActive = getNonActivePlayer().getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
            if (firstHandActive.isEmpty()) {
                mulligansActive.complete(Collections.emptyList());
            } else {
                getBehaviours().get(getActivePlayerId()).mulliganAsync(this, getActivePlayer(), firstHandActive, res -> {
                    // TODO: Game started should not be set here, strictly speaking
                    // Fixes issues with mulligans not being dismissed correctly
                    getActivePlayer().setAttribute(Attribute.GAME_STARTED);
                    // Update this user
                    updateClientWithGameState(getActivePlayerId());
                    mulligansActive.complete(res);
                });
            }

            if (firstHandNonActive.isEmpty()) {
                mulligansNonActive.complete(Collections.emptyList());
            } else {
                getBehaviours().get(getNonActivePlayerId()).mulliganAsync(this, getNonActivePlayer(), firstHandNonActive, (res) -> {
                    getNonActivePlayer().setAttribute(Attribute.GAME_STARTED);
                    updateClientWithGameState(getNonActivePlayerId());
                    mulligansNonActive.complete(res);
                });
            }

            // If this is interrupted, it'll bubble up to the general interrupt handler
            var simultaneousMulligans = await(CompositeFuture.join(mulligansActive.future(), mulligansNonActive.future()));

            // If we got this far, we should cancel the time
            if (mulliganTimerId != null) {
                scheduler.cancelTimer(mulliganTimerId);
            }

            // The timer will have completed the mulligans, that's why we don't timeout simultaneous mulligans
            if (simultaneousMulligans == null || simultaneousMulligans.failed()) {
                // An error occurred
                throw new VertxException(simultaneousMulligans == null ? new TimeoutException() : simultaneousMulligans.cause());
            }

            var discardedCardsActive = mulligansActive.future().result();
            var discardedCardsNonActive = mulligansNonActive.future().result();
            getLogic().handleMulligan(getActivePlayer(), true, discardedCardsActive);
            getLogic().handleMulligan(getNonActivePlayer(), false, discardedCardsNonActive);

            traceMulligans(mulligansActive.future().result(), mulligansNonActive.future().result());

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

    /**
     * Plays the game. {@link GameContext#play()} is eventually called.
     *
     * @param fork When {@code false}, blocks until the game is done. Otherwise, plays the game inside a {@link Thread}
     */
    public void play(boolean fork) {
        isRunning = true;
        if (fork) {
            // We're going to build this fiber with a huge stack
            if (getThread() != null) {
                throw new UnsupportedOperationException("Cannot play with a fork twice!");
            }
						// this should create a new thread
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
                // Send the trace information to the clients since the game is now running
                var carrier = new BinaryCarrier();
                tracer.inject(span.context(), Format.Builtin.BINARY, carrier);
                // workaround for current limitations in vertx
                this.interrupted = false;
                try {
                    LOGGER.trace("play: Starting forked game");
                    super.play(false);
                } catch (Throwable throwable) {
                    var rootCause = Throwables.getRootCause(throwable);
                    if (rootCause instanceof InterruptedException) {
                        interrupted = true;
                        // Generally only an interrupt from endGame() is allowed to gracefully interrupt this daemon.
                        span.log(ImmutableMap.of(
                                Fields.EVENT, "interrupt",
                                "graceful", true
                        ));
                        // The game is already ended whenever the fiber is interrupted, there's no other place that the external user
                        // is allowed to interrupt the fiber. So we don't need to call endGame here.
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
                    // Regardless of what happens that causes an event loop exception, make certain the user is released from their game
                    // clears the interrupted flag
                    // due to bugs, we have to not clear the previous interrupted status
                    interrupted = this.interrupted || Thread.interrupted();
                    close();
                    if (interrupted) {
                        // sets it back
                        Thread.currentThread().interrupt();
                    }
                    // Always set the trace
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
        Async.lock(lock);
        try {
            for (var behaviour : getBehaviours()) {
                if (behaviour instanceof HasElapsableTurns) {
                    ((HasElapsableTurns) behaviour).setElapsed(false);
                }
            }

            // cancel timers
            if (turnTimerId != null) {
                var didCancel = false;
                for (var client : getClients()) {
                    didCancel |= client.getScheduler().cancelTimer(turnTimerId);
                }

                if (didCancel) {
                    turnTimerId = null;
                } else {
                    LOGGER.error("unexpectedly could not cancel turn timer");
                }
            }

            // set up the timer in all human games
            // todo: make this configurable
            if (getBehaviours().stream().allMatch(Behaviour::isHuman)) {
                var client = getClient(playerId);
                Objects.requireNonNull(client, "client unexpectedly null");

                timerLengthMillis = (long) getTurnTimeForPlayer(getActivePlayerId());
                timerStartTimeMillis = System.currentTimeMillis();

                var scheduler = client.getScheduler();
                if (timerLengthMillis > 0) {
                    turnTimerId = scheduler.setTimer(timerLengthMillis, v -> client.elapseAwaitingRequests());
                }
            } else {
                timerLengthMillis = null;
                timerStartTimeMillis = null;
            }

            super.startTurn(playerId);
            var state = new GameState(this, TurnState.TURN_IN_PROGRESS);
            for (var updateClient : getClients()) {
                updateClient.onUpdate(state);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void endTurn() {
        Async.lock(lock);
        try {
            if (turnTimerId != null) {
                scheduler.cancelTimer(turnTimerId);
                turnTimerId = null;
            }
            super.endTurn();
            for (var client : getClients()) {
                client.onTurnEnd(getActivePlayer(), getTurn(), getTurnState());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Ends the mulligans early due to timer elapsing.
     *
     * @param ignored The ignored timer elapse result.
     */
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
            while (!updateAndGetGameOver()) {
                span.log(ImmutableMap.of("turn", getTurn(), "activePlayerId", getActivePlayerId()));
                if (!isRunning()) {
                    break;
                }
                startTurn(getActivePlayerId());
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
        // Do not build game state for events the client is not interested in
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

        // If the trigger is in a private place, do not fire it for the public player
        if (trigger.getHostReference() != null) {
            var host = getEntities()
                    .filter(e -> e.getId() == trigger.getHostReference().getId())
                    .findFirst()
                    .orElse(null);

            if (host != null && GameLogic.PRIVATE.contains(host.getZone()) && host.getSourceCard() != null && !host.getSourceCard().getDesc().revealsSelf()) {
                var owner = host.getOwner();
                var client = getClient(owner);

                // Don't send spurious, private notifications to bot players / players not represented by clients
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
    public void concede(int playerId) {
        Async.lock(lock);
        try {
            // TODO: Make sure we don't have to do anything special here
            super.concede(playerId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void notifyPlayersGameOver() {
        for (var client : getClients()) {
            LOGGER.trace("notifyPlayersGameOver: notifying {}", client.getUserId());
            client.sendGameOver(getGameStateCopy(), getWinner());
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
        Async.lock(lock);
        try {
            LOGGER.trace("endGame {}: calling end game", gameId);

            if (turnTimerId != null) {
                scheduler.cancelTimer(turnTimerId);
                turnTimerId = null;
            }

            isRunning = false;
            // Close the inbound messages from the client, they should be ignored by these client instances
            // This way, a user doesn't accidentally trigger some other kind of processing that's only going to be interrupted
            // later. However, this does block emote processing, which is unfortunate.
            for (var client : getClients()) {
                client.closeInboundMessages();
            }

            // Don't end the game more than once.
            if (didCallEndGame()) {
                return;
            }

						GAMES_FINISHED.increment();

            // We have to release the users before we call end game, because that way when the client receives the end game
            // message, their model of the world is that they're no longer in a game.
            // Actually end the game
            super.endGame();
            LOGGER.trace("endGame {}: called super.endGame", gameId);

            for (var consumer : inGameConsumers) {
                consumer.unregister();
            }

            LOGGER.trace("endGame {}: endGameHandlers run", gameId);

            try {
                this.cluster.removeGameAndRecordReplay(this.gameId);
            } catch (Throwable t) {
                LOGGER.warn("could not record game because", t);
            } finally {
                // todo: this should really be the last end game handler
                if (verticle.getVertx().deploymentIDs().contains(verticle.deploymentID())) {
                    verticle.getVertx().undeploy(verticle.deploymentID()).onFailure(com.hiddenswitch.framework.Environment.onFailure());
                }
            }
        } finally {
            close();
            lock.unlock();
        }
    }


    /**
     * Gets the user IDs of the players in this game context. Includes the AI player
     *
     * @return A list of user IDs.
     */
    private List<String> getUserIds() {
        return getPlayerConfigurations().stream().map(Configuration::getUserId).collect(toList());
    }

    @Override
    public void close() {
        var tracer = GlobalTracer.get();
        var span = tracer.buildSpan("ServerGameContext/dispose")
                .asChildOf(getSpanContext())
                .withTag("gameId", getGameId())
                .start();
        try {
            super.close();
        } finally {
            span.finish();
        }
    }

    public Collection<Trigger> getGameTriggers() {
        return gameTriggers;
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
        return clientsReady.values().stream().allMatch(promise -> promise.future().succeeded());
    }

    @Override
    public Random getRandom() {
        return getLogic().getRandom();
    }

    @Override
    public Long getMillisRemaining() {
        if (timerStartTimeMillis == null
                || timerLengthMillis == null) {
            return null;
        }

        return Math.max(0, timerLengthMillis - (System.currentTimeMillis() - timerStartTimeMillis));
    }

    @Override
    public boolean isGameOver() {
        return updateAndGetGameOver();
    }

    /**
     * Retrieves the deck using the player's {@link Player#getUserId()}
     *
     * @param player The player whose deck collections should be queried.
     * @param name   The name of the deck to retrieve
     * @return A {@link GameDeck} with valid but not located entities, or {@code null} if the deck could not be found.
     */
    @Override
    public GameDeck getDeck(Player player, String name) {
        var allDecks = await(Legacy.getAllDecks(getCardCatalogue(), player.getUserId()));
        var deck = allDecks.getDecksList().stream().filter(get -> get.getCollection().getName().equalsIgnoreCase(name)).findAny();
        return deck.map(decksGetResponse -> ModelConversions.getGameDeck(player.getUserId(), decksGetResponse, getCardCatalogue())).orElse(null);
    }

    @Override
    public void onPlayerReady(Client client) {
        if (clientsReady.containsKey(client.getPlayerId())) {
            clientsReady.get(client.getPlayerId()).tryComplete(client);
        } else {
            LOGGER.trace("tried to signal player was ready that was not");
        }
    }

    @Override
    public void onPlayerReconnected(Client client) {
        Async.lock(lock);
        try {
            // Update the client
            var listIterator = getClients().listIterator();
            while (listIterator.hasNext()) {
                var iteratee = listIterator.next();
                // we're only expecting to replace the client if a new one is actually made
                // the lifecycle of the client isn't related to the lifecycle of the network connection
                if (iteratee.getPlayerId() == client.getPlayerId() && client != iteratee) {
                    var promise = Promise.<Void>promise();
                    iteratee.copyRequestsTo(client);
                    iteratee.close(promise);
                    await(timeout(promise.future(), CLOSE_TIMEOUT_MILLIS));
                    listIterator.set(client);
                    iteratee = client;
                }
                iteratee.onConnectionStarted(getActivePlayer());
            }

            if (client instanceof Behaviour) {
                // Update the behaviour
                setBehaviour(client.getPlayerId(), (Behaviour) client);
            }

            onPlayerReady(client);
            // Only update the reconnecting client
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

        // This player is probably a bot.
        return null;
    }

    public Collection<Configuration> getPlayerConfigurations() {
        return playerConfigurations;
    }

    public boolean isRunning() {
        if (isRunning) {
            return !this.interrupted;
        }
        return false;
    }

    /**
     * Causes both players to lose the game. <b>Never deadlocks.</b>
     * <p>
     * This method is appropriate to call as a bail-out error procedure when the game context is being modified externally
     * by editing or otherwise.
     * <p>
     * Ending the game requires the lock.
     */

    public void loseBothPlayers() {
        try {
            currentContext.set(this);
            getLogic().loseBothPlayers();
            endGame();
        } finally {
            // This should be a no-op in the new engineering of this
        }
    }

    /**
     * Returns once the handlers have successfully registered on this instance
     */

    public Future<CompositeFuture> handlersReady() {
        return all(registrationsReady.stream().map(Promise::future).toList());
    }

}
