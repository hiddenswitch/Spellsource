package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.ReceiveCardSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Receives the spells the opponent cast from their hand their previous turn.
 */
public final class ReceiveOpponentsCastSpellsSpell extends ReceiveCardSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);
		String[] cards = opponent.getGraveyard()
				.stream()
				.filter(entity -> entity.getEntityType() == EntityType.CARD
						&& entity.getSourceCard().getCardType().isCardType(CardType.SPELL)
						&& entity.getAttributeValue(Attribute.PLAYED_FROM_HAND_OR_DECK) == opponent.getAttributeValue(Attribute.LAST_TURN))
				.map(entity -> entity.getSourceCard().getCardId())
				.toArray(String[]::new);
		SpellDesc receiveCardSpell = ReceiveCardSpell.create(cards);
		super.onCast(context, player, receiveCardSpell, source, target);
	}
}
