package net.demilich.metastone.game.spells.desc.aura;

import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;

public class AuraDesc extends Desc<AuraArg, Aura> {

	public AuraDesc(Class<? extends Aura> clazz) {
		super(clazz);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return AuraDesc.class;
	}

	public AuraDesc(Map<AuraArg, Object> arguments) {
		super(arguments);
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

	public EventTriggerDesc getSecondaryTrigger() {
		return ((EventTriggerDesc) getOrDefault(AuraArg.SECONDARY_TRIGGER, null));
	}
}
