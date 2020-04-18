package com.hiddenswitch.spellsource.net.impl.util;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableAction1;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.*;
import com.hiddenswitch.spellsource.net.impl.*;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.net.impl.server.BotsServiceBehaviour;
import com.hiddenswitch.spellsource.net.impl.server.Configuration;
import com.hiddenswitch.spellsource.net.impl.server.VertxScheduler;
import com.hiddenswitch.spellsource.net.models.GetCollectionResponse;
import com.hiddenswitch.spellsource.net.models.LogicGetDeckRequest;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.*;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.TouchingNotification;
import net.demilich.metastone.game.events.TriggerFired;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.TurnState;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.Zones;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

import static com.hiddenswitch.spellsource.net.impl.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;

/**
 * A networked game context from the server's point of view.
 * <p>
 * In addition to storing game state, this class also stores references to {@link Client} objects that (1) get notified
 * when game state changes and how, and (2) allow this class to {@link Behaviour#requestAction(GameContext, Player,
 * List)} and {@link Behaviour#mulligan(GameContext, Player, List)} over a network.
 * <p>
 * This class also automatically adds support for persistence effects written on cards using a {@link
 * PersistenceTrigger}.
 */
public class ServerGameContext extends GameContext implements Server {
	private static final long CLOSE_TIMEOUT_MILLIS = 4000L;
	private static final long REGISTRATION_TIMEOUT = 4000L;
	private static Logger LOGGER = LoggerFactory.getLogger(ServerGameContext.class);
	public static final String WRITER_ADDRESS_PREFIX = "Games/writer[";
	public static final String READER_ADDRESS_PREFIX = "Games/reader[";

