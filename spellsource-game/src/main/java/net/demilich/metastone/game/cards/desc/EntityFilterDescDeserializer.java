package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterArg;
import net.demilich.metastone.game.spells.desc.filter.EntityFilterDesc;

public class EntityFilterDescDeserializer extends DescDeserializer<EntityFilterDesc, EntityFilterArg, EntityFilter> {

	public EntityFilterDescDeserializer() {
		super(EntityFilterDesc.class);
	}

	@Override
	protected EntityFilterDesc createDescInstance() {
		return new EntityFilterDesc();
	}

	@Override
	public void init(SerializationContext ctx) {

		ctx.add(EntityFilterArg.VALUE, ParseValueType.VALUE);
		ctx.add(EntityFilterArg.TARGET_PLAYER, ParseValueType.TARGET_PLAYER);
		ctx.add(EntityFilterArg.ATTRIBUTE, ParseValueType.ATTRIBUTE);
		ctx.add(EntityFilterArg.RACE, ParseValueType.STRING);
		ctx.add(EntityFilterArg.OPERATION, ParseValueType.OPERATION);
		ctx.add(EntityFilterArg.INVERT, ParseValueType.BOOLEAN);
		ctx.add(EntityFilterArg.CARD_TYPE, ParseValueType.CARD_TYPE);
		ctx.add(EntityFilterArg.CARD_SET, ParseValueType.STRING);
		ctx.add(EntityFilterArg.HERO_CLASS, ParseValueType.STRING);
		ctx.add(EntityFilterArg.HERO_CLASSES, ParseValueType.STRING_ARRAY);
		ctx.add(EntityFilterArg.RARITY, ParseValueType.RARITY);
		ctx.add(EntityFilterArg.MANA_COST, ParseValueType.VALUE);
		ctx.add(EntityFilterArg.CARD, ParseValueType.STRING);
		ctx.add(EntityFilterArg.CARDS, ParseValueType.STRING_ARRAY);
		ctx.add(EntityFilterArg.FILTERS, ParseValueType.ENTITY_FILTER_ARRAY);
		ctx.add(EntityFilterArg.TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(EntityFilterArg.SECONDARY_TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(EntityFilterArg.TARGET_SELECTION, ParseValueType.TARGET_SELECTION);
		ctx.add(EntityFilterArg.AND_CONDITION, ParseValueType.CONDITION);
		ctx.add(EntityFilterArg.SPELL, ParseValueType.SPELL);
		ctx.add(EntityFilterArg.ENTITY_TYPE, ParseValueType.ENTITY_TYPE);
	}

	@Override
	protected Class<EntityFilter> getAbstractComponentClass() {
		return EntityFilter.class;
	}

	@Override
	protected Class<EntityFilterArg> getEnumType() {
		return EntityFilterArg.class;
	}
}
