package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.*;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.List;

public class ActivateTriggeredEffectSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Trigger> triggers = context.getTriggersAssociatedWith(target.getReference());
		for (Class triggerClass : new Class[]{TurnStartTrigger.class, TurnEndTrigger.class}) {
			for (Trigger trigger : triggers) {

				if (trigger instanceof Enchantment && !(trigger instanceof Aura)) {
					Enchantment enchantment = (Enchantment) trigger;
					if (enchantment.getTriggers().stream().anyMatch(eT ->
							eT.getClass().equals(triggerClass))) {
						// Correctly set the trigger stacks
						context.getTriggerHostStack().push(target.getReference());
						if (context.getLogic().hasAttribute(player, Attribute.DOUBLE_END_TURN_TRIGGERS) &&
								triggerClass.equals(TurnEndTrigger.class)) {
							context.getLogic().castSpell(target.getOwner(), enchantment.getSpell(),
									target.getReference(), EntityReference.NONE, true);
						}

						context.getLogic().castSpell(target.getOwner(), enchantment.getSpell(),
								target.getReference(), EntityReference.NONE, true);
						context.getTriggerHostStack().pop();
					}
				}
			}
		}


	}
}
