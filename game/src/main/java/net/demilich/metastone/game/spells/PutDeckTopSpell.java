package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Puts the specified cards ({@code target} or otherwise from {@link SpellUtils#getCards(GameContext, Player, Entity,
 * Entity, SpellDesc, int)}) on the top of the deck.
 * <p>
 * Puts {@link net.demilich.metastone.game.spells.desc.SpellArg#HOW_MANY} copies.
 *
 * @see ShuffleToDeckSpell to shuffle a card into the deck.
 * @see PutOnBottomOfDeckSpell to put a card on the bottom of the deck.
 */
public final class PutDeckTopSpell extends Spell {

	private static final long serialVersionUID = 5859693551922929308L;

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = SpellUtils.getCards(context, player, target, source, desc);
		int copies = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		for (Card card : cards) {
			for (int i = 0; i < copies; i++) {
				if (context.getLogic().insertIntoDeck(player, card.getCopy(), player.getDeck().size())) {
					for (SpellDesc subSpell : desc.subSpells(0)) {
						SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
					}
				}
			}
		}
	}
}
