package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;

public class RevealCardSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {

		CardList filteredCards = new CardArrayList();
		if (desc.containsKey(SpellArg.CARD_FILTER) || desc.containsKey(SpellArg.CARD_SOURCE)) {
			filteredCards = desc.getFilteredCards(context, player, source);
		} else if (target instanceof Card
				&& (target.getZone() == Zones.HAND || target.getZone() == Zones.DECK)) {
			filteredCards.add((Card) target);
		}
		if (filteredCards.size() == 0) {
			return;
		}

		Card cardToReveal = context.getLogic().getRandom(filteredCards);
		context.getLogic().revealCard(player, cardToReveal);
		SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		if (subSpell == null) {
			return;
		}
		SpellUtils.castChildSpell(context, player, subSpell, source, target, cardToReveal);
	}
}
