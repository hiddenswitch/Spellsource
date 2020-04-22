package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * {@code true} if the {@code player} has overloaded (locked) any Lun.
 *
 * @see OverloadedCondition if the player has overloaded this turn (yet the Lun is not yet locked).
 */
public class OverloadedManaCrystalsCondition extends Condition {
	public OverloadedManaCrystalsCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return player.getLockedMana() > 0;
	}
}
