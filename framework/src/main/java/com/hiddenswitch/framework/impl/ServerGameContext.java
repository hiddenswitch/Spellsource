package com.hiddenswitch.framework.impl;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableAction1;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Editor;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.schema.spellsource.Tables;
import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.GamesDao;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.rpc.ClientToServerMessage;
import com.hiddenswitch.spellsource.rpc.ServerToClientMessage;
import io.opentracing.log.Fields;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.sync.Sync;
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
import net.demilich.metastone.game.targeting.Zones;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

import static io.vertx.ext.sync.Sync.await;
import static io.vertx.ext.sync.Sync.fiber;
import static java.util.stream.Collectors.toList;

/**
 * A networked game context from the server's point of view.
 * <p>
 * In addition to storing game state, this class also stores references to {@link Client} objects that (1) get notified
 * when game state changes and how, and (2) allow this class to {@link Behaviour#requestAction(GameContext, Player,
 * List)} and {@link Behaviour#mulligan(GameContext, Player, List)} over a network.
 * <p>
 */
public class ServerGameContext extends GameContext implements Server {
	private static final long CLOSE_TIMEOUT_MILLIS = 4000L;
	private static final long REGISTRATION_TIMEOUT = 4000L;
	private static Logger LOGGER = LoggerFactory.getLogger(ServerGameContext.class);
	public static final String WRITER_ADDRESS_PREFIX = "Games.writer.";
	public static final String READER_ADDRESS_PREFIX = "Games.reader.";

	private final transient ReentrantLock lock = new ReentrantLock();
	private final transient Queue<SuspendableAction1<ServerGameContext>> onGameEndHandlers = new ConcurrentLinkedQueue<>();
	private final transient Map<Integer, Promise<Client>> clientsReady = new ConcurrentHashMap<>();
	private final transient List<Client> clients = new ArrayList<>();
	private final transient Deque<Promise> registrationsReady = new ConcurrentLinkedDeque<>();
	private final transient Promise<Void> initialization = Promise.promise();
	private transient Long turnTimerId;
	private final Deque<Configuration> playerConfigurations = new ConcurrentLinkedDeque<>();
	private final Deque<Closeable> closeables = new ConcurrentLinkedDeque<>();
	private final String gameId;
	private final Deque<Trigger> gameTriggers = new ConcurrentLinkedDeque<>();
	private final Scheduler scheduler;
	private boolean isRunning = false;
	private final AtomicInteger notificationCounter = new AtomicInteger(0);
	private Long timerStartTimeMillis;
	private Long timerLengthMillis;

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
			spanBuilder.withTag("playerConfigurations." + i + ".userId", playerConfigurations.get(i).getUserId().toString());
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

			// Check if we should later enable editing
			var isEditable = Editor.isEditable(this);

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
				var inGameConsumer = Games.registerInGame(gameId, userId);
				closeables.add(inGameConsumer::unregister);
				Promise<Void> inGameRegistration = Promise.promise();
				inGameConsumer.completionHandler(inGameRegistration);
				registrationsReady.add(inGameRegistration);

				// When the game ends remove the fact that the user is in this game
				addEndGameHandler(ctx -> {
					Void t = Sync.await(inGameConsumer::unregister);
				});

				Closeable closeableBehaviour = null;
				// Bots simply forward their requests to a bot service provider, that executes the bot logic on a worker thread
				if (configuration.isBot()) {
					player.getAttributes().put(Attribute.AI_OPPONENT, true);
					var vertxContext = Vertx.currentContext();
					// TODO: this has to go to a blocking worker thread
					var behaviour = new GameStateValueBehaviour() {
						@Override
						@Suspendable
						public @Nullable GameAction requestAction(@NotNull GameContext context, @NotNull Player player, @NotNull List<GameAction> validActions) {
							if (vertxContext != null) {
								return await(vertxContext.executeBlocking(fut -> {
									fut.complete(super.requestAction(context, player, validActions));
								}, false));
							}
							return super.requestAction(context, player, validActions);
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
					// Connect to the websocket representing this user by connecting to its handler advertised on the event bus
					var bus = Vertx.currentContext().owner().eventBus();
					var consumer = toClient(userId, bus);
					// By using a publisher, we do not require that there be a working connection while sending
					var producer = toServer(userId, bus);
					consumer.setMaxBufferedMessages(Integer.MAX_VALUE);

					Promise<Void> registration = Promise.promise();
					consumer.completionHandler(registration);
					registrationsReady.add(registration);

					// We'll want to unregister and close these when this instance is disposed
					closeables.add(consumer::unregister);
					closeables.add(producer::close);

					// Create a client that handles game events and action/mulligan requests
					var client = new UnityClientBehaviour(this,
							new VertxScheduler(Vertx.currentContext().owner()),
							consumer.bodyStream(),
							producer,
							userId,
							configuration.getPlayerId(),
							configuration.getNoActivityTimeout());

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
					LOGGER.debug("closeables {}: closing client {}", gameId, finalCloseableBehaviour);
					finalCloseableBehaviour.close(fut);
				});
			}

