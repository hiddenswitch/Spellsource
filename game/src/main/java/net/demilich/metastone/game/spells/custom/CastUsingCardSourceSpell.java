package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;

public class CastUsingCardSourceSpell extends Spell {
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        CardList cards = SpellUtils.getCards(context, player, target, source, desc, 99);
        if (desc.getBool(SpellArg.RANDOM_TARGET)) {
            cards.shuffle(context.getLogic().getRandom());
        }

        int howMany = (int) desc.getOrDefault(SpellArg.HOW_MANY, 1);
        if (cards.isEmpty()) {
            return;
        }

        for (int i = 0; i < howMany; i++) {
            Card card = cards.get(i).clone();
            card.setId(context.getLogic().generateId());
            card.setOwner(player.getId());
            card.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
            SpellUtils.castChildSpell(context, player, desc.getSpell(), source, target, card);
            card.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
        }

    }
}
