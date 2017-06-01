package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.targeting.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class TransformCardSpell extends Spell {

	public static Logger logger = LoggerFactory.getLogger(TransformCardSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = (Card) target;
		if (card.getZone() == Zones.HAND) {
			context.getLogic().removeCard(card);
		} else {
			// logger.warn("Trying to transform card {} in invalid location {}",
			// card, card.getZone());
			return;
		}

		String cardId = (String) desc.get(SpellArg.CARD);
		Card newCard = context.getCardById(cardId);
		context.getLogic().receiveCard(player.getId(), newCard);
	}

}
