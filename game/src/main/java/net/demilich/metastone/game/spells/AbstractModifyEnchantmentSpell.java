package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;

/**
 * A base class for spells that modify the enchantments hosted by {@code target} or the {@code target} itself if it is
 * an enchantment.
 */
public abstract class AbstractModifyEnchantmentSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target.getEntityType() == EntityType.ENCHANTMENT) {
			var enchantment = (Enchantment) target;
			modifyEnchantment(enchantment);
		} else {
			var triggers = context.getLogic().getActiveTriggers(target.getReference());
			for (var trigger : triggers) {
				if (trigger instanceof Enchantment) {
					var enchantment = (Enchantment) trigger;
					modifyEnchantment(enchantment);
				}
			}
		}
	}

	protected void modifyEnchantment(Enchantment enchantment) {
		enchantment.setActivated(false);
	}
}
