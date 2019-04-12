package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;

/**
 * Shuffles a card directly into the deck, not its copy.
 */
public final class ShuffleOriginalToDeckSpell extends ShuffleToDeckSpell {

	@Override
	@Suspendable
	protected Card shuffle(GameContext context, Player player, Card original, boolean quiet) {
		context.getLogic().shuffleToDeck(player, original, quiet);
		return original;
	}
}
