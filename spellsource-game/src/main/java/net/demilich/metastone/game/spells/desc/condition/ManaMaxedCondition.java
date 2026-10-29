package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.spells.Default;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;

/**
 * {@code true} if the player has {@link GameLogic#MAX_MANA} mana.
 */
public class ManaMaxedCondition extends Condition {

	public ManaMaxedCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			@Default(GameLogic.MAX_MANA) Integer value) {
		int maxMana = value != null ? value : context.getLogic().getMaxManaCap(player);
		return player.getMaxMana() >= maxMana || player.getMana() >= maxMana;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
