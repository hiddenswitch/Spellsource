package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.FilterArg;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterDesc;

public class EntityFilterDescDeserializer extends DescDeserializer<EntityFilterDesc, FilterArg, EntityFilter> {

	public EntityFilterDescDeserializer() {
		super(EntityFilterDesc.class);
	}

	@Override
	protected EntityFilterDesc createDescInstance() {
		return new EntityFilterDesc();
	}

	@Override
	protected void init(SerializationContext ctx) {

		ctx.add(FilterArg.VALUE, ParseValueType.VALUE);
		ctx.add(FilterArg.TARGET_PLAYER, ParseValueType.TARGET_PLAYER);
		ctx.add(FilterArg.ATTRIBUTE, ParseValueType.ATTRIBUTE);
		ctx.add(FilterArg.RACE, ParseValueType.RACE);
		ctx.add(FilterArg.OPERATION, ParseValueType.OPERATION);
		ctx.add(FilterArg.INVERT, ParseValueType.BOOLEAN);
		ctx.add(FilterArg.CARD_TYPE, ParseValueType.CARD_TYPE);
		ctx.add(FilterArg.CARD_SET, ParseValueType.CARD_SET);
		ctx.add(FilterArg.HERO_CLASS, ParseValueType.HERO_CLASS);
		ctx.add(FilterArg.HERO_CLASSES, ParseValueType.HERO_CLASS_ARRAY);
		ctx.add(FilterArg.RARITY, ParseValueType.RARITY);
		ctx.add(FilterArg.MANA_COST, ParseValueType.VALUE);
		ctx.add(FilterArg.CARD_ID, ParseValueType.STRING);
		ctx.add(FilterArg.FILTERS, ParseValueType.ENTITY_FILTER_ARRAY);
		ctx.add(FilterArg.TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(FilterArg.SECONDARY_TARGET, ParseValueType.TARGET_REFERENCE);

	}

	@Override
	protected Class<EntityFilter> getAbstractComponentClass() {
		return EntityFilter.class;
	}

	@Override
	protected Class<FilterArg> getEnumType() {
		return FilterArg.class;
	}
}
