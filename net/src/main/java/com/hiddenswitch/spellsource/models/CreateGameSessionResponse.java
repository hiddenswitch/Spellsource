package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.ServerGameContext;
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

	public static CreateGameSessionResponse session(String deploymentId, ServerGameContext session) {
		return new CreateGameSessionResponse(deploymentId, session.getGameId(), new UserId(session.getPlayer1().getUserId()), new UserId(session.getPlayer2().getUserId()));
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

	public UserId getUserId1() {
		return userId1;
	}

	public CreateGameSessionResponse setUserId1(UserId userId1) {
		this.userId1 = userId1;
		return this;
	}

	public UserId getUserId2() {
		return userId2;
	}

	public CreateGameSessionResponse setUserId2(UserId userId2) {
		this.userId2 = userId2;
		return this;
	}

	public String getGameId() {
		return gameId;
	}

	public CreateGameSessionResponse setGameId(String gameId) {
		this.gameId = gameId;
		return this;
	}

	public boolean isPending() {
		return pending;
	}

	public CreateGameSessionResponse setPending(boolean pending) {
		this.pending = pending;
		return this;
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public CreateGameSessionResponse setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		CreateGameSessionResponse that = (CreateGameSessionResponse) o;

		return new EqualsBuilder()
				.append(pending, that.pending)
				.append(userId1, that.userId1)
				.append(userId2, that.userId2)
				.append(gameId, that.gameId)
				.append(deploymentId, that.deploymentId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(userId1)
				.append(userId2)
				.append(gameId)
				.append(pending)
				.append(deploymentId)
				.toHashCode();
	}
}