	private final transient ReentrantLock lock = new ReentrantLock();
	private final transient Queue<SuspendableAction1<ServerGameContext>> onGameEndHandlers = new ConcurrentLinkedQueue<>();
	private final transient Map<Integer, Promise<Client>> clientsReady = new ConcurrentHashMap<>();
	private final transient List<Client> clients = new ArrayList<>();
	private final transient Deque<Promise> registrationsReady = new ConcurrentLinkedDeque<>();
	private final transient Promise<Void> initialization = Promise.promise();
	private transient TimerId turnTimerId;
	private final Deque<Configuration> playerConfigurations = new ConcurrentLinkedDeque<>();
	private final Deque<Closeable> closeables = new ConcurrentLinkedDeque<>();
	private final GameId gameId;
	private final Deque<Trigger> gameTriggers = new ConcurrentLinkedDeque<>();
	private final Scheduler scheduler;
	private boolean isRunning = false;
	private final AtomicInteger eventCounter = new AtomicInteger(0);
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
	public ServerGameContext(@NotNull GameId gameId, @NotNull Scheduler scheduler, @NotNull List<Configuration> playerConfigurations) {
		super();
		Tracer tracer = GlobalTracer.get();
		Tracer.SpanBuilder spanBuilder = tracer.buildSpan("ServerGameContext/init")
				.withTag("gameId", gameId.toString());
		for (int i = 0; i < playerConfigurations.size(); i++) {
			spanBuilder.withTag("playerConfigurations." + i + ".userId", playerConfigurations.get(i).getUserId().toString());
			spanBuilder.withTag("playerConfigurations." + i + ".deckId", playerConfigurations.get(i).getDeck().getDeckId());
			spanBuilder.withTag("playerConfigurations." + i + ".isBot", playerConfigurations.get(i).isBot());
		}
		Span span = spanBuilder.start();
		try (Scope s1 = tracer.activateSpan(span)) {
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
			// Mulligans should happen simultaneously
			setLogic(new NetworkedGameLogic());

			// Persistence effects mean cards that remember things that have happened to them in other games
			enablePersistenceEffects();
			enableTriggers();

			List<Integer> usedPlayerIds = new ArrayList<>(2);

			// Check if we should later enable editing
			var isEditable = Editor.isEditable(this);

			// Each configuration corresponds to a human or bot player
			// To accommodate spectators or multiple-controllers-per-game-player, use additional Client objects
			for (Configuration configuration : getPlayerConfigurations()) {
				UserId userId = configuration.getUserId();
				if (userId == null) {
					throw new IllegalArgumentException("userId cannot be null");
				}

				// Initialize the player objects
				// This will create an actual valid deck and player object
				GameDeck deck = (GameDeck) configuration.getDeck().clone();
				if (deck == null) {
					throw new IllegalArgumentException("deck cannot be null");
				}

				Player player = Player.forUser(userId.toString(), configuration.getPlayerId(), deck);
				if (usedPlayerIds.contains(configuration.getPlayerId())) {
					throw new IllegalArgumentException("playerId already used");
				}
				usedPlayerIds.add(configuration.getPlayerId());

				setPlayer(configuration.getPlayerId(), player);
				// ...and import all the attributes that might be specific to the queue and its rules (typically just the
				// DECK_ID and USER_ID attributes at the moment.
				for (Map.Entry<Attribute, Object> kv : configuration.getPlayerAttributes().entrySet()) {
					player.getAttributes().put(kv.getKey(), kv.getValue());
				}

				Closeable closeableBehaviour = null;
				// Bots simply forward their requests to a bot service provider, that executes the bot logic on a worker thread
				if (configuration.isBot()) {
					player.getAttributes().put(Attribute.AI_OPPONENT, true);
					BotsServiceBehaviour behaviour = new BotsServiceBehaviour(gameId);
					setBehaviour(configuration.getPlayerId(), behaviour);
					closeableBehaviour = behaviour;
					// Does not have a client representing it
				} else {
					// Connect to the websocket representing this user by connecting to its handler advertised on the event bus
					EventBus bus = Vertx.currentContext().owner().eventBus();
					MessageConsumer<ClientToServerMessage> consumer = toClient(userId, bus);
					// By using a publisher, we do not require that there be a working connection while sending
					MessageProducer<ServerToClientMessage> producer = toServer(userId, bus);
					consumer.setMaxBufferedMessages(Integer.MAX_VALUE);
					producer.setWriteQueueMaxSize(Integer.MAX_VALUE);

					Promise<Void> registration = Promise.promise();
					consumer.completionHandler(registration);
					registrationsReady.add(registration);

					// We'll want to unregister and close these when this instance is disposed
					closeables.add(consumer::unregister);
					closeables.add(fut -> {
						producer.close();
						fut.handle(Future.succeededFuture());
					});

					// Create a client that handles game events and action/mulligan requests
					UnityClientBehaviour client = new UnityClientBehaviour(this,
							new VertxScheduler(Vertx.currentContext().owner()),
							consumer.bodyStream(),
							producer,
							userId,
							configuration.getPlayerId(),
							configuration.getNoActivityTimeout());

					// This client too needs to be closed
					closeableBehaviour = client;

					// Once the game is disposed, there should be no more client instances referenced here
					closeables.add(fut -> {
						consumer.unregister();
						getClients().clear();
						CompositeFuture.join(getBehaviours().stream()
								.filter(Closeable.class::isInstance)
								.map(Closeable.class::cast).map(c -> {
									Promise<Void> promise = Promise.promise();
									c.close(promise);
									return promise.future();
								}).collect(toList()))
								.setHandler(h -> fut.handle(h.succeeded() ? Future.succeededFuture() : Future.failedFuture(h.cause())));
						setBehaviour(0, null);
						setBehaviour(1, null);
					});

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

				closeables.add(closeableBehaviour);
			}

			// Only enable editing if we actually got this far
			if (isEditable) {
				closeables.add(Editor.enableEditing(this));
			}
		} finally {
			span.finish();
		}
	}

	public static MessageProducer<ServerToClientMessage> toServer(UserId userId, EventBus bus) {
		return bus.publisher(getMessagesFromServerAddress(userId.toString()));
	}

	public static MessageConsumer<ClientToServerMessage> toClient(UserId userId, EventBus bus) {
		return bus.consumer(getMessagesFromClientAddress(userId.toString()));
	}

	/**
	 * Sends game traffic over the {@link Connection} messaging system.
	 *
	 * @return A way to disconnect the machinery that makes the messaging happen for this particular server instance.
	 */
	public static Closeable handleConnections() {
		Set<MessageConsumer<ServerToClientMessage>> consumers = new ConcurrentHashSet<>();

		// Set up the connectivity for the user.
		Connection.SetupHandler handler = (connection, fut) -> {
			Vertx vertx = Vertx.currentContext().owner();
			EventBus bus = vertx.eventBus();
			String userId = connection.userId();

			// Game messages
			MessageConsumer<ServerToClientMessage> consumer = fromServer(bus, userId);
			consumer.setMaxBufferedMessages(Integer.MAX_VALUE);
			consumers.add(consumer);


			// Read messages from the client and send them to the server processing this request.
			connection.handler(suspendableHandler(env -> {
				if (env.getGame() != null && env.getGame().getClientToServer() != null) {
					ClientToServerMessage msg = env.getGame().getClientToServer();
					if (msg.getMessageType() == MessageType.FIRST_MESSAGE) {
						LOGGER.debug("handleConnections connection.handler {}: Received first message from socket, now sending", connection.userId());
					}
					clientToServer(bus, userId, msg);
				}
			}));

			// Write messages from server game contexts to the client
			consumer.bodyStream().handler(serverToClient -> {
				try {
					connection.write(new Envelope().game(
							new EnvelopeGame().serverToClient(serverToClient)));
				} catch (IllegalStateException ex) {
					// TODO: We might want to signal to the server that the message it tried to send failed.
					LOGGER.warn("handleConnections {}: Socket disconnected for message {}", userId, serverToClient);
				}
			});

			// When the user disconnects, make sure to remove these event bus registrations
			connection.endHandler(suspendableHandler(v1 -> {
				try {
					consumers.remove(consumer);
					consumer.unregister();
				} catch (Throwable any) {
					LOGGER.error("handleConnections: ", any);
				}
			}));

			consumer.completionHandler(fut);
		};

		// Handle the connections here.
		Connection.connected(handler);

		// Remove all remaining handlers
		return completionHandler -> {
			Connection.getHandlers().remove(handler);

			CompositeFuture.all(consumers.stream().map(mc -> {
				Promise<Void> promise = Promise.promise();
				mc.unregister(promise);
				return promise.future();
			}).collect(toList())).setHandler(v1 -> {
				if (v1.succeeded()) {
					completionHandler.handle(Future.succeededFuture());
				} else {
					completionHandler.handle(Future.failedFuture(v1.cause()));
				}
			});
		};
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
		return READER_ADDRESS_PREFIX + userId + "]";
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
		return WRITER_ADDRESS_PREFIX + userId + "]";
	}

	/**
	 * Enables this match to track persistence effects.
	 *
	 * @see PersistenceTrigger for more about how this method is used.
	 */
	private void enablePersistenceEffects() {
		getGameTriggers().add(new PersistenceTrigger(this, this.gameId));
	}

	/**
	 * Enables this match to use custom networked triggers
	 */
	private void enableTriggers() {
		for (com.hiddenswitch.spellsource.net.impl.Trigger trigger : Spellsource.spellsource().getGameTriggers().values()) {
			Map<SpellArg, Object> arguments = new SpellDesc(DelegateSpell.class);
			arguments.put(SpellArg.NAME, trigger.getSpellId());
			SpellDesc spell = new SpellDesc(arguments);
			Enchantment enchantment = new Enchantment(trigger.getEventTriggerDesc().create(), spell);
			enchantment.setOwner(0);
			getGameTriggers().add(enchantment);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return A simultaneous mulligan game logic
	 */
	@Override
	public NetworkedGameLogic getLogic() {
		return (NetworkedGameLogic) super.getLogic();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Simultaneously processes mulligans and awaits until all human players have sent a "FIRST_MESSAGE."
	 */
	@Override
	@Suspendable
	public void init() {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("ServerGameContext/init")
				.asChildOf(tracer.activeSpan())
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			LOGGER.trace("init {}: Game starts {} {} vs {} {}", getGameId(), getPlayer1().getName(), getPlayer1().getUserId(), getPlayer2().getName(), getPlayer2().getUserId());
			startTrace();
			int startingPlayerId = getLogic().determineBeginner(PLAYER_1, PLAYER_2);
			getEnvironment().put(Environment.STARTING_PLAYER, startingPlayerId);
			setActivePlayerId(startingPlayerId);

			// Make sure the players are initialized before sending the original player updates.
			getLogic().initializePlayerAndMoveMulliganToSetAside(PLAYER_1, startingPlayerId == PLAYER_1);
			getLogic().initializePlayerAndMoveMulliganToSetAside(PLAYER_2, startingPlayerId == PLAYER_2);

			initialization.complete();
			Future bothClientsReady;
			long timeout = Math.min(Games.getDefaultNoActivityTimeout(), Games.getDefaultConnectionTime());
			if (!clientsReady.values().stream().allMatch(fut -> fut.future().isComplete())) {
				// If this is interrupted, it will bubble up to the general interrupt handler
				bothClientsReady = awaitResult(CompositeFuture.join(clientsReady.values().stream().map(Promise::future).collect(toList()))::setHandler, timeout);
			} else {
				bothClientsReady = Future.succeededFuture();
			}
			// One of the two clients did not connect in time, log a win for the player that connected
			if (bothClientsReady == null
					|| bothClientsReady.failed()) {
				// Mark the players that have not connected in time as destroyed, which in updateAndGetGameOver will eventually
				// lead to a double loss
				for (Map.Entry<Integer, Promise<Client>> entry : clientsReady.entrySet()) {
					if (!entry.getValue().future().isComplete()) {
						LOGGER.warn("init {}: Game prematurely ended because player id={} did not connect in {}ms", getGameId(), entry.getKey(), timeout);
						getLogic().concede(entry.getKey());
					}
				}
				isRunning = false;
				// resume() will check if the game is over
				return;
			}

			// When players reconnect, we don't want them to trigger these futures anymore
			clientsReady.clear();

			// Signal to the game context has made everything have valid IDs.
			getLogic().contextReady();

			for (Client client : getClients()) {
				client.onConnectionStarted(getActivePlayer());
			}

			// Record the time that we started the game in system milliseconds, in case a card wants to use this for an event-based thing.
			getPlayers().forEach(p -> p.getAttributes().put(Attribute.GAME_START_TIME_MILLIS, (int) (System.currentTimeMillis() % Integer.MAX_VALUE)));

			// Set the mulligan timer
			final TimerId mulliganTimerId;
			if (getBehaviours().stream().allMatch(Behaviour::isHuman)) {
				// Only two human players will get timers
				timerLengthMillis = getLogic().getMulliganTimeMillis();
				timerStartTimeMillis = System.currentTimeMillis();
				mulliganTimerId = scheduler.setTimer(timerLengthMillis, suspendableHandler(this::endMulligans));
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

			List<Card> firstHandActive = getActivePlayer().getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
			List<Card> firstHandNonActive = getNonActivePlayer().getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
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
			CompositeFuture simultaneousMulligans = awaitResult(CompositeFuture.join(mulligansActive.future(), mulligansNonActive.future())::setHandler);

			// If we got this far, we should cancel the time
			if (mulliganTimerId != null) {
				scheduler.cancelTimer(mulliganTimerId);
			}

			// The timer will have completed the mulligans, that's why we don't timeout simultaneous mulligans
			if (simultaneousMulligans == null || simultaneousMulligans.failed()) {
				// An error occurred
				throw new VertxException(simultaneousMulligans == null ? new TimeoutException() : simultaneousMulligans.cause());
			}

			List<Card> discardedCardsActive = mulligansActive.future().result();
			List<Card> discardedCardsNonActive = mulligansNonActive.future().result();
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
				Tracer tracer = GlobalTracer.get();
				Span span = tracer.buildSpan("ServerGameContext/play")
						.asChildOf(getSpanContext())
						.start();
				Scope scope = tracer.activateSpan(span);
				// Send the trace information to the clients since the game is now running
				BinaryCarrier carrier = new BinaryCarrier();
				tracer.inject(span.context(), Format.Builtin.BINARY, carrier);

				getPlayerConfigurations().forEach(c -> Connection.writeStream(c.getUserId())
						.write(new Envelope()
								.added(new EnvelopeAdded()
										.spanContext(new com.hiddenswitch.spellsource.client.models.SpanContext()
												.data(carrier.getBytes())))).end());

				try {
					LOGGER.debug("play {}: Starting forked game", gameId);
					super.play(false);
				} catch (Throwable throwable) {
					Throwable rootCause = Throwables.getRootCause(throwable);
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
					// Always set the trace
					span.setTag("trace", getTrace().dump());
					span.finish();
					scope.close();
					UserId[] userIds = getPlayerConfigurations().stream().map(Configuration::getUserId).toArray(UserId[]::new);
					Vertx.currentContext().runOnContext((ctx) -> ServerGameContext.releaseUsers(gameId, userIds));
					dispose();
				}
				return null;
			}));

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

			for (Behaviour behaviour : getBehaviours()) {
				if (behaviour instanceof HasElapsableTurns) {
					((HasElapsableTurns) behaviour).setElapsed(false);
				}
			}

			if (getBehaviours().get(getNonActivePlayerId()).isHuman()) {
				timerLengthMillis = (long) getTurnTimeForPlayer(getActivePlayerId());
				timerStartTimeMillis = System.currentTimeMillis();

				if (turnTimerId == null) {
					turnTimerId = scheduler.setTimer(timerLengthMillis, suspendableHandler(ignored -> {
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
			GameState state = new GameState(this, TurnState.TURN_IN_PROGRESS);
			for (Client client : getClients()) {
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
			for (Client client : getClients()) {
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
		for (Client client : getClients()) {
			client.elapseAwaitingRequests();
		}
	}

	@Override
	@Suspendable
	public void resume() {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("ServerGameContext/resume")
				.asChildOf(tracer.activeSpan())
				.start();
		Scope scope = tracer.activateSpan(span);
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
		GameState state = getGameStateCopy();
		for (Client client : getClients()) {
			client.onUpdate(state);
		}
	}

	@Suspendable
	private void updateClientWithGameState(int playerId) {
		GameState state = getGameStateCopy();
		for (Client client : getClients()) {
			if (client.getPlayerId() == playerId) {
				client.onUpdate(state);
			}
		}
	}

	@Override
	@Suspendable
	public void fireGameEvent(GameEvent gameEvent) {
		eventCounter.incrementAndGet();
		// Do not build game state for events the client is not interested in
		if (gameEvent.isClientInterested()) {
			GameState gameStateCopy = getGameStateCopy();
			for (Client client : getClients()) {
				client.sendNotification(gameEvent, gameStateCopy);
			}
		}
		super.fireGameEvent(gameEvent, new ArrayList<>(gameTriggers));
		if (eventCounter.decrementAndGet() == 0) {
			for (Client client : getClients()) {
				client.lastEvent();
			}
		}
	}

	@Override
	@Suspendable
	public void onEnchantmentFired(Enchantment trigger) {
		super.onEnchantmentFired(trigger);

		TriggerFired triggerFired = new TriggerFired(this, trigger);
		final GameState gameStateCopy = getGameStateCopy();

		// If the trigger is in a private place, do not fire it for the public player
		if (trigger.getHostReference() != null) {
			Entity host = getEntities()
					.filter(e -> e.getId() == trigger.getHostReference().getId())
					.findFirst()
					.orElse(null);

			if (host != null && Zones.PRIVATE.contains(host.getZone()) && host.getSourceCard() != null && !host.getSourceCard().getDesc().revealsSelf()) {
				int owner = host.getOwner();
				Client client = getClient(owner);

				// Don't send spurious, private notifications to bot players / players not represented by clients
				if (client != null) {
					client.sendNotification(triggerFired, gameStateCopy);
				}
				return;
			}
		}

		for (Client client : getClients()) {
			client.sendNotification(triggerFired, gameStateCopy);
		}
	}

	@Override
	@Suspendable
	public void onWillPerformGameAction(int playerId, GameAction action) {
		super.onWillPerformGameAction(playerId, action);
		GameState gameStateCopy = getGameStateCopy();
		for (Client client : getClients()) {
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
		for (Client client : getClients()) {
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
			isRunning = false;
			// Close the inbound messages from the client, they should be ignored by these client instances
			// This way, a user doesn't accidentally trigger some other kind of processing that's only going to be interrupted
			// later. However, this does block emote processing, which is unfortunate.
			for (Client client : getClients()) {
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

			// Now that the game is over, we have to stop processing the game event loop. We'll check that we're not in the
			// loop right now. If we are, we don't need to interrupt ourselves. Conceding, server shutdown and other lifecycle
			// issues will result, eventually, in calling end game outside this game's event loop. In that case, we'll
			// interrupt the event loop. Can the event loop itself be in the middle of calling endGame? In that case, the lock
			// prevents two endGames from being processed simultaneously, along with other important mutating events, like
			// other callbacks that may be processing player modifications to the game.
			if (getFiber() != null && !Strand.currentStrand().equals(getFiber())) {
				getFiber().interrupt();
				setFiber(null);
			}
		} finally {
			releaseUsers();
			dispose();
			lock.unlock();
		}
	}

	@Suspendable
	public void releaseUsers() {
		GameId gameId = new GameId(getGameId());
		if (Fiber.isCurrentFiber() && Vertx.currentContext() != null) {
			SuspendableMap<UserId, GameId> games = null;
			try {
				games = Games.getUsersInGames();
			} catch (SuspendExecution suspendExecution) {
				throw new RuntimeException(suspendExecution);
			}

			for (UserId userId : getUserIds()) {
				games.remove(userId, gameId);
			}
		} else {
			UserId[] userIds = getUserIds().toArray(new UserId[0]);
			releaseUsers(gameId, userIds);
		}
	}

	private static void releaseUsers(GameId gameId, @NotNull UserId[] userIds) {
		Tracer tracer = GlobalTracer.get();
		Tracer.SpanBuilder spanBuilder = tracer.buildSpan("ServerGameContext/releaseUsers")
				.withTag("gameId", gameId.toString());

		for (int i = 0; i < userIds.length; i++) {
			spanBuilder.withTag("userId." + i, userIds[i].toString());
		}
		Span span = spanBuilder.start();

		try (Scope s1 = tracer.activateSpan(span)) {
			Context context = Vertx.currentContext();
			if (context == null) {
				return;
			}
			context.owner().sharedData().<UserId, GameId>getAsyncMap(Games.GAMES_PLAYERS_MAP, res -> {
				try (Scope s2 = tracer.activateSpan(span)) {
					if (res.failed()) {
						Tracing.error(res.cause());
						return;
					}

					for (UserId userId : userIds) {
						res.result().removeIfPresent(userId, gameId, Promise.promise());
					}
				} finally {
					span.finish();
				}
			});
		}

	}

	/**
	 * Gets the user IDs of the players in this game context. Includes the AI player
	 *
	 * @return A list of user IDs.
	 */
	private List<UserId> getUserIds() {
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
	public void dispose() {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("ServerGameContext/dispose")
				.asChildOf(getSpanContext())
				.withTag("gameId", getGameId())
				.start();

		try (Scope s1 = tracer.activateSpan(span)) {
			Iterator<Closeable> iter = closeables.iterator();
			while (iter.hasNext()) {
				try {
					Closeable closeable = iter.next();
					Void res = awaitResult(closeable::close, CLOSE_TIMEOUT_MILLIS);
				} catch (Throwable any) {
					Tracing.error(any, span, false);
				} finally {
					iter.remove();
				}
			}
			super.dispose();
		} finally {
			span.finish();
		}
	}

	public Collection<Trigger> getGameTriggers() {
		return gameTriggers;
	}

	@Override
	@Suspendable
	public void onEmote(Client sender, int entityId, Emote.MessageEnum message) {
		for (Client client : getClients()) {
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
		TouchingNotification touch = new TouchingNotification(sender.getPlayerId(), entityId, touching);

		for (Client client : getClients()) {
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
		GetCollectionResponse response = Logic.getDeck(new LogicGetDeckRequest()
				.withUserId(new UserId(player.getUserId()))
				.withDeckName(name));

		if (response.equals(GetCollectionResponse.empty())) {
			return null;
		}

		return response.asDeck(player.getUserId());
	}

	@Override
	@Suspendable
	public void onPlayerReady(Client client) {
		if (clientsReady.containsKey(client.getPlayerId())) {
			@SuppressWarnings("unchecked")
			Promise<Client> fut = clientsReady.get(client.getPlayerId());
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
			ListIterator<Client> listIterator = getClients().listIterator();
			while (listIterator.hasNext()) {
				Client next = listIterator.next();
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
		for (Client client : getClients()) {
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
		CompositeFuture join = CompositeFuture.join(Stream.concat(
				registrationsReady.stream().map(Promise::future),
				Stream.of(initialization.future())
		).collect(toList()));
		CompositeFuture res = awaitResult(join::setHandler, REGISTRATION_TIMEOUT);
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
