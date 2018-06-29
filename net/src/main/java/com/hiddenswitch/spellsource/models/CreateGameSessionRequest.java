package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.Games;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.server.Configuration;
import net.demilich.metastone.game.spells.trigger.Trigger;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class CreateGameSessionRequest extends ConfigurationRequest implements Serializable {
	private Trigger triggers;
	private long noActivityTimeout = Games.getDefaultNoActivityTimeout();

	public Trigger getTriggers() {
		return triggers;
	}

	public CreateGameSessionRequest setTriggers(Trigger triggers) {
		this.triggers = triggers;
		return this;
	}

	public long getNoActivityTimeout() {
		return noActivityTimeout;
	}

	public CreateGameSessionRequest setNoActivityTimeout(long noActivityTimeout) {
		this.noActivityTimeout = noActivityTimeout;
		return this;
	}

	@Override
	public CreateGameSessionRequest setConfigurations(List<Configuration> configurations) {
		super.setConfigurations(configurations);
		return this;
	}

	@Override
	public CreateGameSessionRequest setGameId(GameId gameId) {
		super.setGameId(gameId);
		return this;
	}
}
