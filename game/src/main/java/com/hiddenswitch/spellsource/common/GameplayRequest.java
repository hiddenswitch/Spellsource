package com.hiddenswitch.spellsource.common;

import io.vertx.core.Handler;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.List;

/**
 * Created by bberman on 12/5/16.
 */
public class GameplayRequest {
	public String callbackId;
	public final Handler callback;
	public List<Card> starterCards;
	public GameplayRequestType type;
	public GameState state;
	public List<GameAction> actions;

	public GameplayRequest(String callbackId, GameplayRequestType type, GameState state, List<GameAction> actions, Handler callback) {
		this(type, state, actions, callback);
		this.callbackId = callbackId;
	}

	public GameplayRequest(GameplayRequestType type, GameState state, List<GameAction> actions, Handler callback) {
		this.type = type;
		this.callback = callback;
		this.state = state;
		this.actions = actions;
	}

	public GameplayRequest(String callbackId, GameplayRequestType type, List<Card> starterCards, Handler<List<Card>> callback) {
		this(type, starterCards, callback);
		this.callbackId = callbackId;
	}

	public GameplayRequest(GameplayRequestType type, List<Card> starterCards, Handler<List<Card>> callback) {
		this.type = type;
		this.starterCards = starterCards;
		this.callback = callback;
	}
}
