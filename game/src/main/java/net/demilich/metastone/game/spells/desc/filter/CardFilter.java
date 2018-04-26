package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.utils.Attribute;
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

import java.util.List;

public class CardFilter extends EntityFilter {

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

		Card card = entity.getSourceCard();

		CardType cardType = (CardType) getDesc().get(FilterArg.CARD_TYPE);
		if (cardType != null && !card.getCardType().isCardType(cardType)) {
			return false;
		}
		Race race = (Race) getDesc().get(FilterArg.RACE);
		if (race != null && !card.getRace().hasRace(race)) {
			return false;
		}

		HeroClass[] heroClasses = (HeroClass[]) getDesc().get(FilterArg.HERO_CLASSES);
		if (heroClasses != null && heroClasses.length > 0) {
			boolean test = false;
			for (HeroClass heroClass : heroClasses) {
				test |= !heroClassTest(context, player, card, heroClass);
			}
			if (!test) {
				return false;
			}
		}

		HeroClass heroClass = (HeroClass) getDesc().get(FilterArg.HERO_CLASS);
		if (heroClass != null && heroClassTest(context, player, card, heroClass)) {
			return false;
		}

		if (getDesc().containsKey(FilterArg.MANA_COST)) {
			int manaCost = getDesc().getValue(FilterArg.MANA_COST, context, player, null, host, 0);
			// TODO: Should we be looking at base mana cost or modified mana cost here?
			if (manaCost != card.getBaseManaCost()) {
				return false;
			}
		}
		Rarity rarity = (Rarity) getDesc().get(FilterArg.RARITY);
		if (rarity != null && !card.getRarity().isRarity(rarity)) {
			return false;
		}

		if (getDesc().containsKey(FilterArg.ATTRIBUTE)) {
			Attribute attribute = (Attribute) getDesc().get(FilterArg.ATTRIBUTE);
			Operation operation = null;
			if (getDesc().containsKey(FilterArg.OPERATION)) {
				operation = (Operation) getDesc().get(FilterArg.OPERATION);
			}
			if (!getDesc().containsKey(FilterArg.OPERATION)
					&& getDesc().containsKey(FilterArg.VALUE)) {
				operation = Operation.EQUAL;
			}
			if (operation == Operation.HAS || operation == null) {
				return card.hasAttribute(attribute);
			}

			int targetValue;
			if (entities == null) {
				targetValue = getDesc().getInt(FilterArg.VALUE);
			} else {
				targetValue = getDesc().getValue(FilterArg.VALUE, context, player, entities.get(0), null, 0);
			}

			int actualValue = card.getAttributeValue(attribute);
			return SpellUtils.evaluateOperation(operation, actualValue, targetValue);
		}

		CardSet cardSet = (CardSet) getDesc().get(FilterArg.CARD_SET);
		if (cardSet != null && cardSet != CardSet.ANY && card.getCardSet() != cardSet) {
			return false;
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
			arguments.put(FilterArg.CARD_TYPE, cardType);
		}
		if (race != null) {
			arguments.put(FilterArg.RACE, race);
		}
		return new CardFilter(arguments);
	}
}
