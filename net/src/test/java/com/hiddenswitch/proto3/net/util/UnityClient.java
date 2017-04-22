package com.hiddenswitch.proto3.net.util;

import com.hiddenswitch.proto3.net.Games;
import com.hiddenswitch.proto3.net.client.ApiClient;
import com.hiddenswitch.proto3.net.client.ApiException;
import com.hiddenswitch.proto3.net.client.api.DefaultApi;
import com.hiddenswitch.proto3.net.client.models.*;
import io.vertx.core.Handler;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;

/**
 * Created by bberman on 4/10/17.
 */
public class UnityClient {
	private ApiClient apiClient;
	private DefaultApi api;
	private boolean gameOver;
	private Handler<UnityClient> onGameOver;
	private Account account;

	public UnityClient() {
		apiClient = new ApiClient();
		apiClient.setBasePath("http://localhost:8080/v1");
		api = new DefaultApi(apiClient);
	}

	public void createUserAccount(String username) {
		if (username == null) {
			username = RandomStringUtils.randomAlphanumeric(10);
		}

		try {
			CreateAccountResponse car = api.createAccount(new CreateAccountRequest().email(username + "@hiddenswitch.com").name(username).password("testpass"));
			api.getApiClient().setApiKey(car.getLoginToken());
			account = car.getAccount();
			Assert.assertNotNull(account);
			Assert.assertTrue(account.getDecks().size() > 0);
		} catch (ApiException e) {
			Assert.fail(e.getMessage());
		}
	}

	public void gameOver(Handler<UnityClient> handler) {
		onGameOver = io.vertx.ext.sync.Sync.fiberHandler(handler);
	}

	public void matchmakeAndPlayAgainstAI(String deckId) {
		if (deckId == null) {
			deckId = account.getDecks().get(random(account.getDecks().size())).getId();
		}
		try {
			MatchmakingQueuePutResponse mqpr = api.matchmakingConstructedQueuePut(new MatchmakingQueuePutRequest()
					.casual(true)
					.deckId(deckId));

			Assert.assertNotNull(mqpr.getUnityConnection());
			Assert.assertNotNull(mqpr.getConnection());
			Assert.assertNotNull(mqpr.getUnityConnection().getFirstMessage());

			String url = mqpr.getUnityConnection().getUrl();
			Assert.assertNotNull(url);

			// Get the port from the url

			url = "ws://localhost:" + Integer.toString((new URI(url)).getPort()) + "/" + Games.WEBSOCKET_PATH;

			WebsocketClientEndpoint endpoint = new WebsocketClientEndpoint(new URI(url));
			endpoint.addMessageHandler(h -> {
				ServerToClientMessage message = apiClient.getJSON().deserialize(h, ServerToClientMessage.class);

				switch (message.getMessageType()) {
					case ON_UPDATE:
						Assert.assertNotNull(message.getChanges());
						Assert.assertNotNull(message.getGameState());
						break;
					case ON_GAME_EVENT:
						Assert.assertNotNull(message.getEvent());
						Assert.assertNotNull(message.getChanges());
						break;
					case ON_MULLIGAN:
						Assert.assertNotNull(message.getStartingCards());
						Assert.assertTrue(message.getStartingCards().size() > 0);
						endpoint.sendMessage(serialize(new ClientToServerMessage()
								.messageType(MessageType.UPDATE_MULLIGAN)
								.repliesTo(message.getId())
								.discardedCardIndices(Collections.singletonList(0))));
						break;
					case ON_REQUEST_ACTION:
						Assert.assertNotNull(message.getChanges());
						Assert.assertNotNull(message.getActions());
						Assert.assertNotNull(message.getActions().getActions());
						final int actionCount = message.getActions().getActions().size();
						Assert.assertTrue(actionCount > 0);
						// There should always be an end turn, choose one, discover or battlecry action
						Assert.assertTrue(message.getActions().getActions().stream().anyMatch(p -> EnumSet.of(
								ActionType.BATTLECRY,
								ActionType.DISCOVER,
								ActionType.END_TURN
						).contains(p.getActionType())));
						// Pick a random action
						endpoint.sendMessage(serialize(new ClientToServerMessage()
								.messageType(MessageType.UPDATE_ACTION)
								.repliesTo(message.getId())
								.actionIndex(random(actionCount))));
						break;
					case ON_GAME_END:
						// The game has ended.
						try {
							endpoint.userSession.close();
						} catch (IOException ignored) {
						}
						this.gameOver = true;
						if (onGameOver != null) {
							onGameOver.handle(this);
						}
						break;

				}
			});

			endpoint.sendMessage(serialize(mqpr.getUnityConnection().getFirstMessage()));

		} catch (ApiException | URISyntaxException e) {
			Assert.fail(e.getMessage());
		}
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

}
