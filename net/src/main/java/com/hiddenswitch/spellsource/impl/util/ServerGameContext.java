package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableAction1;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.common.*;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.TimerId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.server.BotsServiceBehaviour;
import com.hiddenswitch.spellsource.impl.server.Configuration;
import com.hiddenswitch.spellsource.impl.server.VertxScheduler;
import com.hiddenswitch.spellsource.models.GetCollectionResponse;
import com.hiddenswitch.spellsource.models.LogicGetDeckRequest;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.Json;
import io.vertx.core.streams.Pump;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.web.RoutingContext;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.TouchingNotification;
import net.demilich.metastone.game.events.TriggerFired;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.Zones;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.logic.TurnState;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitEvent;
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
	private static Logger LOGGER = LoggerFactory.getLogger(ServerGameContext.class);
	public static final String WRITER_ADDRESS_PREFIX = "Games::writer[";
	public static final String READER_ADDRESS_PREFIX = "Games::reader[";

	private final transient ReentrantLock lock = new ReentrantLock();
	private final transient Queue<SuspendableAction1<ServerGameContext>> onGameEndHandlers = new ConcurrentLinkedQueue<>();
	private final transient Map<Integer, Future<Client>> clientsReady = new HashMap<>();
	private final transient List<Client> clients = new ArrayList<>();
	private final transient List<Future> registrationsReady = new ArrayList<>();
	private final List<Configuration> playerConfigurations = new ArrayList<>();
	private final List<Closeable> closeables = new ArrayList<>();
	private final GameId gameId;
	private final List<Trigger> gameTriggers = new ArrayList<>();
	private final Scheduler scheduler;
	private boolean isRunning = false;
	private final AtomicInteger eventCounter = new AtomicInteger(0);
	private transient Fiber<Void> fiber;
	private Long timerStartTimeMillis;
	private Long timerLengthMillis;
	private transient TimerId turnTimerId;
	private boolean didExpire;

	/**
	 * {@inheritDoc}
	 * <p>
	 *
	 * @param gameId               The game ID that corresponds to this game context.
	 * @param scheduler            The {@link Scheduler} instance to use for scheduling game events.
	 * @param playerConfigurations The information about the players who will be connecting / playing this game context
	 */
	public ServerGameContext(GameId gameId, Scheduler scheduler, List<Configuration> playerConfigurations) {
		super();

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

		// Each configuration corresponds to a human or bot player
		// To accommodate spectators or multiple-controllers-per-game-player, use additional Client objects
		for (Configuration configuration : getPlayerConfigurations()) {
			UserId userId = configuration.getUserId();

			// Initialize the player objects
			// This will create an actual valid deck and player object
			Player player = Player.forUser(userId.toString(), configuration.getPlayerId(), (GameDeck) configuration.getDeck());
			setPlayer(configuration.getPlayerId(), player);
			// ...and import all the attributes that might be specific to the queue and its rules (typically just the
			// DECK_ID and USER_ID attributes at the moment.
			for (Map.Entry<Attribute, Object> kv : configuration.getPlayerAttributes().entrySet()) {
				player.getAttributes().put(kv.getKey(), kv.getValue());
			}

			// Bots simply forward their requests to a bot service provider, that executes the bot logic on a worker thread
			if (configuration.isBot()) {
				player.getAttributes().put(Attribute.AI_OPPONENT, true);
				setBehaviour(configuration.getPlayerId(), new BotsServiceBehaviour());
				// Does not have a client representing it
			} else {
				// Connect to the websocket representing this user by connecting to its handler advertised on the event bus
				EventBus bus = Vertx.currentContext().owner().eventBus();
				MessageConsumer<Buffer> consumer = bus.consumer(getMessagesFromClientAddress(userId.toString()));
				// By using a publisher, we do not require that there be a working connection while sending
				MessageProducer<Buffer> producer = bus.publisher(getMessagesFromServerAddress(userId.toString()));

				Future<Void> registration = Future.future();
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
				closeables.add(client);

				// The client implements the behaviour interface since it is supposed to be able to respond to requestAction
				// and mulligan calls
				setBehaviour(configuration.getPlayerId(), client);
				// However, unlike a behaviour, there can be multiple clients per player ID. This will facilitate spectating.
				getClients().add(client);
				// This future will be completed when FIRST_MESSAGE is received from the client. And the actual unity client
				// will only be notified to send this message when the constructor finishes.
				Future<Client> fut = Future.future();
				clientsReady.put(configuration.getPlayerId(), fut);
			}
		}
	}

	/**
	 * Sends game traffic over the {@link com.hiddenswitch.spellsource.Connection} messaging system.
	 *
	 * @return A way to disconnect the machinery that makes the messaging happen for this particular server instance.
	 */
	public static Closeable handleConnections() {
		Vertx vertx = Vertx.currentContext().owner();
		EventBus bus = vertx.eventBus();
		Set<MessageConsumer<Buffer>> consumers = new ConcurrentHashSet<>();
		Set<MessageProducer<Buffer>> producers = new ConcurrentHashSet<>();

		// Set up the connectivity for the user.
		Handler<Connection> handler = connection -> {
			String userId = connection.userId();
			MessageConsumer<Buffer> consumer = bus.consumer(getMessagesFromServerAddress(userId));
			MessageProducer<Buffer> producer = bus.publisher(getMessagesFromClientAddress(userId));
			consumers.add(consumer);
			producers.add(producer);

			// Read messages from the client and send them to the server processing this request.
			connection.handler(env -> {
				if (env.getGame() != null && env.getGame().getClientToServer() != null) {
					if (env.getGame().getClientToServer().getMessageType() == MessageType.FIRST_MESSAGE) {
						LOGGER.debug("handleConnections connection.handler {}: Received first message from socket, now sending", connection.userId());
					}
					producer.send(Buffer.buffer(Json.encode(env.getGame().getClientToServer())));
				}
			});

			// Write messages from the server to the game body.
			consumer.bodyStream().handler(serverToClientBuf -> {
				try {
					connection.write(new Envelope().game(
							new EnvelopeGame().serverToClient(Json.decodeValue(serverToClientBuf, ServerToClientMessage.class))));
				} catch (IllegalStateException ex) {
					// TODO: We might want to signal to the server that the message it tried to send failed.
					LOGGER.warn("handleConnections {}: Socket disconnected for message {}", userId, serverToClientBuf);
				}
			});

			// When the user disconnects, make sure to remove these event bus registrations
			connection.endHandler(suspendableHandler(v1 -> {
				try {
					consumers.remove(consumer);
					producers.remove(producer);
					producer.close();
					consumer.unregister();
				} catch (Throwable any) {
					LOGGER.error("handleConnections: ", any);
				}

			}));
		};

		// Handle the connections here.
		Connection.connected(handler);

		// Remove all remaining handlers
		return completionHandler -> {
			Connection.getHandlers().remove(handler);
			for (MessageProducer<Buffer> producer : producers) {
				producer.close();
			}

			CompositeFuture.all(consumers.stream().map(mc -> {
				Future<Void> future = Future.future();
				mc.unregister(future);
				return future;
			}).collect(toList())).setHandler(v1 -> {
				if (v1.succeeded()) {
					completionHandler.handle(Future.succeededFuture());
				} else {
					completionHandler.handle(Future.failedFuture(v1.cause()));
				}
			});
		};
	}

	/**
	 * Creates a web socket handler to route game traffic (actions, game states, etc.) between the HTTP/WS client this
	 * handler will create and the appropriate event bus address for game traffic.
	 *
	 * @return A suspendable handler.
	 * @deprecated Game traffic should come across {@link #handleConnections()} instead.
	 */
	@Deprecated
	public static Handler<RoutingContext> createWebSocketHandler() {
		// Eventually this will be migrated to the Connection / envelope messaging scheme.
		return context -> {
			String userId = Accounts.userId(context);
			Vertx vertx = context.vertx();
			EventBus bus = vertx.eventBus();

			LOGGER.debug("createWebSocketHandler: Creating WebSocket to EventBus mapping for userId {}", userId);

			ServerWebSocket socket;
			HttpServerRequest request = context.request();

			try {
				socket = request.upgrade();
			} catch (IllegalStateException ex) {
				LOGGER.error("createWebSocketHandler: Failed to upgrade with error: {}. Request={}", new ToStringBuilder(request)
						.append("headers", request.headers().entries())
						.append("uri", request.uri())
						.append("userId", userId).toString());
				throw ex;
			}

			MessageConsumer<Buffer> consumer = bus.consumer(getMessagesFromServerAddress(userId));
			MessageProducer<Buffer> producer = bus.publisher(getMessagesFromClientAddress(userId));
			// This pumps messages to and from the event bus, but inside a fiber
			Pump socketToEventBus = new SuspendablePump<>(socket, producer, Integer.MAX_VALUE).start();
			Pump eventBusToSocket = new SuspendablePump<>(consumer.bodyStream(), socket, Integer.MAX_VALUE).start();

			socket.closeHandler(suspendableHandler(disconnected -> {
				try {
					// Include a reference in this lambda to ensure the pump lasts
					eventBusToSocket.numberPumped();
					producer.close();
					consumer.unregister();
					socketToEventBus.stop();
				} catch (Throwable throwable) {
					LOGGER.warn("createWebSocketHandler socket closeHandler: Failed to clean up resources from a user {} socket due to an exception {}", userId, throwable);
				}
			}));

		};
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
		for (com.hiddenswitch.spellsource.impl.Trigger trigger : Spellsource.spellsource().getGameTriggers().values()) {
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
		LOGGER.trace("init {}: Game starts {} {} vs {} {}", getGameId(), getPlayer1().getName(), getPlayer1().getUserId(), getPlayer2().getName(), getPlayer2().getUserId());
		startTrace();
		int startingPlayerId = getLogic().determineBeginner(PLAYER_1, PLAYER_2);
		setActivePlayerId(startingPlayerId);

		// Await both clients ready for 10s
		Future bothClientsReady;
		if (!clientsReady.values().stream().allMatch(Future::isComplete)) {
			bothClientsReady = awaitResult(CompositeFuture.join(new ArrayList<>(clientsReady.values()))::setHandler, 10000L);
		} else {
			bothClientsReady = Future.succeededFuture();
		}
		// One of the two clients did not connect in time, log a win for the player that connected
		if (bothClientsReady == null
				|| bothClientsReady.failed()) {
			// Mark the players that have not connected in time as destroyed, which in updateAndGetGameOver will eventually
			// lead to a double loss
			for (Map.Entry<Integer, Future<Client>> entry : clientsReady.entrySet()) {
				if (!entry.getValue().isComplete()) {
					LOGGER.warn("init {}: Game prematurely ended because player {} did not connect in 25s", getGameId(), entry.getKey());
					getLogic().concede(entry.getKey());
				}
			}
			isRunning = false;
			// resume() will check if the game is over
			return;
		}

		// When players reconnect, we don't want them to trigger these futures anymore
		clientsReady.clear();

		// Make sure the players are initialized before sending the original player updates.
		getLogic().initializePlayerAndMoveMulliganToSetAside(PLAYER_1, startingPlayerId == PLAYER_1);
		getLogic().initializePlayerAndMoveMulliganToSetAside(PLAYER_2, startingPlayerId == PLAYER_2);
		LOGGER.trace("init {}: Players initialized", getGameId());

		// Signal to the game context has made everything have valid IDs.
		getLogic().contextReady();

		LOGGER.trace("init {}: Updating active players", getGameId());
		for (Client client : getClients()) {
			client.onActivePlayer(getActivePlayer());
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
		updateClientsWithGameState();

		// Simultaneous mulligans now
		Future<List<Card>> mulligansActive = Future.future();
		Future<List<Card>> mulligansNonActive = Future.future();

		List<Card> firstHandActive = getActivePlayer().getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
		List<Card> firstHandNonActive = getNonActivePlayer().getSetAsideZone().stream().map(Entity::getSourceCard).collect(toList());
		getBehaviours().get(getActivePlayerId()).mulliganAsync(this, getActivePlayer(), firstHandActive, mulligansActive::complete);
		getBehaviours().get(getNonActivePlayerId()).mulliganAsync(this, getNonActivePlayer(), firstHandNonActive, mulligansNonActive::complete);

		// If this is interrupted, it'll bubble up to the general interrupt handler
		CompositeFuture simultaneousMulligans = awaitResult(CompositeFuture.join(mulligansActive, mulligansNonActive)::setHandler);

		// If we got this far, we should cancel the time
		if (mulliganTimerId != null) {
			scheduler.cancelTimer(mulliganTimerId);
		}

		// The timer will have completed the mulligans, that's why we don't timeout simultaneous mulligans
		if (simultaneousMulligans == null || simultaneousMulligans.failed()) {
			// An error occurred
			LOGGER.error("init {}: The mulligan phase ended prematurely", getGameId());
		}

		List<Card> discardedCardsActive = mulligansActive.result();
		List<Card> discardedCardsNonActive = mulligansNonActive.result();
		getLogic().handleMulligan(getActivePlayer(), true, discardedCardsActive);
		getLogic().handleMulligan(getNonActivePlayer(), false, discardedCardsNonActive);

		traceMulligans(mulligansActive.result(), mulligansNonActive.result());

		try {
			startGame();
		} catch (NullPointerException | IndexOutOfBoundsException playerNull) {
			LOGGER.error("init {}: Game already ended during mulligan phase.", getGameId());
		}

	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Starts playing in a {@link Fiber} (i.e., {@link #play(boolean)} is called with {@code true}).
	 */
	@Override
	@Suspendable
	public void play() {
		play(false);
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
			if (fiber != null) {
				throw new UnsupportedOperationException("Cannot play with a fork twice!");
			}
			fiber = new Fiber<>(String.format("ServerGameContext::fiber[%s]", getGameId()), Sync.getContextScheduler(), 512, () -> {
				try {
					LOGGER.debug("play {}: Starting forked game", gameId);
					super.play();
				} catch (VertxException interrupted) {
					// Generally only an interrupt from endGame() is allowed to gracefully interrupt this daemon.
					if (Strand.currentStrand().isInterrupted() || interrupted.getCause() instanceof InterruptedException) {
						LOGGER.debug("play {}: Interrupted gracefully", getGameId());
					} else {
						LOGGER.error("play {}: Possibly interrupted by {}", getGameId(), interrupted.getMessage(), interrupted);
					}
					// The game is already ended whenever the fiber is interrupted, there's no other place that the external user
					// is allowed to interrupt the fiber.
				} catch (RuntimeException other) {
					LOGGER.error("play {}: An error occurred and we're going to attempt ending the game normally.", getGameId(), other);
					try {
						endGame();
					} catch (Throwable endGameError) {
						LOGGER.error("play {}: Ending the game threw an exception.", getGameId(), endGameError);
						// TODO: Deal with any other issues
					}
				} finally {
					// Regardless of what happens that causes an event loop exception, make certain the user is released from their game
					UserId[] userIds = getPlayerConfigurations().stream().map(Configuration::getUserId).toArray(UserId[]::new);
					Vertx.currentContext().runOnContext((ctx) -> ServerGameContext.releaseUsers(gameId, userIds));
				}
				return null;
			});

			fiber.start();
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
				if (behaviour instanceof UnityClientBehaviour) {
					((UnityClientBehaviour) behaviour).setElapsed(false);
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
						Client client = getClient(playerId);
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
		if (!isRunning()) {
			return;
		}
		while (!updateAndGetGameOver()) {
			if (!isRunning()) {
				break;
			}
			startTurn(getActivePlayerId());
			while (takeActionInTurn()) {
				if (!isRunning()) {
					break;
				}
			}
			if (getTurn() > GameLogic.TURN_LIMIT) {
				break;
			}
		}
		endGame();
	}

	private int getTurnTimeForPlayer(int activePlayerId) {
		return getLogic().getTurnTimeMillis(activePlayerId);
	}

	@Override
	@Suspendable
	protected void onGameStateChanged() {
		updateClientsWithGameState();
	}

	@Suspendable
	private void updateClientsWithGameState() {
		GameState state = getGameStateCopy();
		for (Client client : getClients()) {
			client.onUpdate(state);
		}
	}

	@Override
	@Suspendable
	public void fireGameEvent(GameEvent gameEvent) {
		eventCounter.incrementAndGet();
		final GameState gameStateCopy = getGameStateCopy();
		for (Client client : getClients()) {
			client.sendNotification(gameEvent, gameStateCopy);
		}
		super.fireGameEvent(gameEvent, gameTriggers);
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

			// This way the message that the game is over doesn't come before the player's connection information is removed
			// from the server.
			if (!didExpire) {
				didExpire = true;
				releaseUsers();
			}

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
			if (fiber != null && !Strand.currentStrand().equals(fiber)) {
				fiber.interrupt();
				fiber = null;
			}


			dispose();

		} finally {
			lock.unlock();
		}
	}

	@Suspendable
	public void releaseUsers() {
		GameId gameId = new GameId(getGameId());
		if (Fiber.isCurrentFiber()) {
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

	private static void releaseUsers(GameId gameId, UserId[] userIds) {
		@Nullable Context context = Vertx.currentContext();
		if (context == null) {
			return;
		}
		context.owner().sharedData().<UserId, GameId>getAsyncMap(Games.GAMES_PLAYERS_MAP, res -> {
			if (res.failed()) {
				return;
			}

			for (UserId userId : userIds) {
				res.result().removeIfPresent(userId, gameId, Future.future());
			}
		});
	}

	/**
	 * Gets the user IDs of the players in this game context. Includes the AI player
	 *
	 * @return A list of user IDs.
	 */
	private List<UserId> getUserIds() {
		return getPlayerConfigurations().stream().map(Configuration::getUserId).collect(toList());
	}

	@Suspendable
	public void handleEndGame(SuspendableAction1<ServerGameContext> handler) {
		onGameEndHandlers.add(handler);
	}

	@Override
	@Suspendable
	public void dispose() {
		Iterator<Closeable> iter = closeables.iterator();
		while (iter.hasNext()) {
			try {
				Closeable closeable = iter.next();
				Void res = awaitResult(closeable::close);
			} catch (Throwable any) {
				LOGGER.error("dispose", any);
			} finally {
				iter.remove();
			}
		}
		super.dispose();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		// We'll do the same thing as disposed, except we won't do it in a fiber and only if we're in a vertx context
		@Nullable Context context = Vertx.currentContext();
		if (context == null) {
			return;
		}
		Iterator<Closeable> iter = closeables.iterator();
		while (iter.hasNext()) {
			try {
				Closeable closeable = iter.next();
				closeable.close(Future.future());
			} catch (Throwable any) {
				LOGGER.error("dispose", any);
			} finally {
				iter.remove();
			}
		}
		releaseUsers();
	}

	public List<Trigger> getGameTriggers() {
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
		GameState gameStateCopy = getGameStateCopy();

		for (Client client : getClients()) {
			if (client.getPlayerId() != sender.getPlayerId()) {
				client.sendNotification(touch, gameStateCopy);
			}
		}
	}

	@Override
	public boolean isGameReady() {
		return clientsReady.values().stream().allMatch(Future::succeeded);
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
			Future<Client> fut = clientsReady.get(client.getPlayerId());
			if (!fut.isComplete()) {
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
				if (next.getPlayerId() == client.getPlayerId()) {
					listIterator.set(client);
					next = client;
				}
				next.onActivePlayer(getActivePlayer());
			}
			onPlayerReady(client);
			onGameStateChanged();
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

	public List<Configuration> getPlayerConfigurations() {
		return playerConfigurations;
	}

	public boolean isRunning() {
		if (isRunning) {
			return !fiber.isInterrupted() && !fiber.isTerminated();
		}
		return false;
	}

	@Suspendable
	public void loseBothPlayers() {
		try {
			getLogic().loseBothPlayers();
			endGame();
		} finally {
			releaseUsers();
		}
	}

	public void setDidExpire(boolean didExpire) {
		this.didExpire = didExpire;
	}

	public boolean getDidExpire() {
		return didExpire;
	}

	/**
	 * Returns once the handlers have successfully registered on this instance
	 */
	@Suspendable
	public void awaitHandlersReady() {
		CompositeFuture res = awaitResult(CompositeFuture.join(registrationsReady)::setHandler);
	}
}
