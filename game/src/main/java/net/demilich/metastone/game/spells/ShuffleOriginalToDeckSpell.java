package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

/**
 * Shuffles a card directly into the deck, not its copy.
 */
public final class ShuffleOriginalToDeckSpell extends ShuffleToDeckSpell {

	@Override
	@Suspendable
	protected Card shuffle(GameContext context, Player player, Entity targetEntity, Card targetCard, boolean extraCopy, int sourcePlayerId) {
		context.getLogic().shuffleToDeck(player, targetCard, extraCopy, sourcePlayerId);
		return targetCard;
	}
}
