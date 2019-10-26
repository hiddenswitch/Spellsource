package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.targeting.DamageType;

/**
 * Deals damage to the specified actor, bypassing its armor.
 */
public final class DamageIgnoringArmorSpell extends DamageSpell {

	@Override
	protected DamageType getDamageType() {
		return DamageType.IGNORES_ARMOR;
	}
}
