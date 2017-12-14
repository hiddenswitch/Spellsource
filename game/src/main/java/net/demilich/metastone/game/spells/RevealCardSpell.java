package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class RevealCardSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		final CardList filteredCards = desc.getFilteredCards(context, player, source);
		if (filteredCards.size() == 0) {
			return;
		}

		Card cardToReveal = context.getLogic().getRandom(filteredCards);
		context.getLogic().revealCard(player, cardToReveal);
		// For sub spells, the revealed card is passed as the target.
		SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		if (subSpell == null) {
			return;
		}
		SpellUtils.castChildSpell(context, player, subSpell, source, cardToReveal);
	}
}
