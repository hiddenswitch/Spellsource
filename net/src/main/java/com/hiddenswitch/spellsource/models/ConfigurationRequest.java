package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.Games;
import com.hiddenswitch.spellsource.impl.DeckId;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.server.Configuration;
import net.demilich.metastone.game.decks.CollectionDeck;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.cards.AttributeMap;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bberman on 4/3/17.
 */
public class ConfigurationRequest implements Serializable {
	private GameId gameId;
	private List<Configuration> configurations = new ArrayList<>();
	private Trigger triggers;
	private long noActivityTimeout = Games.getDefaultNoActivityTimeout();

	public static ConfigurationRequest botMatch(GameId gameId, UserId userId, UserId botId, DeckId deckId, DeckId botDeckId) {
		return new ConfigurationRequest()
				.setGameId(gameId)
				.setConfigurations(Arrays.asList(
						new Configuration()
								.setPlayerId(0)
								.setUserId(userId)
								.setBot(false)
								.setDeck(new CollectionDeck(deckId.toString()))
								.setPlayerAttributes(new AttributeMap()),
						new Configuration()
								.setPlayerId(1)
								.setUserId(botId)
								.setBot(true)
								.setDeck(new CollectionDeck(botDeckId.toString()))
								.setPlayerAttributes(new AttributeMap())
				));
	}

	public static ConfigurationRequest versusMatch(GameId gameId, UserId userId1, DeckId deckId1, UserId userId2, DeckId deckId2) {
		return new ConfigurationRequest()
				.setGameId(gameId)
				.setConfigurations(Arrays.asList(
						new Configuration()
								.setPlayerId(0)
								.setUserId(userId1)
								.setBot(false)
								.setDeck(new CollectionDeck(deckId1.toString()))
								.setPlayerAttributes(new AttributeMap()),
						new Configuration()
								.setPlayerId(1)
								.setUserId(userId2)
								.setBot(false)
								.setDeck(new CollectionDeck(deckId2.toString()))
								.setPlayerAttributes(new AttributeMap())
				));
	}

	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public ConfigurationRequest setConfigurations(List<Configuration> configurations) {
		this.configurations = configurations;
		return this;
	}

	public GameId getGameId() {
		return gameId;
	}

	public ConfigurationRequest setGameId(GameId gameId) {
		this.gameId = gameId;
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public Trigger getTriggers() {
		return triggers;
	}

	public ConfigurationRequest setTriggers(Trigger triggers) {
		this.triggers = triggers;
		return this;
	}

	public long getNoActivityTimeout() {
		return noActivityTimeout;
	}

	public ConfigurationRequest setNoActivityTimeout(long noActivityTimeout) {
		this.noActivityTimeout = noActivityTimeout;
		return this;
	}
}
