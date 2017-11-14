package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.google.common.collect.Sets;
import com.hiddenswitch.spellsource.Games;
import com.hiddenswitch.spellsource.client.ApiClient;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.client.models.*;
import io.vertx.core.Handler;
import io.vertx.ext.unit.TestContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class UnityClient {
	public static String basePath = "http://localhost:80" + "";
	private ApiClient apiClient;
	private DefaultApi api;
	private boolean gameOver;
	private Handler<UnityClient> onGameOver;
	private Account account;
	private TestContext context;
	private WebsocketClientEndpoint endpoint;
	private String gameId;
	private AtomicInteger turnsToPlay = new AtomicInteger(999);
	private List<java.util.function.Consumer<ServerToClientMessage>> handlers = new ArrayList<>();


	public UnityClient(TestContext context) {
		apiClient = new ApiClient();
		apiClient.setBasePath(basePath);
		api = new DefaultApi(apiClient);
		this.context = context;
	}

	public UnityClient(TestContext context, int turnsToPlay) {
		this(context);
		this.turnsToPlay = new AtomicInteger(turnsToPlay);
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
			api.getApiClient().setApiKey(car.getLoginToken());
			account = car.getAccount();
			context.assertNotNull(account);
			context.assertTrue(account.getDecks().size() > 0);
		} catch (ApiException e) {
			context.fail(e.getMessage());
		}
		return this;
	}

	public UnityClient loginWithUserAccount(String username) {
		return loginWithUserAccount(username, "testpass");
	}

	public void gameOver(Handler<UnityClient> handler) {
		onGameOver = io.vertx.ext.sync.Sync.fiberHandler(handler);
	}

	public UnityClient matchmakeAndPlayAgainstAI() {
		return matchmakeAndPlayAgainstAI(null);
	}

	public UnityClient matchmakeAndPlayAgainstAI(String deckId) {
		if (deckId == null) {
			deckId = account.getDecks().get(random(account.getDecks().size())).getId();
		}
		try {
			MatchmakingQueuePutResponse mqpr = api.matchmakingConstructedQueuePut("constructed", new MatchmakingQueuePutRequest()
					.casual(true)
					.deckId(deckId));

			final MatchmakingQueuePutResponseUnityConnection unityConnection = mqpr.getUnityConnection();
			play(unityConnection);

		} catch (ApiException | URISyntaxException e) {
			context.fail(e.getMessage());
		}
		return this;
	}

	public UnityClient matchmakeAndPlay(String deckId) throws InterruptedException {
		if (deckId == null) {
			deckId = account.getDecks().get(random(account.getDecks().size())).getId();
		}

		try {
			MatchmakingQueuePutResponseUnityConnection unityConnection = null;
			while (unityConnection == null) {
				MatchmakingQueuePutResponse mqpr = api.matchmakingConstructedQueuePut("constructed", new MatchmakingQueuePutRequest()
						.casual(false)
						.deckId(deckId));
				unityConnection = mqpr.getUnityConnection();
				Thread.sleep(500);
			}

			play(unityConnection);
		} catch (ApiException | URISyntaxException e) {
			context.fail(e.getMessage());
		}
		return this;
	}

	public UnityClient contextPlay(IntegrationTestContext unityContext) {
		return this;
	}

	private void play(MatchmakingQueuePutResponseUnityConnection unityConnection) throws URISyntaxException {
		Assert.assertNotNull(unityConnection);
		Assert.assertNotNull(unityConnection.getFirstMessage());

		String url = unityConnection.getUrl();
		Assert.assertNotNull(url);

		// Get the port from the url

		url = "ws://localhost:" + Integer.toString((new URI(url)).getPort()) + "/" + Games.WEBSOCKET_PATH;

		endpoint = new WebsocketClientEndpoint(new URI(url));
		endpoint.addMessageHandler(h -> {
			ServerToClientMessage message = apiClient.getJSON().deserialize(h, ServerToClientMessage.class);

			for (java.util.function.Consumer<ServerToClientMessage> handler : handlers) {
				if (handler != null) {
					handler.accept(message);
				}
			}

			switch (message.getMessageType()) {
				case ON_TURN_END:
					if (turnsToPlay.getAndDecrement() <= 0) {
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
					context.assertNotNull(message.getStartingCards());
					context.assertTrue(message.getStartingCards().size() > 0);
					endpoint.sendMessage(serialize(new ClientToServerMessage()
							.messageType(MessageType.UPDATE_MULLIGAN)
							.repliesTo(message.getId())
							.discardedCardIndices(Collections.singletonList(0))));
					break;
				case ON_REQUEST_ACTION:
					assertValidActions(message);
					assertValidStateAndChanges(message);
					context.assertNotNull(message.getGameState());
					context.assertNotNull(message.getChanges());
					context.assertNotNull(message.getActions());
					final int actionCount = message.getActions().getCompatibility().size();
					context.assertTrue(actionCount > 0);
					// There should always be an end turn, choose one, discover or battlecry action
					// Pick a random action
					endpoint.sendMessage(serialize(new ClientToServerMessage()
							.messageType(MessageType.UPDATE_ACTION)
							.repliesTo(message.getId())
							.actionIndex(random(actionCount))));
					break;
				case ON_GAME_END:
					// The game has ended.
					try {
						endpoint.getUserSession().close();
					} catch (IOException ignored) {
					}
					this.gameOver = true;
					if (onGameOver != null) {
						onGameOver.handle(this);
					}
					break;

			}
		});

		endpoint.sendMessage(serialize(unityConnection.getFirstMessage()));
	}

	protected void assertValidActions(ServerToClientMessage message) {

	}

	public void disconnect() {
		try {
			endpoint.getUserSession().close();
		} catch (IOException e) {
			e.printStackTrace();
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
		return apiClient.getJSON().serialize(obj);
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
		float time = 0f;
		while (!(time > 36f || this.isGameOver())) {
			try {
				Strand.sleep(1000);
			} catch (SuspendExecution | InterruptedException suspendExecution) {
				suspendExecution.printStackTrace();
				return this;
			}

			time += 1f;
		}
		return this;
	}

	public UnityClient loginWithUserAccount(String username, String password) {
		try {
			LoginResponse lr = api.login(new LoginRequest().email(username + "@hiddenswitch.com").password(password));
			api.getApiClient().setApiKey(lr.getLoginToken());
			account = lr.getAccount();
			context.assertNotNull(account);
		} catch (ApiException e) {
			context.fail(e.getMessage());
		}
		return this;
	}
}
