package net.demilich.metastone.game.spells.desc.trigger;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.EventTriggerDescDeserializer;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.trigger.EventTrigger;

@JsonDeserialize(using = EventTriggerDescDeserializer.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public final class EventTriggerDesc extends Desc<EventTriggerArg, EventTrigger> {

	public EventTriggerDesc() {
		super();
	}

	public EventTriggerDesc(Class<? extends EventTrigger> triggerClass) {
		super(triggerClass);
	}

	public EventTriggerDesc(Map<EventTriggerArg, Object> arguments) {
		super(arguments);
	}

	@Override
	protected Class<? extends Desc> getDescImplClass() {
		return EventTriggerDesc.class;
	}

	@Override
	public EventTriggerArg getClassArg() {
		return EventTriggerArg.CLASS;
	}

	@Override
	public EventTriggerDesc clone() {
		return (EventTriggerDesc) copyTo(new EventTriggerDesc(getDescClass()));
	}

	public TargetPlayer getSourcePlayer() {
		return (TargetPlayer) get(EventTriggerArg.SOURCE_PLAYER);
	}

	public TargetPlayer getTargetPlayer() {
		return (TargetPlayer) get(EventTriggerArg.TARGET_PLAYER);
	}

}
