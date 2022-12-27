package com.hiddenswitch.framework.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.spellsource.Tables;
import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.opentracing.log.Fields;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Future;
import io.vertx.core.*;
import io.vertx.core.eventbus.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.executeblocking.ExecuteBlocking;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
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
import java.util.function.Consumer;
import java.util.function.Function;

import static com.hiddenswitch.framework.Environment.sleep;
import static com.hiddenswitch.framework.Environment.withDslContext;
import static com.hiddenswitch.framework.Games.ADDRESS_IS_IN_GAME;
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
	private static final long CLOSE_TIMEOUT_MILLIS = 4000L;
	private static final long REGISTRATION_TIMEOUT = 4000L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerGameContext.class);
	public static final String WRITER_ADDRESS_PREFIX = "Games.writer.";
	public static final String READER_ADDRESS_PREFIX = "Games.reader.";

	private final transient ReentrantLock lock = new ReentrantLock();
	private final transient Queue<Consumer<ServerGameContext>> onGameEndHandlers = new ConcurrentLinkedQueue<>();
	private final transient Map<Integer, Promise<Client>> clientsReady = new ConcurrentHashMap<>();
	private final transient List<Client> clients = new ArrayList<>();
	private final transient List<Promise> registrationsReady = new ArrayList<>();
	private final transient Promise<Void> initialization = Promise.promise();
	private final Context context;
	private transient Long turnTimerId;
	private final List<Configuration> playerConfigurations = new ArrayList<>();
	private final List<Closeable> closeables = new ArrayList<>();
	private final String gameId;
	private final Deque<Trigger> gameTriggers = new ConcurrentLinkedDeque<>();
	private final Scheduler scheduler;
	private boolean isRunning = false;
	private final AtomicInteger notificationCounter = new AtomicInteger(0);
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
	 */
	public ServerGameContext(@NotNull String gameId, @NotNull Scheduler scheduler, @NotNull List<Configuration> playerConfigurations) {
		super();
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
			Function<Callable<?>, Future<?>> execBlockingOnContext = ExecuteBlocking::executeBlocking;

			// The deck format will be the smallest one that can contain all the cards in the decks.
			setDeckFormat(DeckFormat.getSmallestSupersetFormat(playerConfigurations
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
				closeables.add(inGameConsumer::unregister);
				Promise<Void> inGameRegistration = Promise.promise();
				inGameConsumer.completionHandler(inGameRegistration);
				registrationsReady.add(inGameRegistration);

				// When the game ends remove the fact that the user is in this game
				addEndGameHandler(ctx -> await(inGameConsumer.unregister()));

				Closeable closeableBehaviour = null;
				// Bots simply forward their requests to a bot service provider, that executes the bot logic on a worker thread
				if (configuration.isBot()) {
					player.getAttributes().put(Attribute.AI_OPPONENT, true);
					// TODO: this has to go to a blocking worker thread
					var behaviour = new GameStateValueBehaviour() {
						@Override
						public @Nullable GameAction requestAction(@NotNull GameContext context, @NotNull Player player, @NotNull List<GameAction> validActions) {
							var fut = new CompletableFuture<GameAction>();
							execBlockingOnContext.apply(() -> {
								try {
									fut.complete(super.requestAction(context, player, validActions));
								} catch (Throwable t) {
									fut.completeExceptionally(t);
								}
								return null;
							});
							return fut.join();
						}
					};
					behaviour.setParallel(false)
							.setMaxDepth(2)
							.setLethalTimeout(15000L)
							.setTimeout(320L)
							.setThrowsExceptions(false)
							.setThrowOnInvalidPlan(false);

					setBehaviour(configuration.getPlayerId(), behaviour);
					closeableBehaviour = Promise::complete;
					// Does not have a client representing it
				} else {
					// Connect to the GRPC stream representing this user by connecting to its handler advertised on the event bus
					var bus = Vertx.currentContext().owner().eventBus();
					var consumer = fromClient(userId, bus);
					consumer.setMaxBufferedMessages(Integer.MAX_VALUE);
					consumer.pause();
					// By using a publisher, we do not require that there be a working connection while sending
					var producer = fromServer(userId, bus);

					Promise<Void> registration = Promise.promise();
					consumer.completionHandler(registration);
					registrationsReady.add(registration);

					// We'll want to unregister and close these when this instance is disposed
					closeables.add(consumer::unregister);
					closeables.add(producer::close);

					// Create a client that handles game events and action/mulligan requests
					LOGGER.trace("creating unity client behavior for user Id {}", userId);
					var client = new UnityClientBehaviour(this,
							new VertxScheduler(context.owner()),
							consumer.bodyStream(),
							producer,
							userId,
							configuration.getPlayerId(),
							configuration.getNoActivityTimeout());
					consumer.resume();

					// This client too needs to be closed
					closeableBehaviour = client;

					// The client implements the behaviour interface since it is supposed to be able to respond to requestAction
					// and mulligan calls
					setBehaviour(configuration.getPlayerId(), client);
					// However, unlike a behaviour, there can be multiple clients per player ID. This will facilitate spectating.
					getClients().add(client);
					// This future will be completed when FIRST_MESSAGE is received from the client. And the actual unity client
					// will only be notified to send this message when the constructor finishes.
					Promise<Client> fut = Promise.promise();
					clientsReady.put(configuration.getPlayerId(), fut);
				}

				var finalCloseableBehaviour = closeableBehaviour;

				closeables.add(fut -> {
					LOGGER.trace("closeables {}: closing client {}", gameId, finalCloseableBehaviour);
					finalCloseableBehaviour.close(fut);
				});
			}
		} finally {
			span.finish();
		}
	}

	public static MessageProducer<Spellsource.ServerToClientMessage> fromServer(String userId, EventBus bus) {
		return bus.publisher(getMessagesFromServerAddress(userId));
	}

	public static MessageConsumer<Spellsource.ClientToServerMessage> fromClient(String userId, EventBus bus) {
		return bus.consumer(getMessagesFromClientAddress(userId));
	}

	public static void subscribeGame(ReadStream<Spellsource.ClientToServerMessage> request, WriteStream<Spellsource.ServerToClientMessage> response) {
		var context = (ContextInternal) Vertx.currentContext();
		var eventBus = context.owner().eventBus();
		var userId = Accounts.userId();

		request.handler(msg -> clientToServer(eventBus, userId, msg));
		var consumer = fromServer(eventBus, userId);
		consumer.setMaxBufferedMessages(Integer.MAX_VALUE);
		consumer.bodyStream().pipeTo(response);
	}

	public static void clientToServer(EventBus bus, String userId, Spellsource.ClientToServerMessage msg) {
		if (msg.getMessageType() == Spellsource.MessageTypeMessage.MessageType.FIRST_MESSAGE) {
			LOGGER.trace("did get first message, now publishing");
		}
		bus.publish(getMessagesFromClientAddress(userId), msg, new DeliveryOptions());
	}

	public static MessageConsumer<Spellsource.ServerToClientMessage> fromServer(EventBus bus, String userId) {
		return bus.consumer(getMessagesFromServerAddress(userId));
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
				mulliganTimerId = scheduler.setTimer(timerLengthMillis,
						event -> com.hiddenswitch.framework.Environment.fiber(() -> {
							this.endMulligans(event);
							return (Void) null;
						}));
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
			var async = com.hiddenswitch.framework.Environment.async();
			async.run(v -> {
				var thread = Thread.currentThread();
				setThread(thread);
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
		lock.lock();
		try {
			// Start the turn timer
			if (turnTimerId != null) {
				scheduler.cancelTimer(turnTimerId);
				turnTimerId = null;
			}

			for (var behaviour : getBehaviours()) {
				if (behaviour instanceof HasElapsableTurns) {
					((HasElapsableTurns) behaviour).setElapsed(false);
				}
			}

			if (getBehaviours().get(getNonActivePlayerId()).isHuman()) {
				timerLengthMillis = (long) getTurnTimeForPlayer(getActivePlayerId());
				timerStartTimeMillis = System.currentTimeMillis();

				if (turnTimerId == null) {
					turnTimerId = scheduler.setTimer(timerLengthMillis, ignored -> com.hiddenswitch.framework.Environment.fiber(() -> {
						// Since executing the callback may itself trigger more action requests, we'll indicate to
						// the NetworkDelegate (i.e., this ServerGameContext instance) that further
						// networkRequestActions should be executed immediately.
						HasElapsableTurns client = getClient(playerId);
						if (client == null) {
							// Simply end the turn, since there were no requests pending to begin with
							endTurn();
						} else {
							client.elapseAwaitingRequests();
						}
						return (Void) null;
					}));
				} else {
					LOGGER.warn("startTurn {}: Timer set twice!", getGameId());
				}


			} else {
				timerLengthMillis = null;
				timerStartTimeMillis = null;
				LOGGER.trace("startTurn {}: Not setting timer because opponent is not human.", getGameId());
			}
			super.startTurn(playerId);
			var state = new GameState(this, TurnState.TURN_IN_PROGRESS);
			for (var client : getClients()) {
				client.onUpdate(state);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void endTurn() {
		lock.lock();
		try {
			super.endTurn();
			if (turnTimerId != null) {
				scheduler.cancelTimer(turnTimerId);
			}
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
		lock.lock();
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
		lock.lock();
		try {
			LOGGER.trace("endGame {}: calling end game", gameId);
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

			// We have to release the users before we call end game, because that way when the client receives the end game
			// message, their model of the world is that they're no longer in a game.
			releaseUsers();
			// Actually end the game
			super.endGame();
			LOGGER.trace("endGame {}: called super.endGame", gameId);

			// No end of game handler should be called more than once, so we're removing them one-by-one as we're processing
			// them.
			Consumer<ServerGameContext> handler;

			while ((handler = onGameEndHandlers.poll()) != null) {
				try {
					handler.accept(this);
				} catch (Throwable t) {
					LOGGER.warn("onGameEndHandler threw", t);
				}
			}

			LOGGER.trace("endGame {}: endGameHandlers run", gameId);

			// Now that the game is over, we have to stop processing the game event loop. We'll check that we're not in the
			// loop right now. If we are, we don't need to interrupt ourselves. Conceding, server shutdown and other lifecycle
			// issues will result, eventually, in calling end game outside this game's event loop. In that case, we'll
			// interrupt the event loop. Can the event loop itself be in the middle of calling endGame? In that case, the lock
			// prevents two endGames from being processed simultaneously, along with other important mutating events, like
			// other callbacks that may be processing player modifications to the game.
			if (getThread() != null && !Thread.currentThread().equals(getThread())) {
				getThread().interrupt();
				LOGGER.trace("endGame {}: interrupted fiber", gameId);
				setThread(null);
			}
		} finally {
			releaseUsers();
			close();
			lock.unlock();
		}
	}


	public void releaseUsers() {
		// This should be a no-op in the new engineering of this
	}

	/**
	 * Gets the user IDs of the players in this game context. Includes the AI player
	 *
	 * @return A list of user IDs.
	 */
	private List<String> getUserIds() {
		return getPlayerConfigurations().stream().map(Configuration::getUserId).collect(toList());
	}

	/**
	 * Adds a handler for when the game ends, for any reason.
	 *
	 * @param handler
	 */
	public void addEndGameHandler(Consumer<ServerGameContext> handler) {
		onGameEndHandlers.add(handler);
	}

	@Override
	public void close() {
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("ServerGameContext/dispose")
				.asChildOf(getSpanContext())
				.withTag("gameId", getGameId())
				.start();

		try (var s1 = tracer.activateSpan(span)) {
			var iter = closeables.iterator();
			while (iter.hasNext()) {
				try {
					var closeable = iter.next();
					var promise = Promise.<Void>promise();
					closeable.close(promise);
					await(promise.future());
				} catch (Throwable any) {
					Tracing.error(any, span, false);
				} finally {
					iter.remove();
				}
			}
			LOGGER.trace("dispose {}: closers closed", gameId);
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

	/**
	 * Retrieves the deck using the player's {@link Player#getUserId()}
	 *
	 * @param player The player whose deck collections should be queried.
	 * @param name   The name of the deck to retrieve
	 * @return A {@link GameDeck} with valid but not located entities, or {@code null} if the deck could not be found.
	 */
	@Override
	public GameDeck getDeck(Player player, String name) {
		var allDecks = await(Legacy.getAllDecks(player.getUserId()));
		var deck = allDecks.getDecksList().stream().filter(get -> get.getCollection().getName().equalsIgnoreCase(name)).findAny();
		return deck.map(decksGetResponse -> ModelConversions.getGameDeck(player.getUserId(), decksGetResponse)).orElse(null);
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
		try {
			lock.lock();
			// Update the client
			var listIterator = getClients().listIterator();
			while (listIterator.hasNext()) {
				var next = listIterator.next();
				if (next.getPlayerId() == client.getPlayerId() && client != next) {
					next.close(Promise.promise());
					closeables.remove(next);
					listIterator.set(client);
					next = client;
					closeables.add(client);
				}
				next.onConnectionStarted(getActivePlayer());
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
			getLogic().loseBothPlayers();
			endGame();
		} finally {
			releaseUsers();
		}
	}

	/**
	 * Returns once the handlers have successfully registered on this instance
	 */

	public Future<CompositeFuture> handlersReady() {
		if (!isRunning()) {
			throw new IllegalStateException("must call play(true)");
		}
		return all(registrationsReady.stream().map(Promise::future).toList());
	}

	/**
	 * The lock to prevent simultaneous editing of the game context from external sources.
	 *
	 * @return
	 */
	public Lock getLock() {
		return lock;
	}
}
