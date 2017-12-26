package net.demilich.metastone.game.spells.desc.trigger;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.trigger.EventTrigger;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public final class EventTriggerDesc extends Desc<EventTriggerArg> {

	public static Map<EventTriggerArg, Object> build(Class<? extends EventTrigger> triggerClass) {
		final Map<EventTriggerArg, Object> arguments = new EnumMap<>(EventTriggerArg.class);
		arguments.put(EventTriggerArg.CLASS, triggerClass);
		return arguments;
	}

	public static EventTriggerDesc createEmpty(Class<? extends EventTrigger> triggerClass) {
		return new EventTriggerDesc(EventTriggerDesc.build(triggerClass));
	}

	public EventTriggerDesc(Map<EventTriggerArg, Object> arguments) {
		super(arguments);
	}

	public EventTrigger create() {
		Class<? extends EventTrigger> triggerClass = getTriggerClass();
		try {
			return triggerClass.getConstructor(EventTriggerDesc.class).newInstance(this);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	public TargetPlayer getSourcePlayer() {
		return (TargetPlayer) get(EventTriggerArg.SOURCE_PLAYER);
	}

	public TargetPlayer getTargetPlayer() {
		return (TargetPlayer) get(EventTriggerArg.TARGET_PLAYER);
	}

	@SuppressWarnings("unchecked")
	public Class<? extends EventTrigger> getTriggerClass() {
		return (Class<? extends EventTrigger>) get(EventTriggerArg.CLASS);
	}

}
