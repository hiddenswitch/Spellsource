package net.demilich.metastone.game.cards.dynamicdescription;

import net.demilich.metastone.game.cards.desc.DescDeserializer;
import net.demilich.metastone.game.cards.desc.ParseValueType;

/**
 * A deserializer of dynamic descriptions.
 */
public class DynamicDescriptionDeserializer extends DescDeserializer<DynamicDescriptionDesc, DynamicDescriptionArg, DynamicDescription> {

	public DynamicDescriptionDeserializer() {
		super(DynamicDescriptionDesc.class);
	}

	protected DynamicDescriptionDeserializer(Class<? extends DynamicDescriptionDesc> vc) {
		super(vc);
	}

	@Override
	protected DynamicDescriptionDesc createDescInstance() {
		return new DynamicDescriptionDesc();
	}

	@Override
	public void init(SerializationContext ctx) {
		ctx.add(DynamicDescriptionArg.CONDITION, ParseValueType.CONDITION);
		ctx.add(DynamicDescriptionArg.VALUE, ParseValueType.VALUE);
		ctx.add(DynamicDescriptionArg.STRING, ParseValueType.STRING);
		ctx.add(DynamicDescriptionArg.DESCRIPTION1, ParseValueType.DYNAMIC_DESCRIPTION);
		ctx.add(DynamicDescriptionArg.DESCRIPTION2, ParseValueType.DYNAMIC_DESCRIPTION);
		ctx.add(DynamicDescriptionArg.DESCRIPTIONS, ParseValueType.DYNAMIC_DESCRIPTION_ARRAY);
	}

	@Override
	protected Class<DynamicDescription> getAbstractComponentClass() {
		return DynamicDescription.class;
	}

	@Override
	protected Class<DynamicDescriptionArg> getEnumType() {
		return DynamicDescriptionArg.class;
	}
}
