package net.demilich.metastone.game.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains data about the player's mulligan required to reproduce their mulligan exactly.
 */
public class MulliganTrace implements Serializable, Cloneable {
	private int playerId;
	private List<Integer> entityIds;

	public MulliganTrace() {
	}

	public int getPlayerId() {
		return playerId;
	}

	public MulliganTrace setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}

	public List<Integer> getEntityIds() {
		return entityIds;
	}

	public MulliganTrace setEntityIds(List<Integer> entityIds) {
		this.entityIds = entityIds;
		return this;
	}

	@Override
	protected MulliganTrace clone() {
		MulliganTrace clone = null;
		try {
			clone = (MulliganTrace) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		if (entityIds!=null){
			clone.entityIds = new ArrayList<>(entityIds);
		}
		return clone;
	}
}
