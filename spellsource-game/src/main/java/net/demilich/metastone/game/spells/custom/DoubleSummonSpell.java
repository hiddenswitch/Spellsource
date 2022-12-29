package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;


/**
 * A dummy spell used for a simpler implementation of Khadgar's effect with a {@link net.demilich.metastone.game.spells.aura.SpellOverrideAura}
 */
public class DoubleSummonSpell extends SummonSpell {

    @Override

    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        super.onCast(context, player, desc, source, target);
        super.onCast(context, player, desc, source, target);
    }
}
