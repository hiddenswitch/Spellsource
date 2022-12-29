package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Draws {@link SpellArg#VALUE} cards from the top of the player's deck.
 * <p>
 * Casts the {@link SpellArg#SPELL} sub-spell with the newly drawn card as the {@link
 * net.demilich.metastone.game.targeting.EntityReference#OUTPUT}.
 * <p>
 * The method used to draw cards from the deck will trigger fatigue damage if the deck is empty. If an effect puts a
 * card into the deck on the (n-1)th sub spell just before attempting the n-th draw, this spell will draw it correctly.
 * <p>
 * For <b>example,</b> to draw a card and set its cost to 1:
 * <pre>
 *   {
 *     "class": "DrawCardSpell",
 *     "spell": {
 *       "class": "CardCostModifierSpell",
 *       "target": "OUTPUT",
 *       "cardCostModifier": {
 *         "class": "CardCostModifier",
 *         "target": "SELF",
 *         "operation": "SET",
 *         "value": 1
 *       }
 *     }
 *   }
 * </pre>
 * Observe that the target of the {@code "CardCostModifierSpell"} subspell is {@link
 * net.demilich.metastone.game.targeting.EntityReference#OUTPUT}, which is the card that you actually drew.
 *
 * @see FromDeckToHandSpell to draw a specific card from the deck.
 */
public class DrawCardSpell extends Spell {

	private static Logger LOGGER = LoggerFactory.getLogger(DrawCardSpell.class);

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(DrawCardSpell.class);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int count) {
		Map<SpellArg, Object> arguments = new SpellDesc(DrawCardSpell.class);
		arguments.put(SpellArg.VALUE, count);
		return new SpellDesc(arguments);
	}

	private static Logger logger = LoggerFactory.getLogger(DrawCardSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.VALUE, SpellArg.CARD_FILTER);
		int cardCount = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);

		// If there is a card filter written on this effect, use a FromDeckToHandSpell instead
		if (desc.containsKey(SpellArg.CARD_FILTER)) {
			LOGGER.debug("{} {}: CARD_FILTER specified, doing a FromDeckToHandSpell instead", context.getGameId(), source);
			FromDeckToHandSpell.drawFromDeck(context, player, source, target, cardCount, false, desc.getCardFilter(), desc.getSpell(), null);
			return;
		}
		for (int i = 0; i < cardCount; i++) {
			Card card = context.getLogic().drawCard(player.getId(), source);

			if (card == null || card.getZone() != Zones.HAND) {
				continue;
			}

			SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
			if (subSpell != null) {
				SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
			}
		}
	}
}

