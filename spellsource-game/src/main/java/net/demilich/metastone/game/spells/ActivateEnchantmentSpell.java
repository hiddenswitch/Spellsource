package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.spells.trigger.Enchantment;

/**
 * Activates the enchantments hosted by the target or the enchantment targeted in {@code target}
 */
public class ActivateEnchantmentSpell extends AbstractModifyEnchantmentSpell {

	@Override
	protected void modifyEnchantment(Enchantment enchantment) {
		enchantment.setActivated(true);
	}
}


