package net.demilich.metastone.game.spells.desc.condition;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.cards.desc.ConditionDescDeserializer;
import net.demilich.metastone.game.cards.desc.Desc;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(using = ConditionDescDeserializer.class)
public class ConditionDesc extends Desc<ConditionArg, Condition> {

	public ConditionDesc() {
		super();
	}

	public ConditionDesc(Class<? extends Condition> conditionClass) {
		super(conditionClass);
	}

	public ConditionDesc(Map<ConditionArg, Object> arguments) {
		super(arguments);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return ConditionDesc.class;
	}

	@Override
	public ConditionArg getClassArg() {
		return ConditionArg.CLASS;
	}

	@Override
	public ConditionDesc clone() {
		return (ConditionDesc)copyTo(new ConditionDesc(getDescClass()));
	}
}
