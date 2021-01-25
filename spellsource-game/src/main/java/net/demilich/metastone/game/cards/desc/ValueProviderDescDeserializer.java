package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderDesc;

public class ValueProviderDescDeserializer extends DescDeserializer<ValueProviderDesc, ValueProviderArg, ValueProvider> {

	public ValueProviderDescDeserializer() {
		super(ValueProviderDesc.class);
	}

	@Override
	protected ValueProviderDesc createDescInstance() {
		return new ValueProviderDesc();
	}

	@Override
	public void init(SerializationContext ctx) {
		ctx.add(ValueProviderArg.TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(ValueProviderArg.PLAYER_ATTRIBUTE, ParseValueType.PLAYER_ATTRIBUTE);
		ctx.add(ValueProviderArg.ATTRIBUTE, ParseValueType.ATTRIBUTE);
		ctx.add(ValueProviderArg.MULTIPLIER, ParseValueType.VALUE);
		ctx.add(ValueProviderArg.CARD_SOURCE, ParseValueType.CARD_SOURCE);
		ctx.add(ValueProviderArg.CARD_FILTER, ParseValueType.ENTITY_FILTER);
		ctx.add(ValueProviderArg.VALUE, ParseValueType.INTEGER);
		ctx.add(ValueProviderArg.MIN, ParseValueType.VALUE);
		ctx.add(ValueProviderArg.MAX, ParseValueType.VALUE);
		ctx.add(ValueProviderArg.OFFSET, ParseValueType.VALUE);
		ctx.add(ValueProviderArg.IF_TRUE, ParseValueType.VALUE);
		ctx.add(ValueProviderArg.IF_FALSE, ParseValueType.VALUE);
		ctx.add(ValueProviderArg.RACE, ParseValueType.STRING);
		ctx.add(ValueProviderArg.TARGET_PLAYER, ParseValueType.TARGET_PLAYER);
		ctx.add(ValueProviderArg.CONDITION, ParseValueType.CONDITION);
		ctx.add(ValueProviderArg.FILTER, ParseValueType.ENTITY_FILTER);
		ctx.add(ValueProviderArg.OPERATION, ParseValueType.ALGEBRAIC_OPERATION);
		ctx.add(ValueProviderArg.GAME_VALUE, ParseValueType.GAME_VALUE);
		ctx.add(ValueProviderArg.VALUE1, ParseValueType.VALUE);
		ctx.add(ValueProviderArg.VALUE2, ParseValueType.VALUE);
		ctx.add(ValueProviderArg.EVALUATE_ONCE, ParseValueType.BOOLEAN);
	}

	@Override
	protected Class<ValueProvider> getAbstractComponentClass() {
		return ValueProvider.class;
	}

	@Override
	protected Class<ValueProviderArg> getEnumType() {
		return ValueProviderArg.class;
	}
}
