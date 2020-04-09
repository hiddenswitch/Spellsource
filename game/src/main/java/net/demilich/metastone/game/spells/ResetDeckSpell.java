package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sets the deck to its initial state at the start of the game.
 * <p>
 * Implements Bountiful Porzora.
 */
public class ResetDeckSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Reconstruct the deck.
		List<Card> originalDeck = context.getEntities()
				.filter(e -> e.getOwner() == player.getId()
						&& e.getEntityType() == EntityType.CARD
						&& e.getAttributes().containsKey(Attribute.STARTING_INDEX) && e.hasAttribute(Attribute.STARTED_IN_DECK))
				.sorted(Comparator.comparingInt(e -> (int) e.getAttributes().get(Attribute.STARTING_INDEX)))
				.map(e -> e.getSourceCard().getCardId())
				.map(context::getCardById)
				.collect(Collectors.toList());

		// Remove all the cards from the player's deck
		while (!player.getDeck().isEmpty()) {
			context.getLogic().removeCard(player.getDeck().peek());
		}

		// Add the new cards to the top of the player's deck silently.
		for (int i = 0; i < originalDeck.size(); i++) {
			Card card = originalDeck.get(i);
			context.getLogic().insertIntoDeck(player, card, i, true);
		}
	}
}
