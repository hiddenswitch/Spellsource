package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * {@code true} when the {@code player} has a weapon that is not broken.
 */
public class HasWeaponCondition extends Condition {

	public HasWeaponCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			String card) {
		if (player.getWeaponZone().isEmpty()) {
			return false;
		}
		var weapon = player.getWeaponZone().get(0);
		if (weapon.isBroken()) {
			return false;
		}
		return card == null || weapon.getSourceCard().getCardId().contains(card);
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
