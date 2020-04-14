package net.demilich.metastone.game.spells.trigger;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.costmodifier.CardCostModifier;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.HasValue;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * The trigger manager processes game events.
 * <p>
 * Every time a game event is fired using {@link GameContext#fireGameEvent(GameEvent)}, the trigger manager iterates
 * through the list of enchantments in the game and determines which ones need to have their spells cast (in the case of
 * plain {@link Enchantment} objects) or their affected entities and effects processed (in the case of {@link Aura})
 * auras.
 * <p>
 * Triggers are executed on a depth-first basis. This means that as soon as a game event is fired, triggers are checked
 * to respond to that event. This is as opposed to a breadth-first basis, where an event is processed only when all the
 * events prior to it have also been processed.
 * <p>
 * When a trigger fires, its effects could cause an event which causes it to fire again. This is called a recursive
 * trigger, and in this situation, the trigger manager "delays" the second firing until the remaining side effects of
 * the first firing have been processed. Recursion still correctly occurs, it is just delayed.
 */
public class TriggerManager implements Cloneable, Serializable {
	public static Logger LOGGER = LoggerFactory.getLogger(TriggerManager.class);

	private final List<Trigger> triggers = new ArrayList<Trigger>();
	private final Deque<QueuedTrigger> deferredTriggersQueue = new ArrayDeque<>();
	private final Set<Trigger> processingTriggers = new HashSet<>();
	private int depth = 0;

	public TriggerManager() {
	}

	private TriggerManager(TriggerManager otherTriggerManager) {
		for (Trigger gameEventListener : otherTriggerManager.triggers) {
			triggers.add(gameEventListener.clone());
		}
	}

	public void addTrigger(Trigger trigger) {
		var index = triggers.size();
		triggers.add(trigger);
		if (trigger instanceof Enchantment) {
			var enchantment = (Enchantment) trigger;
			if (Objects.equals(enchantment.getEntityLocation(), EntityLocation.UNASSIGNED)) {
				enchantment.setEntityLocation(new EntityLocation(Zones.ENCHANTMENT, trigger.getOwner(), index));
			}
		}
		if (triggers.size() > 100) {
			LOGGER.warn("addTrigger {}: Warning, many triggers: {}", trigger, triggers.size());
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

		// Push the event data onto the event data stack, used by effects to determine what the EntityReference.EVENT_TARGET
		// is and the value of EventValueProvider
		pushEventData(event);

		try {
			// We take a snapshot of the triggers currently in play at the time of evaluation.
			var triggers = new ArrayList<>(this.triggers);
			if (gameTriggers != null
					&& gameTriggers.size() > 0) {
				// Game triggers execute first
				triggers.addAll(0, gameTriggers);
			}

			// We keep track of which triggers were one turn only in order to expire them after they have fired.
			var oneTurnExpires = new ArrayList<Trigger>();

			// Here we keep track of the triggers that were queued by this event
			// Queueing gives a trigger an opportunity to look at the state of the board BEFORE all the other trigger's
			// effects have been evaluated.
			// For example, queueing is an appropriate time to check if a minion should be buffed, e.g. during a summon,
			// because later a different effect may deal damage to and subsequently mark-as-destroyed the minion.
			var thisQueuedTriggers = new ArrayDeque<Trigger>();
			for (Trigger trigger : triggers) {
				if (Strand.currentStrand().isInterrupted()) {
					return;
				}
				pushHostReference(event, trigger);
				try {
					// In order to stop premature expiration, check
					// for a oneTurnOnly tag and that it isn't delayed.
					if (event.getEventType() == EventTypeEnum.TURN_END
							&& trigger.oneTurnOnly()) {
						if (!trigger.interestedIn(EventTypeEnum.TURN_START)
								&& !trigger.interestedIn(EventTypeEnum.TURN_END)) {
							trigger.expire();
						} else {
							oneTurnExpires.add(trigger);
						}
					}

					if (trigger.interestedIn(event.getEventType())
							&& trigger.queues(event)) {
						// We're already processing this trigger, recursively, so we will reevaluate it at the end of this sequence
						if (processingTriggers.contains(trigger)) {
							deferredTriggersQueue.addLast(new QueuedTrigger(event, trigger));
						} else {
							thisQueuedTriggers.addLast(trigger);
						}
					}
				} finally {
					popHostReference(event);
				}
			}

			while (!thisQueuedTriggers.isEmpty()) {
				if (Strand.currentStrand().isInterrupted()) {
					return;
				}
				var trigger = thisQueuedTriggers.poll();
				processTrigger(event, trigger);
			}

			if (processingTriggers.isEmpty()) {
				while (!deferredTriggersQueue.isEmpty()) {
					if (Strand.currentStrand().isInterrupted()) {
						return;
					}

					var deferred = deferredTriggersQueue.poll();
					var trigger = deferred.getTrigger();
					var deferredEvent = deferred.getEvent();

					pushEventData(deferredEvent);
					pushHostReference(deferredEvent, trigger);
					try {
						processTrigger(deferredEvent, trigger);
					} finally {
						popEventData(deferredEvent);
						popHostReference(deferredEvent);
					}
				}
			}

			for (var trigger : oneTurnExpires) {
				trigger.expire();
			}
		} finally {
			popEventData(event);
			depth--;
		}
	}

	private void pushHostReference(GameEvent event, Trigger trigger) {
		EntityReference hostReference = trigger.getHostReference();
		if (hostReference == null) {
			hostReference = EntityReference.NONE;
		}
		event.getGameContext().getTriggerHostStack().push(hostReference);
	}

	private void pushEventData(GameEvent event) {
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
	}

	private void popEventData(GameEvent event) {
		event.getGameContext().getEventValueStack().pop();
		event.getGameContext().getEventSourceStack().pop();
		event.getGameContext().getEventTargetStack().pop();
	}

	@Suspendable
	private void processTrigger(GameEvent event, Trigger trigger) {
		if (processingTriggers.contains(trigger)) {
			throw new RuntimeException();
		}

		processingTriggers.add(trigger);
		pushHostReference(event, trigger);

		if (trigger.fires(event)) {
			trigger.onGameEvent(event);
		}

		try {
			popHostReference(event);
		} catch (NoSuchElementException | IndexOutOfBoundsException noSuchElement) {
			// If the game is over, don't worry about the host stack not having an item.
			LOGGER.error("fireGameEvent loop", noSuchElement);
		}

		processingTriggers.remove(trigger);
	}

	private void popHostReference(GameEvent event) {
		event.getGameContext().getTriggerHostStack().pop();
	}

	/**
	 * Gets the unexpired triggerrs (i.e. {@link Enchantment}) that are hosted by the specified reference.
	 *
	 * @param hostReference
	 * @return
	 */
	public List<Trigger> getUnexpiredTriggers(EntityReference hostReference) {
		List<Trigger> relevantTriggers = new ArrayList<>();
		for (Trigger trigger : triggers) {
			if (trigger.getHostReference().equals(hostReference) && !trigger.isExpired()) {
				relevantTriggers.add(trigger);
			}
		}
		return relevantTriggers;
	}

	/**
	 * Expires a trigger.
	 *
	 * @param trigger
	 */
	public void expire(Trigger trigger) {
		trigger.expire();
	}

	/**
	 * Expires triggers on the specified reference.
	 *
	 * @param entityReference
	 * @param removeAuras
	 * @param keepSelfCardCostModifiers
	 * @param context
	 */
	public void expire(EntityReference entityReference, boolean removeAuras, boolean keepSelfCardCostModifiers, GameContext context) {
		for (Trigger trigger : new ArrayList<>(triggers)) {
			if (trigger.isExpired()) {
				continue;
			}
			if (trigger.getHostReference().equals(entityReference)) {
				if (!removeAuras && trigger instanceof Aura) {
					continue;
				}
				if (keepSelfCardCostModifiers && trigger instanceof CardCostModifier && ((CardCostModifier) trigger).targetsSelf()) {
					continue;
				}
				trigger.onRemove(context);
				trigger.expire();
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

	/**
	 * Keeps track of data regarding a queued trigger firing (the tuple of event and trigger that needs to be processed)
	 */
	private static final class QueuedTrigger implements Serializable {
		private final GameEvent event;
		private final Trigger trigger;

		private QueuedTrigger(@NotNull GameEvent event, @NotNull Trigger trigger) {
			this.event = event;
			this.trigger = trigger;
		}

		@NotNull
		public GameEvent getEvent() {
			return event;
		}

		@NotNull
		public Trigger getTrigger() {
			return trigger;
		}
	}
}
