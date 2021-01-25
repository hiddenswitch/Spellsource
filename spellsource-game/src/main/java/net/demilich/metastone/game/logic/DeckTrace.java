package net.demilich.metastone.game.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains data about a player's deck required to reproduce, exactly, the player's match.
 */
public class DeckTrace implements Serializable, Cloneable {
	private int playerId;
	private List<String> cardIds;

	public DeckTrace() {
	}

	public int getPlayerId() {
		return playerId;
	}

	public DeckTrace setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}

	public List<String> getCardIds() {
		return cardIds;
	}

	public DeckTrace setCardIds(List<String> cardIds) {
		this.cardIds = cardIds;
		return this;
	}

	@Override
	protected DeckTrace clone() {
		DeckTrace clone = null;
		try {
			clone = (DeckTrace) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		clone.cardIds = new ArrayList<>(cardIds);
		return clone;
	}
}
