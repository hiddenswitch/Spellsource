package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.spells.ReceiveCardSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Receives copies of all cards the opponent played from their hand last turn.
 * <p>
 * Implements Tram Heist.
 */
public final class ReceiveOpponentPlayedCardsLastTurnSpell extends ReceiveCardSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Player opponent = context.getOpponent(player);
		String[] cards = opponent.getGraveyard()
				.stream()
				.filter(entity -> entity.getEntityType() == EntityType.CARD
						&& entity.hasAttribute(Attribute.PLAYED_FROM_HAND_OR_DECK)
						&& entity.getAttributeValue(Attribute.PLAYED_FROM_HAND_OR_DECK) == opponent.getAttributeValue(Attribute.LAST_TURN))
				.map(entity -> entity.getSourceCard().getCardId())
				.toArray(String[]::new);
		SpellDesc receiveCardSpell = new SpellDesc(ReceiveCardSpell.class);
		receiveCardSpell.put(SpellArg.CARDS, cards);
		super.onCast(context, player, receiveCardSpell, source, target);
	}
}
