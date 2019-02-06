package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.impl.GameId;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.decks.DeckFormat;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 12/7/16.
 */
public class RequestActionRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	final public GameId gameId;
	final public GameState gameState;
	final public int playerId;
	final public List<GameAction> validActions;
	final public DeckFormat format;

	public RequestActionRequest(GameId gameId, int playerId, List<GameAction> validActions, DeckFormat format, GameState gameState) {
		this.gameId = gameId;
		this.gameState = gameState;
		this.playerId = playerId;
		this.validActions = validActions;
		this.format = format;
	}
}
