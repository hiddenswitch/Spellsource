package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.DiscardEvent;
import net.demilich.metastone.game.events.MillEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.DeckSource;
import net.demilich.metastone.game.spells.desc.source.HandSource;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Discards cards from the {@link net.demilich.metastone.game.targeting.Zones#HAND} or from a {@link CardSource} like
 * {@link DeckSource} which does not generate new cards (does not implement {@link
 * net.demilich.metastone.game.spells.desc.source.HasCardCreationSideEffects}), like {@link DeckSource}.
 * <p>
 * Discarding from the hand generates a {@link DiscardEvent}, while discarding from the deck generates a {@link
 * MillEvent}.
 * <p>
 * To discard all cards, use a {@link SpellArg#VALUE} of {@code -1}.
 * <p>
 * Cards to discard are always chosen at random.
 * <p>
 * {@link SpellArg#CARD_FILTER} can be specified to filter which cards should be discarded.
 * <p>
 * For example, to implement "Battlecry: Discard a Banana to deal 3 damage to an enemy minion:"
 * <pre>
 *   "battlecry": {
 *     "condition": {
 *       "class": "HoldsCardCondition",
 *       "cardFilter": {
 *         "class": "SpecificCardFilter",
 *         "card": "spell_bananas"
 *       }
 *     },
 *     "targetSelection": "ENEMY_MINIONS",
 *     "spell": {
 *       "class": "MetaSpell",
 *       "spells": [
 *         {
 *           "class": "DiscardSpell",
 *           "cardFilter": {
 *             "class": "SpecificCardFilter",
 *             "card": "spell_bananas"
 *           }
 *         },
 *         {
 *           "class": "DamageSpell",
 *           "value": 3
 *         }
 *       ]
 *     }
 *   }
 * </pre>
 */
public class DiscardSpell extends Spell {

	public static final int ALL_CARDS = -1;

	public static SpellDesc create() {
		return create(1);
	}

	public static SpellDesc create(int numberOfCards) {
		Map<SpellArg, Object> arguments = new SpellDesc(DiscardSpell.class);
		arguments.put(SpellArg.VALUE, numberOfCards);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardSource cardSource = (CardSource) desc.getOrDefault(SpellArg.CARD_SOURCE, HandSource.create());
		EntityFilter cardFilter = desc.getCardFilter();
		if (cardFilter == null) {
			cardFilter = AndFilter.create();
		}
		int numberOfCards = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);

		CardList discardableCards = cardSource.getCards(context, source, player).filtered(cardFilter.matcher(context, player, source));
		int cardCount = numberOfCards == ALL_CARDS ? discardableCards.getCount() : numberOfCards;

		for (int i = 0; i < cardCount; i++) {
			Card randomCard = context.getLogic().getRandom(discardableCards);
			if (randomCard == null) {
				return;
			}
			context.getLogic().discardCard(player, randomCard);
			discardableCards.remove(randomCard);
		}
	}

}
