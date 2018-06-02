package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;

public class AuraDescDeserializer extends DescDeserializer<AuraDesc, AuraArg, Aura> {

	public AuraDescDeserializer() {
		super(AuraDesc.class);
	}
	
	@Override
	protected AuraDesc createDescInstance() {
		return new AuraDesc();
	}

	@Override
	protected void init(SerializationContext ctx) {
		ctx.add(AuraArg.FILTER, ParseValueType.ENTITY_FILTER);
		ctx.add(AuraArg.TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(AuraArg.ATTRIBUTE, ParseValueType.ATTRIBUTE);
		ctx.add(AuraArg.APPLY_EFFECT, ParseValueType.SPELL);
		ctx.add(AuraArg.REMOVE_EFFECT, ParseValueType.SPELL);
		ctx.add(AuraArg.ATTACK_BONUS, ParseValueType.VALUE);
		ctx.add(AuraArg.HP_BONUS, ParseValueType.VALUE);
		ctx.add(AuraArg.SECONDARY_TRIGGER, ParseValueType.EVENT_TRIGGER);
		ctx.add(AuraArg.REVERT_TRIGGER, ParseValueType.EVENT_TRIGGER);
		ctx.add(AuraArg.CONDITION, ParseValueType.CONDITION);
		ctx.add(AuraArg.CARD, ParseValueType.STRING);
	}

	@Override
	protected Class<Aura> getAbstractComponentClass() {
		return Aura.class;
	}

	@Override
	protected Class<AuraArg> getEnumType() {
		return AuraArg.class;
	}
}
