package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class StoreRaceToAttributeSpell extends Spell {

    @Suspendable
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        String race = (String) desc.get(SpellArg.RACE);
        Attribute attribute = desc.getAttribute();
        Entity realTarget = target;
        if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
            realTarget = context.resolveSingleTarget(desc.getSecondaryTarget());
        }
        realTarget.setAttribute(attribute, race);
    }
}
