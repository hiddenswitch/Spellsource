package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class SimulacrumSpell extends Spell {
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Card> cards = player.getHand()
				.stream()
				.filter(c -> c.getCardType() == CardType.MINION)
				.sorted(Comparator.comparingInt(c1 -> context.getLogic().getModifiedManaCost(player, c1)))
				.filter(new Predicate<Card>() {
					Integer first = null;

					@Override
					public boolean test(Card card) {
						if (first == null) {
							first = context.getLogic().getModifiedManaCost(player, card);
						}
						return context.getLogic().getModifiedManaCost(player, card) == first;
					}
				}).collect(toList());

		cards.stream().filter(new RandomSubsetSelector(cards.size(), 1, context.getLogic().getRandom()))
				.findFirst()
				.ifPresent(c -> context.getLogic().receiveCard(player.getId(), c.getCopy()));
	}
}
