package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Comparator;

/**
 * Reverses the order of the player's deck.
 * <p>
 * The card on top of the deck becomes the bottom card, and vice versa.
 */
public final class ReverseDeckSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int size = player.getDeck().size();
		if (size <= 1) {
			return;
		}
		// Sort by reversed index to reverse the deck order
		player.getDeck().sort(Comparator.comparingInt((Card c) -> c.getEntityLocation().getIndex()).reversed());
	}
}
