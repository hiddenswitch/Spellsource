package com.hiddenswitch.spellsource.net.models;

import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.net.impl.GameId;
import io.opentracing.SpanContext;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.decks.DeckFormat;

import java.io.Serializable;
import java.util.List;

/**
 * Encodes a request for a bot to make a move.
 */
public class RequestActionRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	final public GameId gameId;
	final public GameState gameState;
	final public int playerId;
	final public List<GameAction> validActions;
	final public DeckFormat format;
	final public SpanContext spanContext;

	public RequestActionRequest(GameId gameId, int playerId, List<GameAction> validActions, DeckFormat format, GameState gameState, SpanContext spanContext) {
		this.gameId = gameId;
		this.gameState = gameState;
		this.playerId = playerId;
		this.validActions = validActions;
		this.format = format;
		this.spanContext = spanContext;
	}
}
