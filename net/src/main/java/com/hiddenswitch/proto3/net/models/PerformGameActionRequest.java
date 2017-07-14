package com.hiddenswitch.proto3.net.models;

import net.demilich.metastone.game.actions.GameAction;

import java.io.Serializable;

public class PerformGameActionRequest implements Serializable {
	private String gameId;
	private GameAction action;
	private int playerId;

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public GameAction getAction() {
		return action;
	}

	public void setAction(GameAction action) {
		this.action = action;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
}
