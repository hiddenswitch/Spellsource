package com.hiddenswitch.framework.impl;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;

public final class CreateGameSessionResponse {

	public String userId1;
	public String userId2;
	public String gameId;
	public boolean pending;
	public String deploymentId;

	public static CreateGameSessionResponse pending(String deploymentId) {
		return new CreateGameSessionResponse(true, deploymentId);
	}

	public static CreateGameSessionResponse session(String deploymentId, ServerGameContext session) {
		return new CreateGameSessionResponse(deploymentId, session.getGameId(), new String(session.getPlayer1().getUserId()), new String(session.getPlayer2().getUserId()));
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

	private CreateGameSessionResponse(String deploymentId, String gameId, String configurationForPlayer1, String configurationForPlayer2) {
		pending = false;
		this.deploymentId = deploymentId;
		this.gameId = gameId;
		this.userId1 = configurationForPlayer1;
		this.userId2 = configurationForPlayer2;
	}

	@Deprecated
	public CreateGameSessionResponse(String userId1, String userId2, String gameId) {
		this.userId1 = userId1;
		this.userId2 = userId2;
		this.pending = false;
		this.deploymentId = null;
		this.gameId = gameId;
	}

	public String getUserId1() {
		return userId1;
	}

	public CreateGameSessionResponse setUserId1(String userId1) {
		this.userId1 = userId1;
		return this;
	}

	public String getUserId2() {
		return userId2;
	}

	public CreateGameSessionResponse setUserId2(String userId2) {
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
		if (!(o instanceof CreateGameSessionResponse)) return false;
		var that = (CreateGameSessionResponse) o;
		return isPending() == that.isPending() &&
				Objects.equals(getUserId1(), that.getUserId1()) &&
				Objects.equals(getUserId2(), that.getUserId2()) &&
				Objects.equals(getGameId(), that.getGameId()) &&
				Objects.equals(getDeploymentId(), that.getDeploymentId());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("userId1", userId1)
				.add("userId2", userId2)
				.add("gameId", gameId)
				.add("pending", pending)
				.add("deploymentId", deploymentId)
				.toString();
	}
}
