package net.demilich.metastone.game.spells.custom;

import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replaces the player's entire deck with a random pre-built deck chosen from the {@link SpellArg#CARDS} array of card
 * IDs.
 * <p>
 * Each pre-built deck is specified as a separate {@link SpellDesc} in the {@link SpellArg#SPELLS} array, where each
 * sub-spell's {@link SpellArg#CARDS} contains the card IDs for that deck. One deck is chosen at random and the player's
 * current deck is replaced with copies of those cards.
 * <p>
 * Used by Whizbang the Wonderful and Zayle, Shadow Cloak.
 */
public class ReplaceWithPrebuiltDeckSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(ReplaceWithPrebuiltDeckSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc[] decks = (SpellDesc[]) desc.get(SpellArg.SPELLS);
		if (decks == null || decks.length == 0) {
			logger.error("replaceWithPrebuiltDeck {} {}: No decks specified.", context.getGameId(), source);
			return;
		}

		// Pick a random deck
		int index = context.getLogic().getRandom().nextInt(decks.length);
		SpellDesc chosenDeck = decks[index];
		String[] cardIds = (String[]) chosenDeck.get(SpellArg.CARDS);

		if (cardIds == null || cardIds.length == 0) {
			logger.error("replaceWithPrebuiltDeck {} {}: Chosen deck has no cards.", context.getGameId(), source);
			return;
		}

		logger.debug("replaceWithPrebuiltDeck {} {}: Player {} gets deck #{} with {} cards",
				context.getGameId(), source, player.getUserId(), index, cardIds.length);

		// Remove all cards from current deck
		var deckZone = player.getDeck();
		for (int i = deckZone.size() - 1; i >= 0; i--) {
			Card card = deckZone.get(i);
			context.getLogic().removeCard(card);
		}

		// Add new cards
		for (String cardId : cardIds) {
			Card card = context.getCardById(cardId).getCopy();
			card.setId(context.getLogic().generateId());
			card.setOwner(player.getId());
			context.getLogic().shuffleToDeck(player, card, false);
		}
	}
}
