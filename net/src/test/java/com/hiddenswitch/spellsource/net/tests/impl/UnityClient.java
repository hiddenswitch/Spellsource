package com.hiddenswitch.spellsource.net.tests.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.futures.AsyncCompletionStage;
import co.paralleluniverse.strands.SettableFuture;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.hiddenswitch.spellsource.net.Matchmaking;
import com.hiddenswitch.spellsource.net.Configuration;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.client.ApiClient;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.impl.BinaryCarrier;
import com.hiddenswitch.spellsource.net.impl.NoOpLock;
import com.hiddenswitch.spellsource.net.impl.UserId;
import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.propagation.Format;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.junit5.VertxTestContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class UnityClient implements AutoCloseable {
	private static Logger LOGGER = LoggerFactory.getLogger(UnityClient.class);
	private static AtomicInteger ids = new AtomicInteger(0);
	public static final String BASE = "http://localhost:";
	public static String BASE_PATH = BASE + Integer.toString(Configuration.apiGatewayPort());
	private final Tracer tracer = Tracing.initialize("unity", ProbabilisticSampler.TYPE, 1.0);
	private final Span parentSpan;
	private int id;
	private ApiClient apiClient;
	private DefaultApi api;
	private volatile boolean gameOver;
	private Handler<UnityClient> onGameOver;
	private Account account;
	private VertxTestContext context;
	private NettyWebsocketClientEndpoint realtime;
	private AtomicReference<SettableFuture<Void>> matchmakingFut = new AtomicReference<>(new SettableFuture<>());
	private AtomicInteger turnsToPlay = new AtomicInteger(999);
	private List<java.util.function.Consumer<ServerToClientMessage>> handlers = new ArrayList<>();
	private String loginToken;
	private String thisUrl;
	private boolean shouldDisconnect = false;
	protected CountDownLatch gameOverLatch = new CountDownLatch(1);
	// No op lock for now
	protected ReentrantLock messagingLock = new NoOpLock();
	private boolean receivedGameOverMessage;
	private AtomicInteger turnsPlayed = new AtomicInteger();
	private int lastTurnPlayed = 0;
	private ServerToClientMessage lastRequest;
	private CountDownLatch turnsPlayedLatch = new CountDownLatch(9999);
	private boolean paused;

	private UnityClient() {
		id = ids.getAndIncrement();
		apiClient = new ApiClient();
		thisUrl = BASE_PATH;
		apiClient.setBasePath(BASE_PATH);
		api = new DefaultApi(apiClient);
		parentSpan = tracer.buildSpan("UnityClient").withTag("id", id).start();
		tracer.activateSpan(parentSpan);
	}

	public UnityClient(VertxTestContext context) {
		this();
		this.context = context;
	}

	public UnityClient(VertxTestContext context, int port) {
		this(context);
		thisUrl = BASE + port;
		apiClient.setBasePath(thisUrl);
		api = new DefaultApi(apiClient);
	}

	private void verify(SuspendableRunnable block) {
		if (context != null) {
			try {
				block.run();
			} catch (Throwable t) {
				context.failNow(t);
			}
		} else {
			try {
				block.run();
			} catch (Throwable throwable) {
				fail(throwable);
			}
		}
	}

	public UnityClient(VertxTestContext context, String token) {
		this(context);
		this.loginToken = token;
		api.getApiClient().setApiKey(loginToken);
	}

	@Suspendable
	public UnityClient createUserAccount() {
		return createUserAccount(null);
	}

	@Suspendable
	public UnityClient createUserAccount(String username) {
		if (username == null) {
			username = RandomStringUtils.randomAlphanumeric(10);
		}

		var finalUsername = username;
		verify(() -> {
			try {
				var car = api.createAccount(
						new CreateAccountRequest()
								.email(finalUsername + "@hiddenswitch.com")
								.name(finalUsername)
								.password("testpass"));
				loginToken = car.getLoginToken();
				api.getApiClient().setApiKey(loginToken);
				account = car.getAccount();
				assertNotNull(account);
				assertTrue(account.getDecks().size() > 0);

				LOGGER.debug("createUserAccount {} {}: Created account", id, car.getAccount().getId());
			} catch (ApiException e) {
				Tracing.error(e, parentSpan, true);
				fail(e);
			}
		});
		return this;
	}

	public void gameOverHandler(Handler<UnityClient> handler) {
		onGameOver = handler;
	}

	@Suspendable
	public Future<Void> matchmake(String deckId, String queueId) {
		if (deckId == null) {
			deckId = account.getDecks().get(random(account.getDecks().size())).getId();
		}

		var fut = new SettableFuture<Void>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				realtime.sendMessage(Json.encode(new Envelope()
						.method(new EnvelopeMethod()
								.methodId(RandomStringUtils.randomAlphanumeric(10))
								.dequeue(new EnvelopeMethodDequeue().queueId(queueId)))));
				return super.cancel(mayInterruptIfRunning);
			}
		};

		matchmakingFut.set(fut);
		ensureConnected();
		assertTrue(realtime.isOpen());
		try {
			messagingLock.lock();
			sendMessage(new Envelope()
					.method(new EnvelopeMethod()
							.methodId(RandomStringUtils.randomAlphanumeric(10))
							.enqueue(new MatchmakingQueuePutRequest()
									.queueId(queueId)
									.deckId(deckId))));
		} finally {
			messagingLock.unlock();
		}

		return fut;
	}

	@Suspendable
	public void ensureConnected() {
		try {
			messagingLock.lock();
			if (realtime == null) {
				var span = new AtomicReference<Span>(tracer.buildSpan("ServerGameContext/play")
						.asChildOf(parentSpan)
						.start());
				span.get().log("connecting");
				if (getAccount() != null) {
					span.get().setTag("userId", getAccount().getId());
				}
				realtime = new NettyWebsocketClientEndpoint(api.getApiClient().getBasePath().replace("http://", "ws://") + "/realtime", loginToken);
				var firstMessage = new CountDownLatch(1);
				LOGGER.debug("ensureConnected {}: Connected", id);
				realtime.setMessageHandler((String message) -> {
					var s = tracer.activateSpan(span.get());
					try {
						var env = Json.decodeValue(message, Envelope.class);
						if (env.getAdded() != null && env.getAdded().getSpanContext() != null) {
							// Finish the current span, then create another.
							span.get().finish();
							s.close();
							span.set(tracer.buildSpan("ServerGameContext/play")
									.asChildOf(tracer.extract(Format.Builtin.BINARY, new BinaryCarrier(env.getAdded().getSpanContext().getData())))
									.start());
							s = tracer.activateSpan(span.get());
							if (getAccount() != null) {
								span.get().setTag("userId", getAccount().getId());
							}
						}
						span.get().log(ImmutableMap.of(Fields.EVENT, "received",
								"messageType", env.getGame() == null ? "" : env.getGame().getServerToClient().getMessageType().toString()));
						firstMessage.countDown();
						try {
							messagingLock.lock();
							this.handleMessage(env);
						} finally {
							messagingLock.unlock();
						}
					} catch (RuntimeException runtimeException) {
						Tracing.error(runtimeException, span.get(), true);
//						close();
						fail(runtimeException);
					} finally {
						s.close();
					}
				});
				realtime.connect();
				firstMessage.await(4000, TimeUnit.MILLISECONDS);
				assertTrue(firstMessage.getCount() <= 0);
			}
		} catch (Throwable any) {
//			close();
			Tracing.error(any, parentSpan, true);
			fail(any);
		} finally {
			messagingLock.unlock();
		}
	}

	@Suspendable
	protected void handleMessage(Envelope env) {
		handleMatchmaking(env);
		handleGameMessages(env);
	}

	@Suspendable
	protected void handleMatchmaking(Envelope env) {
		if (!env.equals(Matchmaking.gameReadyMessage())) {
			return;
		}

		if (matchmakingFut.get().isCancelled()) {
			Tracing.error(new IllegalStateException("matchmaking was cancelled"), parentSpan, true);
			fail(new IllegalStateException("matchmaking was cancelled"));
		}

		// Might have been cancelled
		assertFalse(matchmakingFut.get().isDone());
		matchmakingFut.get().set(null);
	}

	@Suspendable
	protected void handleGameMessages(Envelope env) {
		if (env.getGame() == null || env.getGame().getServerToClient() == null) {
			return;
		}

		var message = env.getGame().getServerToClient();
		for (var handler : handlers) {
			if (handler != null) {
				handler.accept(message);
			}
		}
		if (turnsToPlay.get() <= 0) {

			if (!gameOver) {
				gameOverLatch.countDown();
			}
			gameOver = true;
			if (shouldDisconnect) {
				disconnect();
			}
			return;
		}

		switch (message.getMessageType()) {
			case ON_UPDATE:
				var turnNumber = message.getGameState().getTurnNumber();
				if (turnNumber != null && lastTurnPlayed != turnNumber) {
					turnsPlayed.incrementAndGet();
					turnsPlayedLatch.countDown();
				}
				if (turnNumber != null && turnNumber >= turnsToPlay.get() && shouldDisconnect) {
					disconnect();
				} else {
					assertValidStateAndChanges(message);
				}
				break;
			case ON_GAME_EVENT:
				assertNotNull(message.getEvent());
				assertValidStateAndChanges(message);
				break;
			case ON_MULLIGAN:
				onMulligan(message);
				if (turnsToPlay.get() == 0 && shouldDisconnect) {
					// don't respond to the mulligan attempt
					disconnect();
					gameOverLatch.countDown();
					break;
				}
				assertNotNull(message.getStartingCards());
				assertTrue(message.getStartingCards().size() > 0);
				realtime.sendMessage(serialize(new Envelope().game(new EnvelopeGame().clientToServer(new ClientToServerMessage()
						.messageType(MessageType.UPDATE_MULLIGAN)
						.repliesTo(message.getId())
						.discardedCardIndices(Collections.singletonList(0))))));
				break;
			case ON_REQUEST_ACTION:
				lastRequest = message;
				if (paused || !onRequestAction(message)) {
					break;
				}
				assertValidActions(message);
				assertValidStateAndChanges(message);
				assertNotNull(message.getGameState());
				assertNotNull(message.getChanges());
				assertNotNull(message.getActions());
				respondRandomAction(message);
				break;
			case ON_GAME_END:
				// The game has ended.
				this.receivedGameOverMessage = true;
				this.gameOver = true;
				// TODO: Should we disconnect realtime here?
				if (shouldDisconnect) {
					disconnect();
				}
				gameOverLatch.countDown();
				if (onGameOver != null) {
					onGameOver.handle(this);
				}
				LOGGER.debug("play {} {}: received game end message.", id, getUserId());
				tracer.activeSpan().finish();
				break;
		}
		LOGGER.trace("play: Done handling message for userId " + getUserId() + " of type " + message.getMessageType().toString());
	}

	@Suspendable
	public void sendMessage(Envelope env) {
		this.realtime.sendMessage(serialize(env));
	}

	/**
	 * Quick plays against an AI.
	 *
	 * @param deckId A deck to use, or {@code null} to use any deck
	 */
	@Suspendable
	public void matchmakeQuickPlay(@Nullable String deckId) {
		var queueId = "quickPlay";
		matchmakeAndPlay(deckId, queueId);
	}

	/**
	 * Blocks until the client is matched. Use {@link #play()} to start playing, and use {@link #waitUntilDone()} to wait
	 * until the player is actually done with the game.
	 *
	 * @param deckId
	 * @param queueId
	 */
	@Suspendable
	public void matchmakeAndPlay(String deckId, String queueId) {
		var matchmaking = matchmake(deckId, queueId);
		try {
			matchmaking.get(35000L, TimeUnit.MILLISECONDS);
			play();
		} catch (InterruptedException | ExecutionException ex) {
			matchmaking.cancel(true);
		} catch (TimeoutException e) {
			Tracing.error(e, parentSpan, true);
			fail(e);
		}
	}

	@Suspendable
	public void matchmakeConstructedPlay(String deckId) {
		var queueId = "constructed";
		matchmakeAndPlay(deckId, queueId);
	}

	@Suspendable
	public void play() {
		paused = false;
		this.receivedGameOverMessage = false;
		this.gameOver = false;
		this.gameOverLatch = new CountDownLatch(1);
		this.turnsPlayed = new AtomicInteger();
		this.lastTurnPlayed = 0;
		LOGGER.debug("play {} {}: Playing", id, getUserId());

		ensureConnected();
		sendStartGameMessage();
	}

	@Suspendable
	public void play(int untilTurns) {
		play();
		this.turnsPlayedLatch = new CountDownLatch(untilTurns);

		try {
			turnsPlayedLatch.await(untilTurns, TimeUnit.SECONDS);
			paused = true;
		} catch (InterruptedException e) {
			return;
		}
	}

	@Suspendable
	private void sendStartGameMessage() {
		sendMessage(new Envelope().game(new EnvelopeGame().clientToServer(Matchmaking.gameReadyMessage().getResult().getEnqueue().getUnityConnection().getFirstMessage())));
		LOGGER.debug("sendStartGameMessage {} {}: sent first message.", id, getUserId());
	}

	@Suspendable
	public void respondRandomAction(ServerToClientMessage message) {
		if (message == null) {
			throw new NullPointerException("no last request found");
		}
		if (realtime == null) {
			LOGGER.warn("respondRandomAction {} {}: Connection was forcibly disconnected.", getUserId(), message.getId());
			return;
		}
		final var actionCount = message.getActions().getCompatibility().size();
		assertTrue(actionCount > 0);
		// There should always be an end turn, choose one, discover or battlecry action
		// Pick a random action
		var action = getActionIndex(message);
		assertNotNull(realtime);
		realtime.sendMessage(serialize(new Envelope().game(new EnvelopeGame().clientToServer(new ClientToServerMessage()
				.messageType(MessageType.UPDATE_ACTION)
				.repliesTo(message.getId())
				.actionIndex(action)))));
		LOGGER.trace("play: UserId " + getUserId() + " sent action with ID " + action);
	}

	@Suspendable
	protected int getActionIndex(ServerToClientMessage message) {
		return random(message.getActions().getCompatibility().size());
	}

	/**
	 * A handler for every request action received by this UnityClient.
	 *
	 * @param message The message received
	 * @return {@code true} if the client should keep playing.
	 */
	@Suspendable
	protected boolean onRequestAction(ServerToClientMessage message) {
		return true;
	}

	@Suspendable
	protected void onMulligan(ServerToClientMessage message) {
	}

	public UserId getUserId() {
		if (getAccount() == null) {
			return null;
		}
		return new UserId(getAccount().getId());
	}

	@Suspendable
	protected void assertValidActions(ServerToClientMessage message) {

	}

	@Suspendable
	public void disconnect() {
		try {
			messagingLock.lock();
			if (realtime != null && realtime.isOpen()) {
				realtime.close();
				realtime = null;
			} else {
				LOGGER.warn("disconnect {}: realtime was null={}, and open {}", id, realtime == null, realtime != null && realtime.isOpen());
			}
		} finally {
			messagingLock.unlock();
		}

	}

	protected void assertValidStateAndChanges(ServerToClientMessage message) {
		assertNotNull(message.getGameState());
		assertNotNull(message.getChanges());
		assertNotNull(message.getGameState().getTurnNumber());
		assertTrue(message.getGameState().getEntities().stream().allMatch(e -> e.getId() >= 0));
		assertTrue(message.getGameState().getEntities().stream().filter(e -> e.getEntityType() == EntityType.PLAYER).count() == 2);
		assertTrue(message.getGameState().getEntities().stream().filter(e -> e.getEntityType() == EntityType.HERO).count() >= 2);
		assertTrue(message.getGameState().getEntities().stream().filter(e ->
				e.getEntityType() == EntityType.HERO
						&& e.getL().getZ() == EntityLocation.ZEnum.E
		).allMatch(h ->
				null != h.getMaxMana()));
		assertNotNull(message.getGameState().getTurnNumber());
		if (message.getGameState().getTurnNumber() > 0) {
			assertTrue(message.getGameState().getEntities().stream().filter(e -> e.getEntityType() == EntityType.HERO).anyMatch(h ->
					h.getMaxMana() >= 1));
		}
		final var entityIds = message.getGameState().getEntities().stream().map(Entity::getId).collect(Collectors.toSet());
		final Set<Integer> changeIds = new HashSet<>(message.getChanges().getIds());
		final var contains = entityIds.containsAll(changeIds);
		if (!contains) {
			fail(/*message.toString()*/ "An ID is missing! " + Sets.difference(changeIds, entityIds).toString());
		}
		if (message.getMessageType() == MessageType.ON_GAME_EVENT
				&& message.getEvent() != null
				&& message.getEvent().getEventType() == GameEvent.EventTypeEnum.TRIGGER_FIRED) {
			assertTrue(entityIds.contains(message.getEvent().getTriggerFired().getTriggerSourceId()));
		}
		assertTrue(contains);
	}

	private int random(int upper) {
		return RandomUtils.nextInt(0, upper);
	}

	private String serialize(Object obj) {
		return Json.encode(obj);
	}

	public ApiClient getApiClient() {
		return apiClient;
	}

	public void setApiClient(ApiClient apiClient) {
		this.apiClient = apiClient;
	}

	public DefaultApi getApi() {
		return api;
	}

	public void setApi(DefaultApi api) {
		this.api = api;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public Account getAccount() {
		return account;
	}

	public AtomicInteger getTurnsToPlay() {
		return turnsToPlay;
	}

	public void addMessageHandler(java.util.function.Consumer<ServerToClientMessage> handler) {
		handlers.add(handler);
	}

	@Suspendable
	public UnityClient waitUntilDone() {
		LOGGER.debug("waitUntilDone {} {}: is waiting", id, getUserId());
		try {
			gameOverLatch.await(120L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Tracing.error(e, parentSpan, true);
			fail(e);
		}
		return this;
	}

	public String getToken() {
		return loginToken;
	}

	public void concede() {
		messagingLock.lock();
		realtime.sendMessage(serialize(new Envelope().game(new EnvelopeGame().clientToServer(new ClientToServerMessage()
				.messageType(MessageType.CONCEDE)))));
		messagingLock.unlock();
	}

	public boolean isShouldDisconnect() {
		return shouldDisconnect;
	}

	public void setShouldDisconnect(boolean shouldDisconnect) {
		this.shouldDisconnect = shouldDisconnect;
	}

	public boolean isConnected() {
		return realtime != null && realtime.isOpen();
	}

	@Override
	public void close() {
		disconnect();

		if (tracer.activeSpan() != null) {
			tracer.activeSpan().finish();
		}
//		if (tracer.scopeManager().active() != null) {
//			tracer.scopeManager().active().close();
//		}
		tracer.close();
	}

	public boolean receivedGameOverMessage() {
		return receivedGameOverMessage;
	}

	public int getTurnsPlayed() {
		return turnsPlayed.get();
	}

	public void respondRandomAction() {
		paused = false;
		respondRandomAction(lastRequest);
	}
}
