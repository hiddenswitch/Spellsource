package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class WeaponDamageSpell extends DamageSpell {


    @Suspendable
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        if (player.getWeaponZone().isEmpty()) {
            return;
        }
        desc.put(SpellArg.IGNORE_SPELL_DAMAGE, true);
        desc.put(SpellArg.VALUE, player.getWeaponZone().get(0).getAttack());
        super.onCast(context, player, desc, player.getHero(), target);
                        //the main purpose of this spell ^ setting the source to the hero, rather than the player
    }


}
