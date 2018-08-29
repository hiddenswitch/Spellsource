package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.HeroPowerEffectTriggeredEvent;
import net.demilich.metastone.game.spells.MetaSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Signifies that the subspells {@link SpellArg#SPELL}, {@link SpellArg#SPELLS}, etc.) represent the "hero power effect"
 * for cards that interact with the hero power.
 */
public final class HeroPowerSpell extends MetaSpell {


    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        super.onCast(context, player, desc, source, target);
        context.fireGameEvent(new HeroPowerEffectTriggeredEvent(context, player.getId(), source.getSourceCard()));
    }
}
