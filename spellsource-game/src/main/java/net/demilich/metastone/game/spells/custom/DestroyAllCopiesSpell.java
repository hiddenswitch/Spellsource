package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

import java.util.ArrayList;

/**
 * Destroys the target and all copies of it wherever they are (hand, deck, battlefield) for both players.
 * <p>
 * Copies are matched by the target's source card ID ({@link Card#getCardId()}).
 * <p>
 * Implements Flik Skyshiv's "Battlecry: Destroy a minion and all copies of it (wherever they are)."
 */
public class DestroyAllCopiesSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target == null) {
			return;
		}

		String cardId = target.getSourceCard().getCardId();

		// Destroy the target itself
		if (target instanceof Actor && !target.isDestroyed()) {
			source.modifyAttribute(Attribute.TOTAL_KILLS, 1);
			context.getLogic().markAsDestroyed((Actor) target, source);
		}

		// Iterate through both players
		for (Player p : context.getPlayers()) {
			// Destroy matching minions on battlefield (excluding the original target)
			var minionsToDestroy = new ArrayList<Minion>();
			for (Minion minion : p.getMinions()) {
				if (minion.getId() != target.getId() && minion.getSourceCard().getCardId().equals(cardId)) {
					minionsToDestroy.add(minion);
				}
			}
			for (Minion minion : minionsToDestroy) {
				context.getLogic().markAsDestroyed(minion, source);
			}

			// Remove matching cards from hand
			var handCards = new ArrayList<Card>();
			for (Card card : p.getHand()) {
				if (card.getCardId().equals(cardId)) {
					handCards.add(card);
				}
			}
			for (Card card : handCards) {
				context.getLogic().removeCard(card);
			}

			// Remove matching cards from deck
			var deckCards = new ArrayList<Card>();
			for (Card card : p.getDeck()) {
				if (card.getCardId().equals(cardId)) {
					deckCards.add(card);
				}
			}
			for (Card card : deckCards) {
				context.getLogic().removeCard(card);
			}
		}
	}
}
