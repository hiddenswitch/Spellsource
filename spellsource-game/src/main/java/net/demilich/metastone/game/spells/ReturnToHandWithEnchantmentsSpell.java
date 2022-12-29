package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.ReturnToHandEvent;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

/**
 * Returns the {@code target} to the hand, keeping its enchantments.
 * <p>
 * Currently, this effect might be incorrectly interpreted as shuffling to the deck with enchantments. While there are
 * no effects currently doing this, some may in the future.
 */
public final class ReturnToHandWithEnchantmentsSpell extends ShuffleToDeckWithEnchantmentsSpell {

	@Override
	protected void moveCopyToDestination(GameContext context, Player player, Entity target, Card copiedCard) {
		// Da Undatakah interaction
		if (!copiedCard.hasAttribute(Attribute.KEEPS_ENCHANTMENTS)) {
			copiedCard.setAttribute(Attribute.KEEPS_ENCHANTMENTS);
		}
		// Prevents cards from being discarded
		if (copiedCard.getZone() == Zones.HAND
				&& copiedCard.hasAttribute(Attribute.DISCARDED)) {
			copiedCard.getAttributes().remove(Attribute.DISCARDED);
		} else {
			context.getLogic().receiveCard(player.getId(), copiedCard);
		}

		// It must still be in the hand to be a returned to hand effect
		if (copiedCard.getZone() == Zones.HAND) {
			SpellUtils.processKeptEnchantments(target, copiedCard);
			context.getLogic().fireGameEvent(new ReturnToHandEvent(context, player.getId(), copiedCard, target));
		}
	}
}
