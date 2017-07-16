package com.hiddenswitch.spellsource.tasks;

import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.client.models.MatchmakingDeck;
import com.hiddenswitch.spellsource.client.models.MatchmakingQueuePutRequest;
import com.hiddenswitch.spellsource.client.models.MatchmakingQueuePutResponse;
import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;
import com.hiddenswitch.spellsource.util.Serialization;
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
	private final String deckId;
	private boolean isCasual = false;
	private ClientConnectionConfiguration connection;
	private final AtomicBoolean isMatchmaking = new AtomicBoolean();

	public MatchmakingTask(String deckId) {
		this.deck = null;
		this.deckId = deckId;
	}

	public MatchmakingTask(Deck deck) {
		this.deck = deck;
		this.deckId = null;
	}

	public MatchmakingTask(String deckId, boolean isCasual) {
		this.deck = null;
		this.deckId = deckId;
		this.isCasual = isCasual;
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
			if (deck != null) {
				final List<String> cardIds = this.deck.getCards().toList().stream()
						.map(Card::getCardId)
						.collect(Collectors.toList());

				// Legacy deck for now
				request.setDeck(new MatchmakingDeck().cards(cardIds).heroClass(deck.getHeroClass().toString()));
			}

			if (deckId != null) {
				request.setDeckId(deckId);
			}

			request.setCasual(isCasual);

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
				connection = Serialization.deserializeBase64(response.getConnection().getJavaSerialized());
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
