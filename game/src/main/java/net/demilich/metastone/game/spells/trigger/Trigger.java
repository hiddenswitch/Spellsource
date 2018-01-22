package net.demilich.metastone.game.spells.trigger;

import java.io.Serializable;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Triggers respond to {@link GameEvent} objects that are raised by various {@link GameLogic} methods, implementing
 * cards that do something when something else happens.
 *
 * @see Enchantment for an implementation that casts a spell when an event is raised. Most trigger effects behave like
 * this.
 */
public interface Trigger extends Serializable {
	/**
	 * Clones the trigger with any internal state.
	 *
	 * @return A clone of this trigger.
	 */
	Trigger clone();

	/**
	 * Checks if a trigger can fire in response to a specific event.
	 *
	 * @param event A game event.
	 * @return {@code true} if the trigger can fire in response to this event.
	 */
	boolean canFire(GameEvent event);

	/**
	 * Gets a reference to the {@link Entity} that is "hosting," or owning, the trigger.
	 *
	 * @return An entity reference. This is typically not null.
	 */
	EntityReference getHostReference();

	/**
	 * Gets the player who owns this trigger.
	 *
	 * @return {@link GameContext#PLAYER_1} or {@link GameContext#PLAYER_2}.
	 */
	int getOwner();

	/**
	 * Returns true if this trigger is a listener for the given {@link GameEventType}.
	 *
	 * @param eventType The event type.
	 * @return {@code true} if this trigger wants its {@link #onGameEvent(GameEvent)} method called whenever it {@link
	 * #canFire(GameEvent)} to the specified {@code eventType}.
	 */
	boolean interestedIn(GameEventType eventType);

	/**
	 * Checks if, due to the execution of possibly complex rules inside or outside the trigger, the trigger is expired
	 * (no longer should fire).
	 *
	 * @return {@code true} if the trigger should be removed and will no longer fire.
	 */
	boolean isExpired();

	/**
	 * Called when the trigger is added into a {@link GameContext}'s state.
	 *
	 * @param context The game context.
	 */
	void onAdd(GameContext context);

	/**
	 * Handles an event this trigger {@link #canFire(GameEvent)} for and is {@link #interestedIn(GameEventType)}.
	 *
	 * @param event The game event this trigger is now processing.
	 */
	@Suspendable
	void onGameEvent(GameEvent event);

	/**
	 * Called when the trigger is removed from the given game context.
	 *
	 * @param context The game context.
	 */
	@Suspendable
	void onRemove(GameContext context);

	/**
	 * Sets or changes the {@link Entity} that is the owner / host of this trigger.
	 *
	 * @param host The host {@link Entity}/
	 */
	void setHost(Entity host);

	/**
	 * Sets the player who is the owner of this trigger.
	 *
	 * @param playerIndex {@link GameContext#PLAYER_1} or {@link GameContext#PLAYER_2}.
	 */
	void setOwner(int playerIndex);

	/**
	 * Indicates this trigger cannot change owners. For example, a {@link Secret} does not have a persistent owner,
	 * because card texts can steal secrets. However, the trigger described by Blessing of Wisdom does have a persistent
	 * owner, since the casting player of that spell should always draw the card it receives.
	 *
	 * @return {@code true} if the trigger's {@link #onGameEvent(GameEvent)} should be evaluated from the point of view
	 * of the owner when the trigger was created, as opposed to what the owner is right now (which may have changed).
	 */
	boolean hasPersistentOwner();

	/**
	 * Indicates this trigger is only active for the current turn, then it should {@link #expire()}.
	 *
	 * @return {@code true} if this is a one-turn long trigger.
	 */
	boolean oneTurnOnly();

	/**
	 * Returns true if the effects of this trigger are delayed until {@link #delayTimeDown()} is called (typically
	 * at the end of a turn).
	 *
	 * @return {@code true} if the effects of the trigger are delayed.
	 */
	boolean isDelayed();

	/**
	 * Notifies the trigger that its delay has elapsed.
	 */
	void delayTimeDown();

	/**
	 * Expires the trigger; marks it for removal and prevents it from executing in the future.
	 */
	void expire();

	/**
	 * Returns {@code true} if the trigger can fire in response to the given {@link GameEvent} given a possible {@link
	 * net.demilich.metastone.game.spells.desc.condition.Condition}
	 *
	 * @param event The currently raised event.
	 * @return {@code true} if the trigger can fire.
	 */
	default boolean canFireCondition(GameEvent event) {
		return canFire(event);
	}
}