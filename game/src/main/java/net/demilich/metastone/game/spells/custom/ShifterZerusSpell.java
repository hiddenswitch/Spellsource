package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

public class ShifterZerusSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = (Card) target;

		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		Card newCard = SpellUtils.getRandomCard(CardCatalogue.query(context.getDeckFormat()), filterCard -> cardFilter.matches(context, player, filterCard, source));
		context.getLogic().replaceCardInHand(player.getId(), card, newCard);
	}

}
