package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.trigger.Enchantment;
import net.demilich.metastone.game.spells.trigger.Trigger;
import net.demilich.metastone.game.spells.trigger.TurnEndTrigger;
import net.demilich.metastone.game.spells.trigger.TurnStartTrigger;

import java.util.List;

/**
 * Filters minions based on whether they have Start-of-turn/End-of-turn effects or not.
 */
public class HasTurnEffectsFilter extends EntityFilter {

    public HasTurnEffectsFilter(EntityFilterDesc desc) {
        super(desc);
    }

    @Override
    protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
	    List<Trigger> triggers = context.getLogic().getActiveTriggers(entity.getReference());
        for (Class triggerClass : new Class[]{TurnStartTrigger.class, TurnEndTrigger.class}) {
            for (Trigger trigger : triggers) {
                if (trigger instanceof Enchantment && !(trigger instanceof Aura)) {
                    Enchantment enchantment = (Enchantment) trigger;
                    if (enchantment.getTriggers().stream().anyMatch(eT ->
                            eT.getClass().equals(triggerClass))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
