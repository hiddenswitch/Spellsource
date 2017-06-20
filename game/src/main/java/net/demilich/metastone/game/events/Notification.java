package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;

import java.io.Serializable;

/**
 * Represents a general notification from inside the {@link net.demilich.metastone.game.logic.GameLogic} that the
 * {@link GameContext} or players might be interested in.
 * <p>
 * Unlike a {@link GameEvent}, a notification does not say anything about having side effects or triggering other rules.
 */
public interface Notification extends Serializable {
	GameContext getGameContext();
}
