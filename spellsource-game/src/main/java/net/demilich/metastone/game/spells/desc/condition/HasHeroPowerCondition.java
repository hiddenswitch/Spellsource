package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * {@code true} when the {@link ConditionArg#TARGET_PLAYER} skill is {@link ConditionArg#CARD}.
 */
public class HasHeroPowerCondition extends Condition {

	public HasHeroPowerCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			String card) {
		var heroPower = player.getHeroPowerZone().get(0);
		if (heroPower.getCardId() == null) {
			return false;
		}
		return heroPower.getCardId().equalsIgnoreCase(card);
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
