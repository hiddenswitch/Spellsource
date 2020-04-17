package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SetRaceSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

public class SetRaceFromAttributeSpell extends SetRaceSpell {

    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        SpellDesc newDesc = desc.clone();
        Attribute attribute = desc.getAttribute();
        Entity realTarget = target;
        if (desc.containsKey(SpellArg.SECONDARY_TARGET)) {
            List<Entity> entities = context.resolveTarget(player, source, desc.getSecondaryTarget());
            realTarget = entities.get(0);
        }
        newDesc.put(SpellArg.RACE, realTarget.getAttribute(attribute));
        super.onCast(context, player, newDesc, source, target);
    }
}
