package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.common.ClientConnectionConfiguration;

import java.io.Serializable;

public class CreateGameSessionResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	private ClientConnectionConfiguration player1;
	private ClientConnectionConfiguration player2;
	private final String gameId;

	public CreateGameSessionResponse(ClientConnectionConfiguration player1, ClientConnectionConfiguration player2, String gameId) {
		setPlayer1(player1);
		setPlayer2(player2);
		this.gameId = gameId;
	}

	public ClientConnectionConfiguration getConfigurationForPlayer1() {
		return player1;
	}

	public ClientConnectionConfiguration getConfigurationForPlayer2() {
		return player2;
	}

	public void setPlayer1(ClientConnectionConfiguration player1) {
		this.player1 = player1;
	}

	public void setPlayer2(ClientConnectionConfiguration player2) {
		this.player2 = player2;
	}

	public String getGameId() {
		return gameId;
	}
}
