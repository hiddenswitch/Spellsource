package net.demilich.metastone.game.services;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.decks.GameDeck;

public interface Inventory {

	/**
	 * Retrieves a deck of the specified name for the given player.
	 *
	 * @param player The player whose deck collections should be queried.
	 * @param name   The name of the deck to retrieve
	 * @return The deck, or {@code null} if no deck was found with the given name.
	 */
	@Suspendable
	GameDeck getDeck(Player player, String name);
}


