package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.condition.ConditionArg;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;

public class ConditionDescDeserializer extends DescDeserializer<ConditionDesc, ConditionArg, Condition> {

	public ConditionDescDeserializer() {
		super(ConditionDesc.class);
	}

	@Override
	protected ConditionDesc createDescInstance() {
		return new ConditionDesc();
	}

	@Override
	public void init(SerializationContext ctx) {
		ctx.add(ConditionArg.RACE, ParseValueType.STRING);
		ctx.add(ConditionArg.VALUE, ParseValueType.VALUE);
		ctx.add(ConditionArg.VALUE1, ParseValueType.VALUE);
		ctx.add(ConditionArg.VALUE2, ParseValueType.VALUE);
		ctx.add(ConditionArg.TARGET_PLAYER, ParseValueType.TARGET_PLAYER);
		ctx.add(ConditionArg.TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(ConditionArg.SECONDARY_TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(ConditionArg.OPERATION, ParseValueType.OPERATION);
		ctx.add(ConditionArg.INVERT, ParseValueType.BOOLEAN);
		ctx.add(ConditionArg.ATTRIBUTE, ParseValueType.ATTRIBUTE);
		ctx.add(ConditionArg.CARD_TYPE, ParseValueType.CARD_TYPE);
		ctx.add(ConditionArg.CONDITIONS, ParseValueType.CONDITION_ARRAY);
		ctx.add(ConditionArg.CARD, ParseValueType.STRING);
		ctx.add(ConditionArg.CARDS, ParseValueType.STRING_ARRAY);
		ctx.add(ConditionArg.FILTER, ParseValueType.ENTITY_FILTER);
		ctx.add(ConditionArg.CARD_FILTER, ParseValueType.ENTITY_FILTER);
		ctx.add(ConditionArg.HERO_CLASS, ParseValueType.STRING);
		ctx.add(ConditionArg.DESCRIPTION, ParseValueType.STRING);
		ctx.add(ConditionArg.RARITY, ParseValueType.RARITY);
	}

	@Override
	protected Class<Condition> getAbstractComponentClass() {
		return Condition.class;
	}

	@Override
	protected Class<ConditionArg> getEnumType() {
		return ConditionArg.class;
	}
}
