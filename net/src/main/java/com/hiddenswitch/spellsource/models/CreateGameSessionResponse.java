package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;
import com.hiddenswitch.spellsource.impl.server.GameSession;
import com.hiddenswitch.spellsource.util.DefaultClusterSerializable;
import com.hiddenswitch.spellsource.util.Serialization;
import com.hiddenswitch.spellsource.util.VertxBufferInputStream;
import com.hiddenswitch.spellsource.util.VertxBufferOutputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.shareddata.impl.ClusterSerializable;

import java.io.IOException;
import java.io.Serializable;

public final class CreateGameSessionResponse implements Shareable, Serializable, DefaultClusterSerializable {
	private static final long serialVersionUID = 1L;

	private final ClientConnectionConfiguration player1;
	private final ClientConnectionConfiguration player2;
	private final String gameId;
	private final boolean pending;
	private final String deploymentId;

	public CreateGameSessionResponse(boolean b, String deploymentId) {
		pending = b;
		this.deploymentId = deploymentId;
		player1 = null;
		player2 = null;
		gameId = null;
	}

	public CreateGameSessionResponse(String deploymentId, String gameId, ClientConnectionConfiguration configurationForPlayer1, ClientConnectionConfiguration configurationForPlayer2) {
		pending = false;
		this.deploymentId = deploymentId;
		this.gameId = gameId;
		this.player1 = configurationForPlayer1;
		this.player2 = configurationForPlayer2;
	}

	public static CreateGameSessionResponse session(String deploymentId, GameSession session) {
		return new CreateGameSessionResponse(deploymentId, session.getGameId(), session.getConfigurationForPlayer1(), session.getConfigurationForPlayer2());
	}

	public CreateGameSessionResponse(ClientConnectionConfiguration player1, ClientConnectionConfiguration player2, String gameId) {
		this.player1 = player1;
		this.player2 = player2;
		this.pending = false;
		this.deploymentId = null;
		this.gameId = gameId;
	}

	public ClientConnectionConfiguration getConfigurationForPlayer1() {
		return player1;
	}

	public ClientConnectionConfiguration getConfigurationForPlayer2() {
		return player2;
	}

	public String getGameId() {
		return gameId;
	}

	public static CreateGameSessionResponse pending(String deploymentId) {
		return new CreateGameSessionResponse(true, deploymentId);
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public boolean isPending() {
		return pending;
	}
}
