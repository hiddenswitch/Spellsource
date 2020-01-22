package com.hiddenswitch.spellsource.net.impl;

import io.vertx.core.Handler;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.List;

/**
 * Indicates a kind of request for the client.
 */
public class GameplayRequest {
	private String callbackId;
	private Handler callback;
	private List<Card> starterCards;
	private List<GameAction> actions;
	private GameplayRequestType type;

	public String getCallbackId() {
		return callbackId;
	}

	public GameplayRequest setCallbackId(String callbackId) {
		this.callbackId = callbackId;
		return this;
	}

	public Handler getCallback() {
		return callback;
	}

	public GameplayRequest setCallback(Handler callback) {
		this.callback = callback;
		return this;
	}

	public List<Card> getStarterCards() {
		return starterCards;
	}

	public GameplayRequest setStarterCards(List<Card> starterCards) {
		this.starterCards = starterCards;
		return this;
	}

	public List<GameAction> getActions() {
		return actions;
	}

	public GameplayRequest setActions(List<GameAction> actions) {
		this.actions = actions;
		return this;
	}

	public GameplayRequestType getType() {
		return type;
	}

	public GameplayRequest setType(GameplayRequestType type) {
		this.type = type;
		return this;
	}
}
