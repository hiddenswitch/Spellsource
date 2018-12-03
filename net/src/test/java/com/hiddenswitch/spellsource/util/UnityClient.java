package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import co.paralleluniverse.strands.concurrent.ReentrantLock;
import com.google.common.collect.Sets;
import com.hiddenswitch.spellsource.Matchmaking;
import com.hiddenswitch.spellsource.Port;
import com.hiddenswitch.spellsource.client.ApiClient;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.UserId;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.TestContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.vertx.ext.sync.Sync.fiberHandler;

public class UnityClient implements AutoCloseable {
	private static Logger LOGGER = LoggerFactory.getLogger(UnityClient.class);
	private static AtomicInteger ids = new AtomicInteger(0);
	public static final String BASE = "http://localhost:";
	public static String basePath = BASE + Integer.toString(Port.port());
	private int id;
	private ApiClient apiClient;
	private DefaultApi api;
	private volatile boolean gameOver;
	private Handler<UnityClient> onGameOver;
	private Account account;
	private TestContext context;
	private NettyWebsocketClientEndpoint realtime;
	private AtomicReference<CompletableFuture<Void>> matchmakingFut = new AtomicReference<>(new CompletableFuture<>());
	private AtomicInteger turnsToPlay = new AtomicInteger(999);
	private List<java.util.function.Consumer<ServerToClientMessage>> handlers = new ArrayList<>();
	private String loginToken;
	private String thisUrl;
	private boolean shouldDisconnect = false;
	protected CountDownLatch gameOverLatch;
	// No op lock for now
	protected ReentrantLock messagingLock = new NoOpLock();
	protected CountDownLatch receivedAtLeastOneMessage;
	private boolean receivedGameOverMessage;
	private AtomicInteger turnsPlayed = new AtomicInteger();


	public UnityClient(TestContext context) {
		apiClient = new ApiClient();
		thisUrl = basePath;
		apiClient.setBasePath(basePath);
		api = new DefaultApi(apiClient);
		id = ids.getAndIncrement();
		this.context = context;
	}

	public UnityClient(TestContext context, int port) {
		this(context);
		apiClient = new ApiClient();
		thisUrl = BASE + Integer.toString(port);
		apiClient.setBasePath(thisUrl);
		api = new DefaultApi(apiClient);
	}

	public UnityClient(TestContext context, String token) {
		this(context);
		this.loginToken = token;
		api.getApiClient().setApiKey(loginToken);
	}

	public UnityClient createUserAccount() {
		return createUserAccount(null);
	}

