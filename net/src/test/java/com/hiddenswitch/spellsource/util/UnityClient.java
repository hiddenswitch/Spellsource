package com.hiddenswitch.spellsource.util;

import com.github.fromage.quasi.fibers.Suspendable;
import com.github.fromage.quasi.strands.concurrent.CountDownLatch;
import com.github.fromage.quasi.strands.concurrent.ReentrantLock;
import com.google.common.collect.Sets;
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

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class UnityClient {
	private static Logger logger = LoggerFactory.getLogger(UnityClient.class);
	public static final String BASE = "http://localhost:";
	public static String basePath = BASE + Integer.toString(Port.port());
	private ApiClient apiClient;
	private DefaultApi api;
	private volatile boolean gameOver;
	private Handler<UnityClient> onGameOver;
	private Account account;
	private TestContext context;
	private WebsocketClientEndpoint realtime;
	private AtomicReference<CompletableFuture<Void>> matchmakingFut = new AtomicReference<>(new CompletableFuture<>());
	private AtomicInteger turnsToPlay = new AtomicInteger(999);
	private List<java.util.function.Consumer<ServerToClientMessage>> handlers = new ArrayList<>();
	private String loginToken;
	private String thisUrl;
	private boolean shouldDisconnect = false;
	protected CountDownLatch gameOverLatch;
	// No op lock for now
	protected ReentrantLock messagingLock = new NoOpLock();


	public UnityClient(TestContext context) {
		apiClient = new ApiClient();
		thisUrl = basePath;
		apiClient.setBasePath(basePath);
		api = new DefaultApi(apiClient);
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
			logger.debug("createUserAccount: Created account " + car.getAccount().getId());
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
		logger.info("matchmake {}: Sending enqueue", getAccount().getId());
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

	public void ensureConnected() {
		try {
			messagingLock.lock();
			if (realtime == null) {
				realtime = new WebsocketClientEndpoint(api.getApiClient().getBasePath().replace("http://", "ws://") + "/realtime", loginToken);
				realtime.setMessageHandler(message -> {
					logger.debug("ensureConnected: Handling realtime message for userId {}", getUserId());
					try {
						messagingLock.lock();

						this.handleMessage(Json.decodeValue(message, Envelope.class));
					} finally {
						messagingLock.unlock();
					}
				});
			}
		} finally {
			messagingLock.unlock();
		}
	}

	protected void handleMessage(Envelope env) {
		handleMatchmaking(env);
		handleGameMessages(env);
	}

	protected void handleMatchmaking(Envelope env) {
		if (env.getResult() != null && env.getResult().getEnqueue() != null) {
			if (matchmakingFut.get().isCancelled()) {
				context.fail(new IllegalStateException("matchmaking was cancelled"));
			}

			// Might have been cancelled
			context.assertFalse(matchmakingFut.get().isDone());
			matchmakingFut.get().complete(null);
		}
	}

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

				logger.debug("play: Starting to handle message for userId " + getUserId() + " of type " + message.getMessageType().toString());
				switch (message.getMessageType()) {
					case ON_TURN_END:
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
						this.gameOver = true;
						// TODO: Should we disconnect realtime here?
						if (shouldDisconnect) {
							disconnect();
						}
						gameOverLatch.countDown();
						if (onGameOver != null) {
							onGameOver.handle(this);
						}
						logger.debug("play: UserId " + getUserId() + " received game end message.");
						break;
				}
				logger.debug("play: Done handling message for userId " + getUserId() + " of type " + message.getMessageType().toString());
			} finally {
				messagingLock.unlock();
			}
		}
	}

	public void sendMessage(Envelope env) {
		this.realtime.sendMessage(serialize(env));
	}

	@Suspendable
	public void matchmakeQuickPlay(String deckId) {
		String queueId = "quickPlay";
		matchmakeAndPlay(deckId, queueId);
	}

	@Suspendable
	protected void matchmakeAndPlay(String deckId, String queueId) {
		Future<Void> matchmaking = matchmake(deckId, queueId);
		try {
			matchmaking.get(30000L, TimeUnit.MILLISECONDS);
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

	public void play() {
		this.gameOver = false;
		this.gameOverLatch = new CountDownLatch(1);
		logger.debug("play: Playing userId " + getUserId());

		ensureConnected();
		logger.debug("play: UserId " + getUserId() + " sent first message.");
		sendStartGameMessage();
	}

	private void sendStartGameMessage() {
		realtime.sendMessage(serialize(new Envelope().game(new EnvelopeGame().clientToServer(new ClientToServerMessage()
				.messageType(MessageType.FIRST_MESSAGE)))));
	}

	public void respondRandomAction(ServerToClientMessage message) {
		if (realtime == null) {
			logger.warn("respondRandomAction {} {}: Connection was forcibly disconnected.", getUserId(), message.getId());
			return;
		}
		final int actionCount = message.getActions().getCompatibility().size();
		context.assertTrue(actionCount > 0);
		// There should always be an end turn, choose one, discover or battlecry action
		// Pick a random action
		int random = random(actionCount);
		context.assertNotNull(realtime);
		realtime.sendMessage(serialize(new Envelope().game(new EnvelopeGame().clientToServer(new ClientToServerMessage()
				.messageType(MessageType.UPDATE_ACTION)
				.repliesTo(message.getId())
				.actionIndex(random)))));
		logger.debug("play: UserId " + getUserId() + " sent action with ID " + Integer.toString(random));
	}

	protected boolean onRequestAction(ServerToClientMessage message) {
		return true;
	}

	protected void onMulligan(ServerToClientMessage message) {
	}

	public UserId getUserId() {
		if (getAccount() == null) {
			return null;
		}
		return new UserId(getAccount().getId());
	}

	protected void assertValidActions(ServerToClientMessage message) {

	}

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
		logger.debug("waitUntilDone: UserId " + getUserId() + " is waiting");
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
	protected void finalize() throws Throwable {
		disconnect();
		super.finalize();
	}

}
