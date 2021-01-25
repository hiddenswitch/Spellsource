package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierArg;
import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;

public class CardCostModifierDescDeserializer extends DescDeserializer<CardCostModifierDesc, CardCostModifierArg, CardCostModifier> {

	public CardCostModifierDescDeserializer() {
		super(CardCostModifierDesc.class);
	}

	@Override
	protected CardCostModifierDesc createDescInstance() {
		return new CardCostModifierDesc();
	}

	@Override
	public void init(SerializationContext ctx) {
		ctx.add(CardCostModifierArg.TARGET_PLAYER, ParseValueType.TARGET_PLAYER);
		ctx.add(CardCostModifierArg.RACE, ParseValueType.STRING);
		ctx.add(CardCostModifierArg.VALUE, ParseValueType.VALUE);
		ctx.add(CardCostModifierArg.MIN_VALUE, ParseValueType.INTEGER);
		ctx.add(CardCostModifierArg.CARD_TYPE, ParseValueType.CARD_TYPE);
		ctx.add(CardCostModifierArg.REQUIRED_ATTRIBUTE, ParseValueType.ATTRIBUTE);
		ctx.add(CardCostModifierArg.EXPIRATION_TRIGGER, ParseValueType.EVENT_TRIGGER);
		ctx.add(CardCostModifierArg.EXPIRATION_TRIGGERS, ParseValueType.EVENT_TRIGGER_ARRAY);
		ctx.add(CardCostModifierArg.TOGGLE_ON_TRIGGER, ParseValueType.EVENT_TRIGGER);
		ctx.add(CardCostModifierArg.TOGGLE_OFF_TRIGGER, ParseValueType.EVENT_TRIGGER);
		ctx.add(CardCostModifierArg.TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(CardCostModifierArg.OPERATION, ParseValueType.ALGEBRAIC_OPERATION);
		ctx.add(CardCostModifierArg.FILTER, ParseValueType.ENTITY_FILTER);
		ctx.add(CardCostModifierArg.CONDITION, ParseValueType.CONDITION);
	}

	@Override
	protected Class<CardCostModifier> getAbstractComponentClass() {
		return CardCostModifier.class;
	}

	@Override
	protected Class<CardCostModifierArg> getEnumType() {
		return CardCostModifierArg.class;
	}
}
