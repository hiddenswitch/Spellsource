package net.demilich.metastone.game.spells;

import co.paralleluniverse.common.util.Objects;
import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.DamageCausedTrigger;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetType;

import java.util.List;

/**
 * Gives a minion {@link net.demilich.metastone.game.cards.Attribute#WITHER} and the appropriate trigger if it does not
 * already have it.
 */
public final class ModifyWitherAttributeSpell extends ModifyAttributeSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		super.onCast(context, player, desc, source, target);
		List<Trigger> triggers = context.getLogic().getActiveTriggers(target.getReference());
		/*
		 *   {
		 *     "eventTrigger": {
		 *       "class": "DamageCausedTrigger",
		 *       "hostTargetType": "IGNORE_OTHER_SOURCES"
		 *     },
		 *     "spell": {
		 *       "class": "WitherSpell",
		 *       "target": "EVENT_TARGET"
		 *     }
		 *   }
		 */
		for (Trigger trigger : triggers) {
			if (isWitherEnchantment(trigger)) {
				return;
			}
		}
		EnchantmentDesc witherEnchantment = new EnchantmentDesc();
		witherEnchantment.setEventTrigger(new EventTriggerDesc(DamageCausedTrigger.class));
		witherEnchantment.getEventTrigger().put(EventTriggerArg.HOST_TARGET_TYPE, TargetType.IGNORE_OTHER_SOURCES);
		witherEnchantment.setSpell(new SpellDesc(WitherSpell.class, EntityReference.EVENT_TARGET, null, false));
		SpellUtils.castChildSpell(context, player, AddEnchantmentSpell.create(witherEnchantment), source, target);
	}

	private boolean isWitherEnchantment(Trigger trigger) {
		if (!(trigger instanceof Enchantment)) {
			return false;
		}
		Enchantment enchantment = (Enchantment) trigger;
		return enchantment.getTriggers().size() == 1
				&& enchantment.getTriggers().get(0).getClass().equals(DamageCausedTrigger.class)
				&& Objects.equal(enchantment.getTriggers().get(0).getDesc().get(EventTriggerArg.HOST_TARGET_TYPE), TargetType.IGNORE_OTHER_SOURCES)
				&& enchantment.getSpell() != null && enchantment.getSpell().getDescClass().equals(WitherSpell.class);
	}
}
