package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.desc.trigger.EnchantmentDesc;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.targeting.EntityReference;

public class ImmolateSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EnchantmentDesc trigger = new EnchantmentDesc();
		EventTriggerDesc eventTriggerDesc = new EventTriggerDesc(TurnEndTrigger.class);
		eventTriggerDesc.put(EventTriggerArg.TARGET_PLAYER, TargetPlayer.SELF);
		trigger.eventTrigger = eventTriggerDesc;
		trigger.maxFires = 1;
		trigger.spell = DamageSpell.create(target.getReference(), desc.getValue(SpellArg.VALUE, context, player, target, source, 3));
		SpellDesc subSpell = AddEnchantmentSpell.create(EntityReference.FRIENDLY_PLAYER, trigger);
		SpellUtils.castChildSpell(context, player, subSpell, source, target);
	}
}
