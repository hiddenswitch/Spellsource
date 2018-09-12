package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class PutDeckTopSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = SpellUtils.getCards(context, player, target, source, desc);

		for (Card card : cards) {
			context.getLogic().putOnTopOfDeck(player, card);

			for (SpellDesc subSpell : desc.subSpells(0)) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
			}
		}
	}
}
