package com.hiddenswitch.proto3.net.models;

import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.entities.Entity;

import java.io.Serializable;
import java.util.List;

public class PerformGameActionRequest implements Serializable {
	private String gameId;
	private GameAction action;
	private int playerId;
	private List<Entity> entities;

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

	public List<Entity> getEntities() {
		return entities;
	}

	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}
}
