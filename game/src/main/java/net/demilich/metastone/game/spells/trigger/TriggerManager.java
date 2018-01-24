package net.demilich.metastone.game.spells.trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.events.HasValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.targeting.EntityReference;

public class TriggerManager implements Cloneable, Serializable {
	public static Logger logger = LoggerFactory.getLogger(TriggerManager.class);

	private final List<Trigger> triggers = new ArrayList<Trigger>();

	public TriggerManager() {
	}

	private TriggerManager(TriggerManager otherTriggerManager) {
		for (Trigger gameEventListener : otherTriggerManager.triggers) {
			triggers.add(gameEventListener.clone());
		}
	}

	public void addTrigger(Trigger trigger) {
		triggers.add(trigger);
		if (triggers.size() > 100) {
			logger.warn("Warning, many triggers: " + triggers.size() + " adding one of type: " + trigger);
		}
	}

	@Override
	public TriggerManager clone() {
		return new TriggerManager(this);
	}

	public void dispose() {
		triggers.clear();
	}

	@Suspendable
	public void fireGameEvent(GameEvent event, List<Trigger> gameTriggers) {
		if (event instanceof HasValue) {
			event.getGameContext().getEventValueStack().push(((HasValue) event).getValue());
		} else {
			event.getGameContext().getEventValueStack().push(0);
		}

		if (event.getEventTarget() != null) {
			event.getGameContext().getEventTargetStack().push(event.getEventTarget().getReference());
		} else {
			event.getGameContext().getEventTargetStack().push(EntityReference.NONE);
		}

		if (event.getEventSource() != null) {
			event.getGameContext().getEventSourceStack().push(event.getEventSource().getReference());
		} else {
			event.getGameContext().getEventSourceStack().push(EntityReference.NONE);
		}

		List<Trigger> triggers = new ArrayList<>(this.triggers);
		if (gameTriggers != null
				&& gameTriggers.size() > 0) {
			// Game triggers execute first and do not serialize
			triggers.addAll(0, gameTriggers);
		}
		List<Trigger> eventTriggers = new ArrayList<Trigger>();
		List<Trigger> removeTriggers = new ArrayList<Trigger>();

		for (Trigger trigger : triggers) {
			// In order to stop premature expiration, check
			// for a oneTurnOnly tag and that it isn't delayed.
			if (event.getEventType() == GameEventType.TURN_END) {
				if (trigger.oneTurnOnly() && !trigger.isDelayed() &&
						!trigger.interestedIn(GameEventType.TURN_START) &&
						!trigger.interestedIn(GameEventType.TURN_END)) {
					trigger.expire();
				}
				trigger.delayTimeDown();
			}
			if (trigger.isExpired()) {
				removeTriggers.add(trigger);
			}

			if (!trigger.interestedIn(event.getEventType())) {
				continue;
			}
			if (triggers.contains(trigger) && trigger.canFire(event)) {
				eventTriggers.add(trigger);
			}
		}

		for (Trigger trigger : eventTriggers) {
			if (trigger.canFireCondition(event) && triggers.contains(trigger)) {
				trigger.onGameEvent(event);
			}

			// we need to double check here if the trigger still exists;
			// after all, a previous trigger may have removed it (i.e. double
			// corruption)
			if (trigger.isExpired()) {
				removeTriggers.add(trigger);
			}
		}

		triggers.removeAll(removeTriggers);

		event.getGameContext().getEventValueStack().pop();
		event.getGameContext().getEventSourceStack().pop();
		event.getGameContext().getEventTargetStack().pop();
	}

	private List<Trigger> getListSnapshot(List<Trigger> triggerList) {
		return new ArrayList<>(triggerList);
	}

	public List<Trigger> getTriggersAssociatedWith(EntityReference entityReference) {
		List<Trigger> relevantTriggers = new ArrayList<>();
		for (Trigger trigger : triggers) {
			if (trigger.getHostReference().equals(entityReference)) {
				relevantTriggers.add(trigger);
			}
		}
		return relevantTriggers;
	}

	public void removeTrigger(Trigger trigger) {
		if (!triggers.remove(trigger)) {
			throw new RuntimeException("Trigger unexpectedly was unable to be removed.");
		}

		trigger.expire();
	}

	public void removeTriggersAssociatedWith(EntityReference entityReference, boolean removeAuras) {
		for (Trigger trigger : getListSnapshot(triggers)) {
			if (trigger.getHostReference().equals(entityReference)) {
				if (!removeAuras && trigger instanceof Aura) {
					continue;
				}
				trigger.expire();
				triggers.remove(trigger);
			}
		}
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}
}
