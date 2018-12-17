package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.weapons.Weapon;

public class HasWeaponCondition extends Condition {

	private static final long serialVersionUID = -5199489404790742685L;

	public HasWeaponCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		Weapon weapon = player.getHero().getWeapon();
		if (weapon == null || weapon.isBroken()) {
			return false;
		}
		String cardId = (String) desc.get(ConditionArg.CARD);
		if (cardId != null && !weapon.getSourceCard().getCardId().contains(cardId)) {
			return false;
		}
		return true;
	}

}
