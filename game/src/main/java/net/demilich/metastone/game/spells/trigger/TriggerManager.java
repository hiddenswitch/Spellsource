package net.demilich.metastone.game.spells.trigger;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.events.HasValue;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The trigger manager contains the code for managing triggers and actually processing events' effects in the game.
 */
public class TriggerManager implements Cloneable, Serializable {
	public static Logger logger = LoggerFactory.getLogger(TriggerManager.class);

	private final List<Trigger> triggers = new ArrayList<Trigger>();
	private int depth = 0;

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
			logger.warn("addTrigger {}: Warning, many triggers: {}", trigger, triggers.size());
		}
	}

	@Override
	public TriggerManager clone() {
		return new TriggerManager(this);
	}

	public void dispose() {
		triggers.clear();
	}

	/**
	 * The core implementation of firing game events.
	 * <p>
	 * This method processes an {@code event}, checking each trigger to see if it should respond to that particular
	 * event.
	 * <p>
	 * This method also manages various environment stacks, like the {@link GameContext#getEventValueStack()} if the event
	 * has a value (like a {@link net.demilich.metastone.game.events.DamageEvent} or the {@link
	 * GameContext#getEventTargetStack()} that helps the {@link EntityReference#EVENT_TARGET} entity reference to work.
	 * <p>
	 * This method will also remove triggers that are expired.
	 *
	 * @param event
	 * @param gameTriggers
	 */
	@Suspendable
	public void fireGameEvent(GameEvent event, List<Trigger> gameTriggers) {
		if (Strand.currentStrand().isInterrupted()) {
			return;
		}
		depth++;
		if (depth > 96) {
			throw new IllegalStateException("infinite recursion");
		}
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
			EntityReference hostReference = trigger.getHostReference();
			if (hostReference == null) {
				hostReference = EntityReference.NONE;
			}
			event.getGameContext().getTriggerHostStack().push(hostReference);
			// In order to stop premature expiration, check
			// for a oneTurnOnly tag and that it isn't delayed.
			if (event.getEventType() == com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.TURN_END) {
				if (trigger.oneTurnOnly() &&
						!trigger.interestedIn(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.TURN_START) &&
						!trigger.interestedIn(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.TURN_END)) {
					trigger.expire();
				}
			}
			if (trigger.isExpired()) {
				removeTriggers.add(trigger);
			}

			if (trigger.interestedIn(event.getEventType())
					&& triggers.contains(trigger) && trigger.queues(event)) {
				eventTriggers.add(trigger);
			}
			event.getGameContext().getTriggerHostStack().pop();
		}

		for (Trigger trigger : eventTriggers) {
			EntityReference hostReference = trigger.getHostReference();
			if (hostReference == null) {
				hostReference = EntityReference.NONE;
			}

			event.getGameContext().getTriggerHostStack().push(hostReference);

			if (trigger.fires(event) && triggers.contains(trigger)) {
				trigger.onGameEvent(event);
			}

			// we need to double check here if the trigger still exists;
			// after all, a previous trigger may have removed it (i.e. double
			// corruption)
			if (trigger.isExpired()) {
				removeTriggers.add(trigger);
			}

			try {
				event.getGameContext().getTriggerHostStack().pop();
			} catch (NoSuchElementException | IndexOutOfBoundsException noSuchElement) {
				// If the game is over, don't worry about the host stack not having an item.
				logger.error("fireGameEvent loop", noSuchElement);
				continue;
			}
		}

		triggers.removeAll(removeTriggers);

		try {
			event.getGameContext().getEventValueStack().pop();
			event.getGameContext().getEventSourceStack().pop();
			event.getGameContext().getEventTargetStack().pop();
		} catch (IndexOutOfBoundsException | NoSuchElementException ex) {
			logger.error("fireGameEvent", ex);
		}
		depth--;
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

	public void removeTriggersAssociatedWith(EntityReference entityReference, boolean removeAuras, boolean keepSelfCardCostModifiers, GameContext context) {
		for (Trigger trigger : getListSnapshot(triggers)) {
			if (trigger.getHostReference().equals(entityReference)) {
				if (!removeAuras && trigger instanceof Aura) {
					continue;
				}
				if (keepSelfCardCostModifiers && trigger instanceof CardCostModifier && ((CardCostModifier) trigger).targetsSelf()) {
					continue;
				}
				trigger.onRemove(context);
				trigger.expire();
				triggers.remove(trigger);
			}
		}
	}

	public List<Trigger> getTriggers() {
		return triggers;
	}

	/**
	 * Expires all triggers in the game, to prevent end-of-game triggering from causing the game to glitch out
	 */
	public void expireAll() {
		for (Trigger trigger : triggers) {
			trigger.expire();
		}
	}
}
