package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.utils.AttributeMap;

public class ShuffleToDeckSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = null;
		AttributeMap map = new AttributeMap();
		SpellDesc subSpell = (SpellDesc) (desc.getOrDefault(SpellArg.SPELL, NullSpell.create()));
		if (target != null) {
			// Implements Kingsbane in a very basic way, since weapons pretty much only get enchanted for attack,
			// durability, windfury, lifesteal and poisonous bonuses.
			map = SpellUtils.processKeptEnchantments(target, map);
			card = target.getSourceCard().getCopy();
		} else if (desc.containsKey(SpellArg.CARD_FILTER)) {
			EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
			CardList cards = CardCatalogue.query(context.getDeckFormat());
			CardList result = new CardArrayList();
			for (Card cardResult : cards) {
				if (cardFilter.matches(context, player, cardResult, source)) {
					result.addCard(cardResult);
				}
			}
			card = context.getLogic().getRandom(result);
		} else {
			String cardId = (String) desc.get(SpellArg.CARD);
			card = context.getCardById(cardId);
		}

		int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		for (int i = 0; i < howMany; i++) {
			if (card != null) {
				final Card copy = card.getCopy();
				copy.getAttributes().putAll(map);
				context.getLogic().shuffleToDeck(player, copy);
				SpellUtils.castChildSpell(context, player, subSpell, source, target, copy);
			}
		}
	}
}
