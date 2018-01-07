package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.server.GameSession;
import com.hiddenswitch.spellsource.util.DefaultClusterSerializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public final class CreateGameSessionResponse implements Serializable, DefaultClusterSerializable {
	private static final long serialVersionUID = 1L;

	public UserId userId1;
	public UserId userId2;
	public String gameId;
	public boolean pending;
	public String deploymentId;

	public static CreateGameSessionResponse pending(String deploymentId) {
		return new CreateGameSessionResponse(true, deploymentId);
	}

	public static CreateGameSessionResponse session(String deploymentId, GameSession session) {
		return new CreateGameSessionResponse(deploymentId, session.getGameId(), session.getUserIds().get(0), session.getUserIds().get(1));
	}

	public CreateGameSessionResponse() {
		userId1 = null;
		userId2 = null;
		gameId = null;
		pending = false;
		deploymentId = null;
	}

	private CreateGameSessionResponse(boolean b, String deploymentId) {
		pending = b;
		this.deploymentId = deploymentId;
		userId1 = null;
		userId2 = null;
		gameId = null;
	}

	private CreateGameSessionResponse(String deploymentId, String gameId, UserId configurationForPlayer1, UserId configurationForPlayer2) {
		pending = false;
		this.deploymentId = deploymentId;
		this.gameId = gameId;
		this.userId1 = configurationForPlayer1;
		this.userId2 = configurationForPlayer2;
	}

	@Deprecated
	public CreateGameSessionResponse(UserId userId1, UserId userId2, String gameId) {
		this.userId1 = userId1;
		this.userId2 = userId2;
		this.pending = false;
		this.deploymentId = null;
		this.gameId = gameId;
	}

	@Override
	public boolean equals(Object obj) {
		CreateGameSessionResponse rhs = (CreateGameSessionResponse) obj;
		return new EqualsBuilder()
				.append(gameId, rhs.gameId)
				.append(pending, rhs.pending)
				.append(deploymentId, rhs.deploymentId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(gameId)
				.append(pending)
				.append(deploymentId)
				.toHashCode();
	}
}
