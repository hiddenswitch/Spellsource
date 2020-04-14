package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SetRaceSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class SetRaceFromAttributeSpell extends SetRaceSpell {

    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        SpellDesc newDesc = desc.clone();
        Attribute attribute = desc.getAttribute();
        Entity realTarget = target;
        if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
            realTarget = context.resolveSingleTarget(desc.getSecondaryTarget());
        }
        newDesc.put(SpellArg.RACE, realTarget.getAttribute(desc.getAttribute()));
        super.onCast(context, player, desc, source, target);
    }
}
