package net.demilich.metastone.game.logic;

import net.demilich.metastone.game.events.HasValue;

/**
 * Records the result of a heal.
 */
public class HealingResult implements HasValue {
	private final int healing;
	private final int excess;

	public HealingResult(int healing, int excess) {
		this.healing = healing;
		this.excess = excess;
	}

	public int getHealing() {
		return healing;
	}

	@Override
	public int getValue() {
		return getHealing();
	}

	public int getExcess() {
		return excess;
	}
}
