package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.Rarity;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.cards.Attribute;

import java.util.List;

/**
 * Filters entities based on various properties of their source cards.
 * <p>
 * The supported properties are:
 * <ul>
 * <li>{@link EntityFilterArg#CARD_TYPE}.</li>
 * <li>{@link EntityFilterArg#RACE}.</li>
 * <li>{@link EntityFilterArg#HERO_CLASS}</li>, including the special hero classes {@link HeroClass#OPPONENT} and {@link
 * HeroClass#SELF}.
 * <li>{@link EntityFilterArg#HERO_CLASSES}</li> to check if the entity's hero class matches any in the list.
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

	private boolean heroClassTest(GameContext context, Player player, Card card, HeroClass heroClass) {
		if (heroClass == HeroClass.OPPONENT) {
			heroClass = context.getOpponent(player).getHero().getHeroClass();
		} else if (heroClass == HeroClass.SELF) {
			heroClass = player.getHero().getHeroClass();
		}

		if (heroClass != null && card.hasHeroClass(heroClass)) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		List<Entity> entities = getTargetedEntities(context, player, host);

		if (entity == null) {
			return false;
		}

		Card card = entity.getSourceCard();

		CardType cardType = (CardType) getDesc().get(EntityFilterArg.CARD_TYPE);
		if (cardType != null && !card.getCardType().isCardType(cardType)) {
			return false;
		}
		Race race = (Race) getDesc().get(EntityFilterArg.RACE);
		if (race != null && !card.getRace().hasRace(race)) {
			return false;
		}

		HeroClass[] heroClasses = (HeroClass[]) getDesc().get(EntityFilterArg.HERO_CLASSES);
		if (heroClasses != null && heroClasses.length > 0) {
			boolean test = false;
			for (HeroClass heroClass : heroClasses) {
				test |= !heroClassTest(context, player, card, heroClass);
			}
			if (!test) {
				return false;
			}
		}

		HeroClass heroClass = (HeroClass) getDesc().get(EntityFilterArg.HERO_CLASS);
		if (heroClass != null && heroClassTest(context, player, card, heroClass)) {
			return false;
		}

		if (getDesc().containsKey(EntityFilterArg.MANA_COST)) {
			int manaCost = getDesc().getValue(EntityFilterArg.MANA_COST, context, player, null, host, 0);
			// TODO: Should we be looking at base mana cost or modified mana cost here?
			if (manaCost != card.getBaseManaCost()) {
				return false;
			}
		}
		Rarity rarity = (Rarity) getDesc().get(EntityFilterArg.RARITY);
		if (rarity != null && !card.getRarity().isRarity(rarity)) {
			return false;
		}

		CardSet cardSet = (CardSet) getDesc().get(EntityFilterArg.CARD_SET);
		if (cardSet != null && cardSet != CardSet.ANY && card.getCardSet() != cardSet) {
			return false;
		}

		if (getDesc().containsKey(EntityFilterArg.ATTRIBUTE)) {
			Attribute attribute = (Attribute) getDesc().get(EntityFilterArg.ATTRIBUTE);
			ComparisonOperation operation = null;
			if (getDesc().containsKey(EntityFilterArg.OPERATION)) {
				operation = (ComparisonOperation) getDesc().get(EntityFilterArg.OPERATION);
			}
			if (!getDesc().containsKey(EntityFilterArg.OPERATION)
					&& getDesc().containsKey(EntityFilterArg.VALUE)) {
				operation = ComparisonOperation.EQUAL;
			}
			if (operation == ComparisonOperation.HAS || operation == null) {
				return card.hasAttribute(attribute);
			}

			int targetValue;
			if (entities == null) {
				targetValue = getDesc().getValue(EntityFilterArg.VALUE, context, player, null, null, 0);
			} else {
				targetValue = getDesc().getValue(EntityFilterArg.VALUE, context, player, entities.get(0), null, 0);
			}

			int actualValue = card.getAttributeValue(attribute);
			return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
		}

		return true;
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}

	public static CardFilter create(CardType cardType) {
		return create(cardType, null);
	}

	public static CardFilter create(CardType cardType, Race race) {
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