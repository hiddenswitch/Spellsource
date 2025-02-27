package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.trigger.secrets.Secret;
import net.demilich.metastone.game.targeting.EntityReference;

import java.io.Serializable;

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
	 * Checks if a trigger should queue in response to a specific event.
	 *
	 * @param event A game event.
	 * @return {@code true} if the trigger should queue in response to this event.
	 */
	boolean queues(GameEvent event);

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
	 * #queues(GameEvent)} to the specified {@code eventType}.
	 */
	boolean interestedIn(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType eventType);

	/**
	 * Checks if, due to the execution of possibly complex rules inside or outside the trigger, the trigger is expired (no
	 * longer should fire).
	 *
	 * @return {@code true} if the trigger should be removed and will no longer fire.
	 */
	boolean isExpired();

	/**
	 * Called when the trigger is added into a {@link GameContext}'s state.
	 *
	 * @param context The game context.
	 * @param player
	 * @param source
	 * @param host
	 */
	void onAdd(GameContext context, Player player, Entity source, Entity host);

	/**
	 * Handles an event this trigger {@link #queues(GameEvent)} for and is {@link #interestedIn(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType)}.
	 *
	 * @param event The game event this trigger is now processing.
	 */
	void onGameEvent(GameEvent event);

	/**
	 * Sets or changes the {@link Entity} that is the owner / host of this trigger.
	 *
	 * @param entityReference The host {@link Entity}/
	 */
	void setHostReference(EntityReference entityReference);

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
	 * @return {@code true} if the trigger's {@link #onGameEvent(GameEvent)} should be evaluated from the point of view of
	 * the owner when the trigger was created, as opposed to what the owner is right now (which may have changed).
	 */
	boolean isPersistentOwner();

	/**
	 * Indicates this trigger is only active for the current turn, then it should {@link #expire(GameContext)}.
	 *
	 * @return {@code true} if this is a one-turn long trigger.
	 */
	boolean oneTurnOnly();

	/**
	 * Expires the trigger; marks it for removal and prevents it from executing in the future.
	 * @param context
	 */
	void expire(GameContext context);

	/**
	 * Returns {@code true} if the trigger fire in response to the given {@link GameEvent}.
	 * <p>
	 * It is already queued in this situation.
	 *
	 * @param event The currently raised event.
	 * @return {@code true} if the trigger can fire.
	 */
	default boolean fires(GameEvent event) {
		return queues(event);
	}

	boolean isActivated();
}