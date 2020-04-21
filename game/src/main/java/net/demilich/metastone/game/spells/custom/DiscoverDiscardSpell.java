package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.HandSource;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

/**
 * A spell that has the same interpretation of arguments as {@link net.demilich.metastone.game.spells.DiscardSpell},
 * except the player chooses which cards to discard. Suitable for a {@link net.demilich.metastone.game.spells.aura.SpellOverrideAura}
 * overriding a {@link net.demilich.metastone.game.spells.DiscardSpell}.
 * <p>
 * Implements Scepter of Sargeras.
 */
public final class DiscoverDiscardSpell extends Spell {

	public static final int ALL_CARDS = -1;
	public static SpellDesc create() {
		return create(1);
	}

	public static SpellDesc create(int numberOfCards) {
		Map<SpellArg, Object> arguments = new SpellDesc(DiscoverDiscardSpell.class);
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
		int maxDiscover = (int) desc.getOrDefault(SpellArg.HOW_MANY, 3);

		if (target instanceof Card && player.getHand().contains(target)) {
			context.getLogic().discardCard(player, (Card) target);
		} else {
			CardList discardableCards = cardSource.getCards(context, source, player).filtered(cardFilter.matcher(context, player, source));
			if (discardableCards.getCount() == 0 || numberOfCards == 0) {
				return;
			}
			if (numberOfCards == ALL_CARDS || numberOfCards == discardableCards.getCount()) {
				discardableCards.forEach(card -> context.getLogic().discardCard(player, card));
			} else {
				for (int i = 0; i < numberOfCards; i++) {
					CardList currentDiscardPool = new CardArrayList();
					discardableCards.shuffle(context.getLogic().getRandom());
					for (int j = 0; j < Math.min(discardableCards.getCount(), maxDiscover); j++) {
						discardableCards.get(j).setAttribute(Attribute.HAND_INDEX, j);
						currentDiscardPool.add(discardableCards.get(j));
					}
					DiscoverAction discoverAction = SpellUtils.discoverCard(context, player, source, desc, currentDiscardPool);
					if (discoverAction == null) {
						return;
					}
					Card chosenCard = discoverAction.getCard();
					if (chosenCard == null) {
						return;
					}
					final int index = chosenCard.getAttributeValue(Attribute.HAND_INDEX);
					Card realCard = discardableCards.stream().filter(card -> card.getAttributeValue(Attribute.HAND_INDEX) == index).findFirst().get();
					discardableCards.forEach(card -> context.getLogic().removeAttribute(player, null, card, Attribute.HAND_INDEX));

					context.getLogic().discardCard(player, realCard);
					discardableCards.remove(realCard);
				}
			}
		}
	}
}
