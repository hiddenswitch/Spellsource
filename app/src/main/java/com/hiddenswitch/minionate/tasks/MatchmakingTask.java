package com.hiddenswitch.minionate.tasks;

import com.hiddenswitch.proto3.net.client.ApiClient;
import com.hiddenswitch.proto3.net.client.ApiException;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.api.DefaultApi;
import com.hiddenswitch.proto3.net.client.auth.ApiKeyAuth;
import com.hiddenswitch.proto3.net.client.models.MatchmakingDeck;
import com.hiddenswitch.proto3.net.client.models.MatchmakingQueuePutRequest;
import com.hiddenswitch.proto3.net.client.models.MatchmakingQueuePutResponse;
import com.hiddenswitch.proto3.net.common.ClientConnectionConfiguration;
import com.hiddenswitch.proto3.net.util.Serialization;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import javafx.concurrent.Task;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.Deck;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by bberman on 12/2/16.
 */
public class MatchmakingTask extends Task<Void> {
	private final Deck deck;
	private ClientConnectionConfiguration connection;
	private final String userId;
	private final AtomicBoolean isMatchmaking;

	public MatchmakingTask(String userId, Deck deck) {
		this.userId = userId;
		this.deck = deck;
		this.isMatchmaking = new AtomicBoolean();
	}

	public void stop() {
		isMatchmaking.set(false);
	}

	@Override
	protected Void call() throws Exception {
		Logger logger = LoggerFactory.getLogger(MatchmakingTask.class);
		this.isMatchmaking.set(true);
		try {
			DefaultApi api = new DefaultApi();

			// Make the first request
			MatchmakingQueuePutRequest request = new MatchmakingQueuePutRequest();
			final List<String> cardIds = this.deck.getCards().toList().stream()
					.map(Card::getCardId)
					.collect(Collectors.toList());

			// Legacy deck for now
			request.setDeck(new MatchmakingDeck().cards(cardIds).heroClass(deck.getHeroClass().toString()));
			MatchmakingQueuePutResponse response = null;
			try {
				response = api.matchmakingConstructedQueuePut(request);
			} catch (ApiException e) {
				if (e.getCode() == 401) {
					logger.error("Unauthorized matchmaking. Did the client log in?");
					this.isMatchmaking.set(false);
				}
			}

			while (isMatchmaking.get()
					&& response != null
					&& response.getConnection() == null) {
				logger.debug("Retrying multiplayer connection...");
				Thread.sleep(2000);
				request = response.getRetry();
				response = api.matchmakingConstructedQueuePut(request);
			}

			if (!isMatchmaking.get()
					&& (response == null || response.getConnection() == null)) {
				logger.debug("Canceling matchmaking.");
				api.matchmakingConstructedQueueDelete();
			} else {
				logger.debug("Matchmaking successful!");
				connection = Serialization.deserialize(response.getConnection().getJavaSerialized());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return null;
	}

	public ClientConnectionConfiguration getConnection() {
		return connection;
	}
}