	public UnityClient createUserAccount(String username) {
		if (username == null) {
			username = RandomStringUtils.randomAlphanumeric(10);
		}

		try {
			CreateAccountResponse car = api.createAccount(new CreateAccountRequest().email(username + "@hiddenswitch.com").name(username).password("testpass"));
			loginToken = car.getLoginToken();
			api.getApiClient().setApiKey(loginToken);
			account = car.getAccount();
			context.assertNotNull(account);
			context.assertTrue(account.getDecks().size() > 0);
			LOGGER.debug("createUserAccount {} {}: Created account", id, car.getAccount().getId());
		} catch (ApiException e) {
			context.fail(e.getMessage());
		}
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

		CompletableFuture<Void> fut = new CompletableFuture<Void>() {
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
		context.assertTrue(realtime.isOpen());
		LOGGER.info("matchmake {} {}: Sending enqueue", id, getAccount().getId());
		try {
			messagingLock.lock();
			realtime.sendMessage(Json.encode(new Envelope()
					.method(new EnvelopeMethod()
							.methodId(RandomStringUtils.randomAlphanumeric(10))
							.enqueue(new MatchmakingQueuePutRequest()
									.queueId(queueId)
									.deckId(deckId)))));
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
				realtime = new NettyWebsocketClientEndpoint(api.getApiClient().getBasePath().replace("http://", "ws://") + "/realtime", loginToken);
				realtime.setMessageHandler((String message) -> {
					LOGGER.trace("ensureConnected {}: Handling realtime message for userId {}", id, getUserId());
					try {
						messagingLock.lock();

						this.handleMessage(Json.decodeValue(message, Envelope.class));
					} finally {
						messagingLock.unlock();
					}
				});
				realtime.getNettyWebSocketCompletableFuture().join();
			}
		} catch (Throwable any) {
			LOGGER.error("ensureConnected: ", any);
			context.fail(any);
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
		if (env.equals(Matchmaking.gameReadyMessage())) {
			if (matchmakingFut.get().isCancelled()) {
				context.fail(new IllegalStateException("matchmaking was cancelled"));
			}

			// Might have been cancelled
			context.assertFalse(matchmakingFut.get().isDone());
			matchmakingFut.get().complete(null);
		}
	}

	@Suspendable
	protected void handleGameMessages(Envelope env) {
		if (env.getGame() != null && env.getGame().getServerToClient() != null) {
			ServerToClientMessage message = env.getGame().getServerToClient();
			messagingLock.lock();
			try {
				for (java.util.function.Consumer<ServerToClientMessage> handler : handlers) {
					if (handler != null) {
						handler.accept(message);
					}
				}
				if (turnsToPlay.get() <= 0) {
					if (!gameOver && onGameOver != null) {
						onGameOver.handle(this);
					}
					if (!gameOver) {
						gameOverLatch.countDown();
					}
					gameOver = true;
					if (shouldDisconnect) {
						disconnect();
					}
					return;
				}

				LOGGER.trace("play: Starting to handle message for userId " + getUserId() + " of type " + message.getMessageType().toString());
				switch (message.getMessageType()) {
					case ON_TURN_END:
						turnsPlayed.incrementAndGet();
						if (turnsToPlay.getAndDecrement() <= 0
								&& shouldDisconnect) {
							disconnect();
						}
						break;
					case ON_UPDATE:
						assertValidStateAndChanges(message);
						break;
					case ON_GAME_EVENT:
						context.assertNotNull(message.getEvent());
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
						context.assertNotNull(message.getStartingCards());
						context.assertTrue(message.getStartingCards().size() > 0);
						realtime.sendMessage(serialize(new Envelope().game(new EnvelopeGame().clientToServer(new ClientToServerMessage()
								.messageType(MessageType.UPDATE_MULLIGAN)
								.repliesTo(message.getId())
								.discardedCardIndices(Collections.singletonList(0))))));
						break;
					case ON_REQUEST_ACTION:
						if (!onRequestAction(message)) {
							break;
						}
						assertValidActions(message);
						assertValidStateAndChanges(message);
						context.assertNotNull(message.getGameState());
						context.assertNotNull(message.getChanges());
						context.assertNotNull(message.getActions());
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
						break;
				}
				LOGGER.trace("play: Done handling message for userId " + getUserId() + " of type " + message.getMessageType().toString());
			} finally {
				messagingLock.unlock();
			}
		}
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
		String queueId = "quickPlay";
		matchmakeAndPlay(deckId, queueId);
	}

	@Suspendable
	protected void matchmakeAndPlay(String deckId, String queueId) {
		Future<Void> matchmaking = matchmake(deckId, queueId);
		try {
			matchmaking.get(25000L, TimeUnit.MILLISECONDS);
			play();
		} catch (InterruptedException | ExecutionException ex) {
			matchmaking.cancel(true);
		} catch (TimeoutException e) {
			context.fail(e);
		}
	}

	@Suspendable
	public void matchmakeConstructedPlay(String deckId) {
		String queueId = "constructed";
		matchmakeAndPlay(deckId, queueId);
	}

	@Suspendable
	public void play() {
		this.receivedGameOverMessage = false;
		this.gameOver = false;
		this.gameOverLatch = new CountDownLatch(1);
		this.turnsPlayed = new AtomicInteger();
		LOGGER.debug("play {} {}: Playing", id, getUserId());

		ensureConnected();
		sendStartGameMessage();
	}

	@Suspendable
	private void sendStartGameMessage() {
		sendMessage(new Envelope().game(new EnvelopeGame().clientToServer(Matchmaking.gameReadyMessage().getResult().getEnqueue().getUnityConnection().getFirstMessage())));
		LOGGER.debug("sendStartGameMessage {} {}: sent first message.", id, getUserId());
	}

	@Suspendable
	public void respondRandomAction(ServerToClientMessage message) {
		if (realtime == null) {
			LOGGER.warn("respondRandomAction {} {}: Connection was forcibly disconnected.", getUserId(), message.getId());
			return;
		}
		final int actionCount = message.getActions().getCompatibility().size();
		context.assertTrue(actionCount > 0);
		// There should always be an end turn, choose one, discover or battlecry action
		// Pick a random action
		int action = getActionIndex(message);
		context.assertNotNull(realtime);
		realtime.sendMessage(serialize(new Envelope().game(new EnvelopeGame().clientToServer(new ClientToServerMessage()
				.messageType(MessageType.UPDATE_ACTION)
				.repliesTo(message.getId())
				.actionIndex(action)))));
		LOGGER.trace("play: UserId " + getUserId() + " sent action with ID " + Integer.toString(action));
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
			}
		} finally {
			messagingLock.unlock();
		}

	}

	protected void assertValidStateAndChanges(ServerToClientMessage message) {
		context.assertNotNull(message.getGameState());
		context.assertNotNull(message.getChanges());
		context.assertNotNull(message.getGameState().getTurnNumber());
		context.assertTrue(message.getGameState().getEntities().stream().allMatch(e -> e.getId() >= 0));
		context.assertTrue(message.getChanges().stream().allMatch(e -> e.getId() >= 0));
		context.assertTrue(message.getGameState().getEntities().stream().filter(e -> e.getEntityType() == Entity.EntityTypeEnum.PLAYER).count() == 2);
		context.assertTrue(message.getGameState().getEntities().stream().filter(e -> e.getEntityType() == Entity.EntityTypeEnum.HERO).count() >= 2);
		context.assertTrue(message.getGameState().getEntities().stream().filter(e ->
				e.getEntityType() == Entity.EntityTypeEnum.HERO
						&& e.getState().getLocation().getZone() == EntityLocation.ZoneEnum.HERO
		).allMatch(h ->
				null != h.getState().getMaxMana()));
		context.assertNotNull(message.getGameState().getTurnNumber());
		if (message.getGameState().getTurnNumber() > 0) {
			context.assertTrue(message.getGameState().getEntities().stream().filter(e -> e.getEntityType() == Entity.EntityTypeEnum.HERO).anyMatch(h ->
					h.getState().getMaxMana() >= 1));
		}
		final Set<Integer> entityIds = message.getGameState().getEntities().stream().map(Entity::getId).collect(Collectors.toSet());
		final Set<Integer> changeIds = message.getChanges().stream().map(EntityChangeSetInner::getId).collect(Collectors.toSet());
		final boolean contains = entityIds.containsAll(changeIds);
		if (!contains) {
			context.fail(/*message.toString()*/ "An ID is missing! " + Sets.difference(changeIds, entityIds).toString());
		}
		if (message.getMessageType() == MessageType.ON_GAME_EVENT
				&& message.getEvent() != null
				&& message.getEvent().getEventType() == GameEvent.EventTypeEnum.TRIGGER_FIRED) {
			context.assertTrue(entityIds.contains(message.getEvent().getTriggerFired().getTriggerSourceId()));
		}
		context.assertTrue(contains);
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
			gameOverLatch.await(90L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
		return this;
	}

	public UnityClient loginWithUserAccount(String username, String password) {
		try {
			LoginResponse lr = api.login(new LoginRequest().email(username + "@hiddenswitch.com").password(password));
			loginToken = lr.getLoginToken();
			api.getApiClient().setApiKey(loginToken);
			account = lr.getAccount();
			context.assertNotNull(account);
		} catch (ApiException e) {
			context.fail(e.getMessage());
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
	public void close() throws Exception {
		disconnect();
	}

	public boolean receivedGameOverMessage() {
		return receivedGameOverMessage;
	}

	public int getTurnsPlayed() {
		return turnsPlayed.get();
	}
}
