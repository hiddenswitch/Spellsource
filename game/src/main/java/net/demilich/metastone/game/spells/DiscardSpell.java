package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.HandSource;
import net.demilich.metastone.game.targeting.EntityReference;

public class DiscardSpell extends Spell {

	public static final int ALL_CARDS = -1;

	public static SpellDesc create() {
		return create(1);
	}

	public static SpellDesc create(int numberOfCards) {
		Map<SpellArg, Object> arguments = SpellDesc.build(DiscardSpell.class);
		arguments.put(SpellArg.VALUE, numberOfCards);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardSource cardSource = (CardSource) desc.getOrDefault(SpellArg.CARD_SOURCE, HandSource.create());
		EntityFilter cardFilter = desc.getCardFilter();
		if (cardFilter == null) {
			cardFilter = AndFilter.create();
		}
		int numberOfCards = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);

		CardList discardableCards = cardSource.getCards(context, source, player).filtered(cardFilter.matcher(context, player, source));
		int cardCount = numberOfCards == ALL_CARDS ? discardableCards.getCount() : numberOfCards;

		for (int i = 0; i < cardCount; i++) {
			Card randomCard = context.getLogic().getRandom(discardableCards);
			if (randomCard == null) {
				return;
			}
			context.getLogic().discardCard(player, randomCard);
			discardableCards.remove(randomCard);
		}
	}

}
