package net.demilich.metastone.game.spells.desc.filter;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.SpellUtils;

import java.util.List;

/**
 * Filters entities based on various properties of their source cards.
 * <p>
 * The supported properties are:
 * <ul>
 * <li>{@link EntityFilterArg#CARD_TYPE}.</li>
 * <li>{@link EntityFilterArg#RACE}.</li>
 * <li>{@link EntityFilterArg#HERO_CLASS}, including the special hero classes {@link HeroClass#OPPONENT} and {@link
 * HeroClass#SELF}.</li>
 * <li>{@link EntityFilterArg#HERO_CLASSES} to check if the entity's hero class matches any in the list.</li>
 * <li>{@link EntityFilterArg#MANA_COST} for the entity's base mana cost. Use {@link ManaCostFilter} for its current
 * cost instead.</li>
 * <li>{@link EntityFilterArg#RARITY}.</li>
 * <li>{@link EntityFilterArg#CARD_SET}.</li>
 * <li>{@link EntityFilterArg#ATTRIBUTE}, including an {@link EntityFilterArg#OPERATION} against it. For attack and
 * health, use {@link AttributeFilter}.</li>
 * </ul>
 */
public final class CardFilter extends EntityFilter {

	public CardFilter(EntityFilterDesc desc) {
		super(desc);
	}

	protected boolean test(
			GameContext context,
			Player player,
			Entity entity,
			Entity host,
			CardType cardType,
			String race,
			String[] heroClasses,
			String heroClass,
			Integer manaCost,
			Rarity rarity,
			String cardSet,
			String card,
			Attribute attribute,
			ComparisonOperation operation,
			Integer value) {
		if (entity == null) {
			return false;
		}

		Card sourceCard = entity.getSourceCard();

		if (cardType != null && !GameLogic.isCardType(sourceCard.getCardType(), cardType)) {
			return false;
		}
		if (race != null && !Race.hasRace(context, sourceCard, race)) {
			return false;
		}
		if (heroClasses == null && heroClass == null && manaCost == null && rarity == null
				&& cardSet == null && card == null && attribute == null) {
			return true;
		}
		return testExtended(context, player, sourceCard, heroClasses, heroClass, manaCost, rarity, cardSet, card, attribute, operation, value);
	}

	private static boolean testExtended(
			GameContext context,
			Player player,
			Card sourceCard,
			String[] heroClasses,
			String heroClass,
			Integer manaCost,
			Rarity rarity,
			String cardSet,
			String card,
			Attribute attribute,
			ComparisonOperation operation,
			Integer value) {
		if (heroClasses != null && heroClasses.length > 0) {
			boolean test = false;
			for (String hc : heroClasses) {
				test |= !HeroClass.hasHeroClass(context, player, sourceCard, hc);
			}
			if (!test) {
				return false;
			}
		}

		if (heroClass != null && HeroClass.hasHeroClass(context, player, sourceCard, heroClass)) {
			return false;
		}

		if (manaCost != null && manaCost != sourceCard.getBaseManaCost()) {
			return false;
		}
		if (rarity != null && !GameLogic.isRarity(sourceCard.getRarity(), rarity)) {
			return false;
		}

		if (cardSet != null && !cardSet.equals("ANY") && !sourceCard.getCardSet().equals(cardSet)) {
			return false;
		}

		if (card != null && !sourceCard.getCardId().contains(card)) {
			return false;
		}

		if (attribute != null) {
			if (operation == null && value != null) {
				operation = ComparisonOperation.EQUAL;
			}
			if (operation == ComparisonOperation.HAS || operation == null) {
				return cardFilterHasAttribute(sourceCard, attribute);
			}

			int targetValue = value != null ? value : 0;
			int actualValue = sourceCard.getAttributeValue(attribute);
			return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
		}

		return true;
	}

	private static boolean cardFilterHasAttribute(Card card, Attribute attribute) {
		if (!card.getAttributes().isEmpty()) {
			return card.hasAttribute(attribute);
		}
		var desc = card.getDesc();
		return switch (attribute) {
			case PASSIVE_TRIGGERS -> desc.getPassiveTrigger() != null || (desc.getPassiveTriggers() != null && desc.getPassiveTriggers().length > 0);
			case DECK_TRIGGERS -> desc.getDeckTrigger() != null || (desc.getDeckTriggers() != null && desc.getDeckTriggers().length > 0);
			case GAME_TRIGGERS -> desc.getGameTriggers() != null && desc.getGameTriggers().length > 0;
			case MANA_COST_MODIFIER -> desc.getManaCostModifier() != null;
			default -> card.hasAttribute(attribute);
		};
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}

	public static CardFilter create(CardType cardType) {
		return create(cardType, null);
	}

	public static CardFilter create(CardType cardType, String race) {
		EntityFilterDesc arguments = new EntityFilterDesc(CardFilter.class);
		if (cardType != null) {
			arguments.put(EntityFilterArg.CARD_TYPE, cardType);
		}
		if (race != null) {
			arguments.put(EntityFilterArg.RACE, race);
		}
		return new CardFilter(arguments);
	}
}