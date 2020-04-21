package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;

/**
 * {@code true} when the {@code player} has a weapon that is not broken.
 */
public class HasWeaponCondition extends Condition {

	public HasWeaponCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		if (player.getWeaponZone().isEmpty()) {
			return false;
		}
		var weapon = player.getWeaponZone().get(0);
		if (weapon.isBroken()) {
			return false;
		}
		String cardId = (String) desc.get(ConditionArg.CARD);
		if (cardId != null && !weapon.getSourceCard().getCardId().contains(cardId)) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
