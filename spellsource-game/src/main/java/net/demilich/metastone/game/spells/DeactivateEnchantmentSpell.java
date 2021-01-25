package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.spells.trigger.Enchantment;

/**
 * Deactivates the enchantments hosted by the target or deactivates the {@code target} enchantment.
 */
public class DeactivateEnchantmentSpell extends AbstractModifyEnchantmentSpell {

	@Override
	protected void modifyEnchantment(Enchantment enchantment) {
		enchantment.setActivated(false);
	}
}
