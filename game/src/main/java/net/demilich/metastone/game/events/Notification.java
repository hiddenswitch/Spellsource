package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.common.Writer;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a general notification from inside the {@link net.demilich.metastone.game.logic.GameLogic} that the {@link
 * GameContext} or players might be interested in.
 * <p>
 * Unlike a {@link GameEvent}, a notification does not say anything about having side effects or triggering other
 * rules.
 */
public interface Notification extends Serializable {
	/**
	 * For visualization purposes, what is the source of this notification?
	 *
	 * @return A reference to the entity that is the visualizable source of this notification.
	 */
	Entity getSource(GameContext context);

	/**
	 * For visualization purposes, what are the targets of this notification?
	 *
	 * @return A reference to the entity that is the visualizable target of this notification.
	 */
	List<Entity> getTargets(GameContext context, int player);

	/**
	 * When true, indicates to processors of this notification that it belongs in the power history.
	 *
	 * @return {@code true} if this notification should be stored in the power history of the game where it occurred.
	 */
	boolean isPowerHistory();

	/**
	 * A user-renderable description of what occurred in this notification.
	 *
	 * @param context
	 * @param playerId
	 * @return
	 */
	String getDescription(GameContext context, int playerId);

	/**
	 * Should this notification be sent in the {@link Writer#onNotification(Notification,
	 * GameState)} pipeline altogether?
	 *
	 * @return {@code false} by default.
	 */
	default boolean isClientInterested() {
		return false;
	}
}

