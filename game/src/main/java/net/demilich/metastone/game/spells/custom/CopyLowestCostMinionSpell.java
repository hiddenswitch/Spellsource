package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.client.models.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.CopyCardSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.CardSource;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Copies the lowest cost {@link Card} of type {@link CardType#MINION} in the player's hand. Chooses the leftmost such
 * card.
 */
public final class CopyLowestCostMinionSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardSource sourceCards = desc.getCardSource();
		List<Card> cards = sourceCards.getCards(context, source, player)
				.stream()
				.filter(c -> c.getCardType() == CardType.MINION)
				.sorted(Comparator.comparingInt((Card c1) -> context.getLogic().getModifiedManaCost(player, c1))
						.thenComparingInt((Card c2) -> c2.getEntityLocation().getIndex()))
				.collect(toList());

		if (cards.isEmpty()) {
			return;
		}

		Card first = cards.get(0);
		int cost = context.getLogic().getModifiedManaCost(player, first);
		cards = cards.stream().filter(c -> context.getLogic().getModifiedManaCost(player, c) == cost).collect(toList());

		Card card = context.getLogic().getRandom(cards);
		SpellDesc copyCard = CopyCardSpell.create(card);
		SpellUtils.castChildSpell(context, player, copyCard, source, card, card);
	}
}

