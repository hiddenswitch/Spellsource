package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.events.FatigueEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class FatigueSpell extends Spell {

    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        int times = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
        for (int i = 0; i < times; i++) {
            Hero hero = player.getHero();
            int fatigue = player.hasAttribute(Attribute.FATIGUE) ? player.getAttributeValue(Attribute.FATIGUE) : 0;
            fatigue++;
            player.setAttribute(Attribute.FATIGUE, fatigue);
            if (!player.hasAttribute(Attribute.DISABLE_FATIGUE)) {
                context.getLogic().damage(player, hero, fatigue, hero);
                context.fireGameEvent(new FatigueEvent(context, player.getId(), fatigue));
                player.getStatistics().fatigueDamage(fatigue);
            }
        }
    }

}
