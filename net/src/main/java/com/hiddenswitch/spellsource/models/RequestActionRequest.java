package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.common.GameState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.decks.DeckFormat;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 12/7/16.
 */
public class RequestActionRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public GameState gameState;
	public int playerId;
	public List<GameAction> validActions;
	public DeckFormat format;

	public RequestActionRequest(GameState gameState, int playerId, List<GameAction> validActions, DeckFormat format) {
		this.gameState = gameState;
		this.playerId = playerId;
		this.validActions = validActions;
		this.format = format;
	}
}
