package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SwapCardsSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Both players swap their leftmost and rightmost cards with their opponent.
 */
public final class VolatileWisdomSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		var cards = new Card[][]{null, null};
		var players = new Player[]{player, context.getOpponent(player)};
		for (var thisPlayer : players) {
			// Get the leftmost and rightmost cards.
			// Leftmost card goes into the 0th index of this array. Rightmost card, if the player has more than 1 card, goes
			// into the 1st index of the array. If the hand is empty both are null.
			var playerCards = new Card[]{thisPlayer.getHand().isEmpty() ? null : thisPlayer.getHand().get(0), thisPlayer.getHand().size() > 1 ? thisPlayer.getHand().get(thisPlayer.getHand().size() - 1) : null};
			cards[thisPlayer.getId()] = playerCards;
		}
		// Now actually do the swaps
		for (var i = 0; i < 2; i++) {
			var card1 = cards[0][i];
			var card2 = cards[1][i];
			if (card1 != null && card2 != null) {
				SwapCardsSpell.swap(context, card1, card2);
			}
		}
	}
}