			// Only enable editing if we actually got this far
			if (isEditable) {
				closeables.add(Editor.enableEditing(this));
			}
		} finally {
			span.finish();
		}
	}

	public static MessageProducer<ServerToClientMessage> toServer(String userId, EventBus bus) {
		return bus.publisher(getMessagesFromServerAddress(userId.toString()));
	}

	public static MessageConsumer<ClientToServerMessage> toClient(String userId, EventBus bus) {
		return bus.consumer(getMessagesFromClientAddress(userId.toString()));
	}

	public static void subscribeGame(ReadStream<ClientToServerMessage> request, WriteStream<ServerToClientMessage> response) {
		var context = (ContextInternal) Vertx.currentContext();
		var eventBus = context.owner().eventBus();
		var userId = Accounts.userId();
		request.handler(msg -> clientToServer(eventBus, userId, msg));

		var consumer = fromServer(eventBus, userId);
		consumer.setMaxBufferedMessages(Integer.MAX_VALUE);
		consumer.bodyStream().pipeTo(response);
	}

	@Suspendable
	public static void clientToServer(EventBus bus, String userId, ClientToServerMessage msg) {
		bus.publish(getMessagesFromClientAddress(userId), msg);
	}

	@Suspendable
	public static MessageConsumer<ServerToClientMessage> fromServer(EventBus bus, String userId) {
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
	@Suspendable
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
			var timeout = Math.min(Games.getDefaultNoActivityTimeout(), Games.getDefaultConnectionTime());
			if (!clientsReady.values().stream().allMatch(fut -> fut.future().isComplete())) {
				// If this is interrupted, it will bubble up to the general interrupt handler
				bothClientsReady = await(CompositeFuture.join(clientsReady.values().stream().map(Promise::future).collect(toList()))::onComplete, timeout);
			} else {
				bothClientsReady = Future.succeededFuture();
			}
			// One of the two clients did not connect in time, log a win for the player that connected
			if (bothClientsReady == null
					|| bothClientsReady.failed()) {
				// Mark the players that have not connected in time as destroyed, which in updateAndGetGameOver will eventually
				// lead to a double loss
				for (var entry : clientsReady.entrySet()) {
					if (!entry.getValue().future().isComplete()) {
						LOGGER.warn("init {}: Game prematurely ended because player id={} did not connect in {}ms", getGameId(), entry.getKey(), timeout);
						getLogic().concede(entry.getKey());
					}
				}
				isRunning = false;
				// resume() will check if the game is over
				return;
			}

			var executor = com.hiddenswitch.framework.Environment.queryExecutor();
			// update game status
			await(executor.execute(dsl -> dsl.update(Tables.GAMES)
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
				mulliganTimerId = scheduler.setTimer(timerLengthMillis, fiber(this::endMulligans));
			} else {
				LOGGER.debug("init {}: No mulligan timer set for game because all players are not human", getGameId());
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
			var simultaneousMulligans = Sync.await(CompositeFuture.join(mulligansActive.future(), mulligansNonActive.future())::onComplete);

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
	 * @param fork When {@code false}, blocks until the game is done. Otherwise, plays the game inside a {@link Fiber}
	 */
	@Suspendable
	public void play(boolean fork) {
		isRunning = true;
		if (fork) {
			// We're going to build this fiber with a huge stack
			if (getFiber() != null) {
				throw new UnsupportedOperationException("Cannot play with a fork twice!");
			}
			setFiber(new Fiber<>(String.format("ServerGameContext/fiber[%s]", getGameId()), Sync.getContextScheduler(), 512, () -> {
				var tracer = GlobalTracer.get();
				var span = tracer.buildSpan("ServerGameContext/play")
						.asChildOf(getSpanContext())
						.start();
				var scope = tracer.activateSpan(span);
				// Send the trace information to the clients since the game is now running
				var carrier = new BinaryCarrier();
				tracer.inject(span.context(), Format.Builtin.BINARY, carrier);

				/*
				getPlayerConfigurations().forEach(c -> Connection.writeStream(c.getUserId())
						.write(new Envelope()
								.added(new EnvelopeAdded()
										.spanContext(new SpanContext()
												.data(carrier.getBytes())))));
				*/

				try {
					LOGGER.debug("play {}: Starting forked game", gameId);
					super.play(false);
				} catch (Throwable throwable) {
					throwable.printStackTrace();
					var rootCause = Throwables.getRootCause(throwable);
					if (Strand.currentStrand().isInterrupted() || rootCause instanceof InterruptedException) {
						// Generally only an interrupt from endGame() is allowed to gracefully interrupt this daemon.
						span.log(ImmutableMap.of(
								Fields.EVENT, "interrupt",
								"graceful", true
						));
						// The game is already ended whenever the fiber is interrupted, there's no other place that the external user
						// is allowed to interrupt the fiber. So we don't need to call endGame here.
					} else {
						getTrace().setTraceErrors(true);
						try {
							endGame();
						} catch (Throwable endGameError) {
							Tracing.error(endGameError, span, false);
						}
						Tracing.error(throwable);
					}

				} finally {
					// Regardless of what happens that causes an event loop exception, make certain the user is released from their game
					var interrupted = Strand.interrupted();
					close();
					if (interrupted) {
						Strand.currentStrand().interrupt();
					}
					// Always set the trace
					span.setTag("trace", getTrace().dump());
					span.finish();
					scope.close();
				}
				return null;
			}));

			// Closing the context should interrupt the fiber
			// Not sure if this should be done at construction time.
			((ContextInternal) Vertx.currentContext()).addCloseHook(v -> {
				if (getFiber() != null && !getFiber().isInterrupted()) {
					getFiber().interrupt();
				}
				v.handle(Future.succeededFuture());
			});

			getFiber().setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
			getFiber().start();
			LOGGER.debug("play {}: Fiber started", gameId);
		} else {
			super.play();
		}
	}

	@Override
	@Suspendable
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
					turnTimerId = scheduler.setTimer(timerLengthMillis, fiber(ignored -> {
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
					}));
				} else {
					LOGGER.warn("startTurn {}: Timer set twice!", getGameId());
				}


			} else {
				timerLengthMillis = null;
				timerStartTimeMillis = null;
				LOGGER.debug("startTurn {}: Not setting timer because opponent is not human.", getGameId());
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
	@Suspendable
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
	@Suspendable
	private void endMulligans(long ignored) {
		for (var client : getClients()) {
			client.elapseAwaitingRequests();
		}
	}

	@Override
	@Suspendable
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

	@Suspendable
	private void updateClientsWithGameState() {
		var state = getGameStateCopy();
		for (var client : getClients()) {
			client.onUpdate(state);
		}
	}

	@Suspendable
	private void updateClientWithGameState(int playerId) {
		var state = getGameStateCopy();
		for (var client : getClients()) {
			if (client.getPlayerId() == playerId) {
				client.onUpdate(state);
			}
		}
	}

	@Override
	@Suspendable
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
	@Suspendable
	public void onNotificationDidFire(Notification event) {
		super.onNotificationDidFire(event);
		if (notificationCounter.decrementAndGet() == 0) {
			for (var client : getClients()) {
				client.lastEvent();
			}
		}
	}

	@Override
	@Suspendable
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

			if (host != null && Zones.PRIVATE.contains(host.getZone()) && host.getSourceCard() != null && !host.getSourceCard().getDesc().revealsSelf()) {
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
	@Suspendable
	public void onWillPerformGameAction(int playerId, GameAction action) {
		super.onWillPerformGameAction(playerId, action);
		var gameStateCopy = getGameStateCopy();
		for (var client : getClients()) {
			client.sendNotification(action, gameStateCopy);
		}
	}

	@Override
	@Suspendable
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
	@Suspendable
	protected void notifyPlayersGameOver() {
		for (var client : getClients()) {
			LOGGER.debug("notifyPlayersGameOver: notifying {}", client.getUserId());
			client.sendGameOver(getGameStateCopy(), getWinner());
		}
	}

	@Override
	public String toString() {
		return String.format("[ServerGameContext gameId=%s turn=%d]", getGameId(), getTurn());
	}

	@Override
	public String getGameId() {
		return gameId.toString();
	}

	@Override
	@Suspendable
	protected void endGame() {
		lock.lock();
		try {
			LOGGER.debug("endGame {}: calling end game", gameId);
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
			LOGGER.debug("endGame {}: called super.endGame", gameId);

			// No end of game handler should be called more than once, so we're removing them one-by-one as we're processing
			// them.
			SuspendableAction1<ServerGameContext> handler;
			while ((handler = onGameEndHandlers.poll()) != null) {
				try {
					handler.call(this);
				} catch (SuspendExecution | InterruptedException execution) {
					throw new RuntimeException(execution);
				}
			}

			LOGGER.debug("endGame {}: endGameHandlers run", gameId);

			// Now that the game is over, we have to stop processing the game event loop. We'll check that we're not in the
			// loop right now. If we are, we don't need to interrupt ourselves. Conceding, server shutdown and other lifecycle
			// issues will result, eventually, in calling end game outside this game's event loop. In that case, we'll
			// interrupt the event loop. Can the event loop itself be in the middle of calling endGame? In that case, the lock
			// prevents two endGames from being processed simultaneously, along with other important mutating events, like
			// other callbacks that may be processing player modifications to the game.
			if (getFiber() != null && !Strand.currentStrand().equals(getFiber())) {
				getFiber().interrupt();
				LOGGER.debug("endGame {}: interrupted fiber", gameId);
				setFiber(null);
			}
		} finally {
			releaseUsers();
			close();
			lock.unlock();
		}
	}

	@Suspendable
	public void releaseUsers() {
		// This should be a no-op in the new engineering of this
		/*
		if (Fiber.isCurrentFiber() && Vertx.currentContext() != null) {
			// everything else is handled in closeables
			SuspendableMap<String, String> queues;
			queues = Matchmaking.getUsersInQueues();

			for (var userId : getUserIds()) {
				queues.remove(userId.toString());
			}
		} else {
			throw new UnsupportedOperationException();
		}*/
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
	public void addEndGameHandler(SuspendableAction1<ServerGameContext> handler) {
		onGameEndHandlers.add(handler);
	}

	@Override
	@Suspendable
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
					Void res = await(h -> {
						Promise<Void> promise = Promise.promise();
						closeable.close(promise);
						promise.future().onComplete(h);
					}, CLOSE_TIMEOUT_MILLIS);
				} catch (Throwable any) {
					Tracing.error(any, span, false);
				} finally {
					iter.remove();
				}
			}
			LOGGER.debug("dispose {}: closers closed", gameId);
			super.close();
		} finally {
			span.finish();
		}
	}

	public Collection<Trigger> getGameTriggers() {
		return gameTriggers;
	}

	@Override
	@Suspendable
	public void onEmote(Client sender, int entityId, String message) {
		for (var client : getClients()) {
			client.sendEmote(entityId, message);
		}
	}

	@Override
	@Suspendable
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
	@Suspendable
	public GameDeck getDeck(Player player, String name) {
		var allDecks = await(Legacy.getAllDecks(player.getUserId()));
		var deck = allDecks.getDecksList().stream().filter(get -> get.getCollection().getName().equalsIgnoreCase(name)).findAny();
		return deck.map(decksGetResponse -> Games.getGameDeck(player.getUserId(), decksGetResponse)).orElse(null);
	}

	@Override
	@Suspendable
	public void onPlayerReady(Client client) {
		if (clientsReady.containsKey(client.getPlayerId())) {
			@SuppressWarnings("unchecked")
			var fut = clientsReady.get(client.getPlayerId());
			if (!fut.future().isComplete()) {
				fut.complete(client);
			}
		}
	}

	@Override
	@Suspendable
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
			return !getFiber().isInterrupted() && !getFiber().isTerminated();
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
	@Suspendable
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
	@Suspendable
	public void awaitReadyForConnections() {
		if (!isRunning()) {
			throw new IllegalStateException("must call play(true)");
		}
		var join = CompositeFuture.join(Stream.concat(
				registrationsReady.stream().map(Promise::future),
				Stream.of(initialization.future())
		).collect(toList()));
		var res = await(join::onComplete, REGISTRATION_TIMEOUT);
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
