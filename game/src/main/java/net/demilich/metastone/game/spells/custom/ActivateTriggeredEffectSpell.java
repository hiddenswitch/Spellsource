package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.EventTrigger;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.List;

public class ActivateTriggeredEffectSpell extends Spell {

    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        EventTriggerDesc eventTriggerDesc = (EventTriggerDesc) desc.get(SpellArg.REVERT_TRIGGER);
        List<Trigger> triggers = context.getTriggersAssociatedWith(target.getReference());
        for (Trigger trigger : triggers) {

            if (trigger instanceof Enchantment) {
                Enchantment enchantment = (Enchantment) trigger;
                if (enchantment.getTriggers().stream().anyMatch(eT ->
                        eT.getClass().equals(eventTriggerDesc.get(EventTriggerArg.CLASS)))) {

                    if (context.getLogic().hasAttribute(player, Attribute.DOUBLE_END_TURN_TRIGGERS) &&
                    eventTriggerDesc.get(EventTriggerArg.CLASS).equals(TurnEndTrigger.class)) {
                        context.getLogic().castSpell(target.getOwner(), enchantment.getSpell(),
                                target.getReference(), EntityReference.NONE, true);
                    }
                    context.getLogic().castSpell(target.getOwner(), enchantment.getSpell(),
                            target.getReference(), EntityReference.NONE, true);
                }
            }
        }

    }
}
