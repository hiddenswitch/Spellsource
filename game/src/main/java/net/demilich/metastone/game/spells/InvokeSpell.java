package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class InvokeSpell extends Spell {

    @Suspendable
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        int manaRemaining = player.getMana();
        int invoke = Math.min(source.getAttributeValue(Attribute.INVOKE), source.getAttributeValue(Attribute.AURA_INVOKE));
        if (manaRemaining < invoke) {
            if (desc.containsKey(SpellArg.SPELL)) {
                SpellUtils.castChildSpell(context, player, desc.getSpell(), source, target);
            }
            return;
        }
        SpellDesc spell1 = (SpellDesc) desc.get(SpellArg.SPELL1);
        SpellDesc spell2 = (SpellDesc) desc.get(SpellArg.SPELL2);
        if (spell1.getSpell() == null) {
            spell1.put(SpellArg.SPELL, NullSpell.create());
        }
        if (spell2.getSpell() == null) {
            spell2.put(SpellArg.SPELL, NullSpell.create());
        }

        Card card1 = InvokeOptionSpell.getTempCard(context, spell1, source.getSourceCard());
        Card card2 = InvokeOptionSpell.getTempCard(context, spell2, source.getSourceCard());

        CardList cards = new CardArrayList();
        cards.add(card1);
        cards.add(card2);
        //add aura invoke cards

        cards.removeIf(card -> card.getBaseManaCost() > manaRemaining);

        desc.put(SpellArg.SPELL, NullSpell.create());
        DiscoverAction discoverAction = SpellUtils.discoverCard(context, player, source, desc, cards);

        SpellUtils.castChildSpell(context, player, discoverAction.getCard().getSpell(), source, target);
    }


}
