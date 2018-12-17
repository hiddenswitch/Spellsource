package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Puts the cards from {@link SpellUtils#getCards(GameContext, Player, Entity, Entity, SpellDesc, int)} on the bottom of
 * the player's deck.
 */
public final class PutOnBottomOfDeckSpell extends Spell {

	private static final long serialVersionUID = 479946813036159860L;

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = SpellUtils.getCards(context, player, target, source, desc);

		for (Card card : cards) {
			if (context.getLogic().insertIntoDeck(player, card.getCopy(), 0)) {
				for (SpellDesc subSpell : desc.subSpells(0)) {
					SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
				}
			}
		}
	}
}
