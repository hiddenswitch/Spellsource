package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class OverkillSpell extends DamageSpell {

    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        if (desc.containsKey(SpellArg.VALUE)) {
            super.onCast(context, player, desc, source, target);
        }
        if (target instanceof Minion) {
            Minion minion = (Minion) target;
            if (minion.getHp() < 0 && minion.isDestroyed()) {
                //fire an overkill event?
                SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
                context.getLogic().castSpell(player.getId(), spell, source.getReference(), target.getReference(), true);
            }
        }
    }
}
