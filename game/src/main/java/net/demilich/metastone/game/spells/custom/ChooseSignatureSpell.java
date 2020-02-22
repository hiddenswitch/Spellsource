package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class ChooseSignatureSpell extends Spell {

    @Suspendable
    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        // Find all the cards which started in the opponent's deck.
        Map<String, List<Entity>> deckCards = context.getEntities()
                .filter(e -> e.getOwner() == player.getId())
                .filter(e -> e.getEntityType() == EntityType.CARD)
                .filter(e -> e.hasAttribute(Attribute.STARTED_IN_DECK))
                .collect(groupingBy(e -> e.getSourceCard().getHeroClass()));

        CardList validChoices = new CardArrayList(deckCards.values().stream().flatMap(Collection::stream).map(Entity::getSourceCard)
                .filter(card -> card.getCardType() == CardType.SPELL).collect(Collectors.toSet()));
        validChoices.removeIf(card -> card.getCardId().equals("passive_signature"));

        Card choice = SpellUtils.discoverCard(context, player, source, desc, validChoices).getCard();

        player.setAttribute(Attribute.SIGNATURE, choice.getCardId());
    }


}
