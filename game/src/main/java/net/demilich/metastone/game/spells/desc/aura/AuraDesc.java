package net.demilich.metastone.game.spells.desc.aura;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.cards.desc.AuraDescDeserializer;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

@JsonDeserialize(using = AuraDescDeserializer.class)
public class AuraDesc extends Desc<AuraArg, Aura> {

	public AuraDesc() {
		super(AuraArg.class);
	}

	public AuraDesc(Class<? extends Aura> clazz) {
		super(clazz, AuraArg.class);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return AuraDesc.class;
	}

	public AuraDesc(Map<AuraArg, Object> arguments) {
		super(arguments, AuraArg.class);
	}

	@Override
	public AuraArg getClassArg() {
		return AuraArg.CLASS;
	}

	@Override
	public AuraDesc clone() {
		return (AuraDesc) copyTo(new AuraDesc(getDescClass()));
	}

	public SpellDesc getApplyEffect() {
		return (SpellDesc) get(AuraArg.APPLY_EFFECT);
	}

	public Attribute getAttribute() {
		return (Attribute) get(AuraArg.ATTRIBUTE);
	}

	public EntityFilter getFilter() {
		return (EntityFilter) get(AuraArg.FILTER);
	}

	public SpellDesc getRemoveEffect() {
		return (SpellDesc) get(AuraArg.REMOVE_EFFECT);
	}

	public EntityReference getTarget() {
		return (EntityReference) get(AuraArg.TARGET);
	}

	public Condition getCondition() {
		return (Condition) get(AuraArg.CONDITION);
	}

	public SpellDesc getSpell() {
		return (SpellDesc) get(AuraArg.SPELL);
	}

	public EventTriggerDesc getSecondaryTrigger() {
		return ((EventTriggerDesc) getOrDefault(AuraArg.SECONDARY_TRIGGER, null));
	}

	public EventTriggerDesc getRevertTrigger() {
		return ((EventTriggerDesc) getOrDefault(AuraArg.REVERT_TRIGGER, null));
	}

	public int getValue() {
		return (int) get(AuraArg.VALUE);
	}

	public EntityReference getSecondaryTarget() {
		return (EntityReference) get(AuraArg.SECONDARY_TARGET);
	}
}
