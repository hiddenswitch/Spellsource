package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.Zones;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Moves a card from the deck to the hand. Triggers a draw event.
 * <p>
 * If a {@code target} is specified and it's inside the caster's deck, that card is the one that is moved.
 * <p>
 * If a {@link SpellArg#CARD} is specified, it is interpreted as a "replacement card" in case no cards are found that
 * satisfy the {@link SpellArg#CARD_FILTER} or the deck is empty.
 * <p>
 * Otherwise, a {@link net.demilich.metastone.game.spells.desc.filter.CardFilter} is evaluated against a {@link
 * net.demilich.metastone.game.spells.desc.source.DeckSource} from the point of view of the caster.
 * <p>
 * If {@link SpellArg#EXCLUSIVE} is set, only draws distinct cards.
 * <p>
 * For example, to draw a 10-cost minion from the caster's deck:
 * <pre>
 *   {
 *     "class": "FromDeckToHandSpell",
 *     "value": 1,
 *     "cardFilter": {
 *       "class": "CardFilter",
 *       "cardType": "MINION",
 *       "manaCost": 10
 *     },
 *     "targetPlayer": "SELF"
 *   }
 * </pre>
 * <p>
 * This effect is to be rolled into the {@link DrawCardSpell}.
 */
public class FromDeckToHandSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(FromDeckToHandSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.VALUE, SpellArg.CARD_FILTER, SpellArg.CARD, SpellArg.EXCLUSIVE);
		drawFromDeck(context, player, source, target, desc.getValue(SpellArg.VALUE, context, player, target, source, 1), desc.getBool(SpellArg.EXCLUSIVE), (EntityFilter) desc.get(SpellArg.CARD_FILTER), desc.getSpell(), (String) desc.get(SpellArg.CARD));
	}

	/**
	 * Draws a filtered card from the deck.
	 *
	 * @param context          the game context
	 * @param player           the player whose card should be drawn
	 * @param source           the source entity
	 * @param target           the card to draw; or, if it is null or not a card in the player's deck, the target that
	 *                         will be passed to the spell specified to be cast on the drawn card (the spell itself will
	 *                         receive the drawn card as the {@link net.demilich.metastone.game.targeting.EntityReference#OUTPUT})
	 * @param cardsToDraw      the number of cards to draw
	 * @param removeDuplicates when {@code true}, removes duplicates from the pool of cards to draw
	 * @param cardFilter       the filter to apply to the cards, or {@code null} for no filter
	 * @param spellOnCard      the spell to cast on the drawn card, if one was successfully drawn and put into the hand
	 * @param replacementCard  when not {@code null}, receive this card instead if the deck does not contain any cards
	 *                         that satisfies the {@code cardFilter}
	 */
	@Suspendable
	public static void drawFromDeck(GameContext context, Player player, Entity source, Entity target, int cardsToDraw, boolean removeDuplicates, @Nullable EntityFilter cardFilter, @Nullable SpellDesc spellOnCard, @Nullable String replacementCard) {
		if (target != null && target.getEntityType() == EntityType.CARD && target.getZone() == Zones.DECK && target.getOwner() == player.getId()) {
			Card card = (Card) target;
			if (player.getDeck().contains(card)) {
				context.getLogic().receiveCard(player.getId(), card, source, true);
				if (spellOnCard != null) {
					SpellUtils.castChildSpell(context, player, spellOnCard, source, target, card);
				}
				return;
			}
		}

		Set<String> drawnCardIds = new HashSet<>();
		for (int i = 0; i < cardsToDraw; i++) {
			CardList relevantCards = getCards(context, player, source, cardFilter);
			Card card = null;

			if (removeDuplicates) {
				relevantCards.removeIf(c -> drawnCardIds.contains(c.getCardId()));
			}

			if (!relevantCards.isEmpty()) {
				card = context.getLogic().getRandom(relevantCards);
			} else if (replacementCard != null) {
				card = context.getCardById(replacementCard);
			}

			if (card != null) {
				card = context.getLogic().receiveCard(player.getId(), card, source, true);
				if (card.getZone() != Zones.HAND) {
					continue;
				}
				drawnCardIds.add(card.getCardId());
				if (spellOnCard != null) {
					SpellUtils.castChildSpell(context, player, spellOnCard, source, target, card);
				}
			}
		}
	}

	private static CardList getCards(GameContext context, Player player, Entity source, EntityFilter cardFilter) {
		CardList relevantCards = null;
		if (cardFilter != null) {
			relevantCards = SpellUtils.getCards(player.getDeck(), card -> cardFilter.matches(context, player, card, source));
		} else {
			relevantCards = SpellUtils.getCards(player.getDeck(), null);
		}
		return relevantCards;
	}
}
