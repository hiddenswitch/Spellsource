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

import java.util.Arrays;
import java.util.List;

public class PutDeckTopSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = SpellUtils.getCards(context, player, target, source, desc);

		for (Card card : cards) {
			try {
				context.getLogic().putOnTopOfDeck(player, card);
			} catch (ArrayStoreException e) {
				context.getLogic().putOnTopOfDeck(player, card.getCopy());
			}

			for (SpellDesc subSpell : desc.subSpells(0)) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
			}
		}
	}
}
