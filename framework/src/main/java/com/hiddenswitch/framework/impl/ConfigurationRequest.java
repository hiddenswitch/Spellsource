package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import io.opentracing.SpanContext;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.decks.CollectionDeck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Indicates a configuration for a new game.
 */
public final class ConfigurationRequest implements Serializable {
	private String gameId;
	private List<Configuration> configurations = new ArrayList<>();
	private long noActivityTimeout = Games.getDefaultNoActivityTimeout();
	@JsonDeserialize(using = SpanContextDeserializer.class)
	@JsonSerialize(using = SpanContextSerializer.class)
	private SpanContext spanContext;

	public ConfigurationRequest() {
	}

	public static ConfigurationRequest botMatch(String gameId, String userId, String botId, String deckId, String botDeckId) {
		return new ConfigurationRequest()
				.setGameId(gameId)
				.setConfigurations(Arrays.asList(
						new Configuration()
								.setPlayerId(0)
								.setUserId(userId)
								.setBot(false)
								.setDeck(new CollectionDeck(deckId)),
//								.setPlayerAttributes(new AttributeMap()),
						new Configuration()
								.setPlayerId(1)
								.setUserId(botId)
								.setBot(true)
								.setDeck(new CollectionDeck(botDeckId))
//								.setPlayerAttributes(new AttributeMap())
				));
	}

	public static ConfigurationRequest versusMatch(String gameId, String userId1, String deckId1, String userId2, String deckId2) {
		return new ConfigurationRequest()
				.setGameId(gameId)
				.setConfigurations(Arrays.asList(
						new Configuration()
								.setPlayerId(0)
								.setUserId(userId1)
								.setBot(false)
								.setDeck(new CollectionDeck(deckId1)),
//								.setPlayerAttributes(new AttributeMap()),
						new Configuration()
								.setPlayerId(1)
								.setUserId(userId2)
								.setBot(false)
								.setDeck(new CollectionDeck(deckId2))
//								.setPlayerAttributes(new AttributeMap())
				));
	}

	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public ConfigurationRequest setConfigurations(List<Configuration> configurations) {
		this.configurations = configurations;
		return this;
	}

	public String getGameId() {
		return gameId;
	}

	public ConfigurationRequest setGameId(String gameId) {
		this.gameId = gameId;
		return this;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("gameId", gameId)
				.add("configurations", configurations)
				.add("noActivityTimeout", noActivityTimeout)
				.add("spanContext", spanContext)
				.toString();
	}

	public long getNoActivityTimeout() {
		return noActivityTimeout;
	}

	public ConfigurationRequest setNoActivityTimeout(long noActivityTimeout) {
		this.noActivityTimeout = noActivityTimeout;
		return this;
	}

	public SpanContext getSpanContext() {
		return spanContext;
	}

	public ConfigurationRequest setSpanContext(SpanContext spanContext) {
		this.spanContext = spanContext;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConfigurationRequest that = (ConfigurationRequest) o;
		return noActivityTimeout == that.noActivityTimeout &&
				Objects.equals(gameId, that.gameId) &&
				Objects.equals(configurations, that.configurations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gameId, configurations, noActivityTimeout);
	}
}
