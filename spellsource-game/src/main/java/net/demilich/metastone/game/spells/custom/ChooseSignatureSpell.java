package net.demilich.metastone.game.spells.custom;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.*;
import java.util.stream.Collectors;

public class ChooseSignatureSpell extends Spell {


    @Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        // Find all the cards which started in your deck.
        List<Card> allDeckCards = context.getEntities()
                .filter(e -> e.getOwner() == player.getId())
                .filter(e -> e.getEntityType() == EntityType.CARD)
                .filter(e -> e.hasAttribute(Attribute.STARTED_IN_DECK))
                .map(e -> e.getSourceCard())
                .collect(Collectors.toList());

        CardList validChoices = new CardArrayList(allDeckCards.stream()
                .filter(card -> card.getCardType() == CardType.SPELL).sorted(Comparator.comparingInt(Card::getBaseManaCost)).map(Card::getCardId)
                .distinct().map(context::getCardById).collect(Collectors.toSet()));

        validChoices.removeIf(card -> card.getCardId().equals("passive_signature"));
        SpellUtils.discoverCard(context, player, source, desc, validChoices);
    }


}
