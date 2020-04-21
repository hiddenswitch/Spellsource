package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

/**
 * Reads a card ID from the specified attribute {@link SpellArg#ATTRIBUTE}. Summons the card with that ID.
 * <p>
 * For example, to summon the last minion a minion destroyed:
 * <pre>
 *      "spell": {
 *          "class": "SummonCardFromAttributeSpell",
 *          "target": "SELF",
 *          "attribute": "LAST_MINION_DESTROYED_CARD_ID"
 *      }
 * </pre>
 * <p>
 * Implements Sourcing Specialist.
 */
public class SummonCardFromAttributeSpell extends Spell {
	public static SpellDesc create(Attribute attributeContainingCardId, String defaultCardId, EntityReference target) {
		Map<SpellArg, Object> arguments = new SpellDesc(SummonCardFromAttributeSpell.class);
		arguments.put(SpellArg.ATTRIBUTE, attributeContainingCardId);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.CARD, defaultCardId);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		String cardId = (String) target.getAttribute((Attribute) desc.get(SpellArg.ATTRIBUTE));
		Card card = null;
		if (cardId == null) {
			// Check if there is a default card
			card = SpellUtils.getCard(context, desc);
		} else {
			card = context.getCardById(cardId);
		}
		if (card == null) {
			return;
		}
		for (int i = 0; i < count; i++) {
			card = count == 1 ? card : card.clone();
			context.getLogic().summon(player.getId(), card.minion(), source, boardPosition, false);
		}
	}
}
