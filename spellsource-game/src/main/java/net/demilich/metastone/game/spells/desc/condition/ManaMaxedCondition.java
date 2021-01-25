package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;

/**
 * {@code true} if the player has {@link GameLogic#MAX_MANA} mana.
 */
public class ManaMaxedCondition extends Condition {

	public ManaMaxedCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return (player.getMaxMana() >= GameLogic.MAX_MANA || player.getMana() >= GameLogic.MAX_MANA);
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
