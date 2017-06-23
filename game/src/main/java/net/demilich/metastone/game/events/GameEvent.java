package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

import java.util.Collections;
import java.util.List;

/**
 * The base class for game events, or things that happen during the execution of a {@link
 * net.demilich.metastone.game.actions.GameAction} that other game rules may react to.
 *
 * @see net.demilich.metastone.game.spells.trigger.Trigger for how to use game events to implement card text.
 * @see GameContext#fireGameEvent(GameEvent) for how game events are fired.
 */
public abstract class GameEvent implements Notification {
	private transient final GameContext context;
	private final int targetPlayerId;
	private final int sourcePlayerId;

	public GameEvent(GameContext context, int targetPlayerId, int sourcePlayerId) {
		this.context = context;
		this.targetPlayerId = targetPlayerId;
		this.sourcePlayerId = sourcePlayerId;
	}

	public Entity getSource() {
		return getEventSource();
	}

	public Entity getTarget() {
		return getEventTarget();
	}

	@Override
	public Entity getSource(GameContext context) {
		return getSource();
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		final Entity target = getTarget();
		return target == null ? Collections.emptyList() : Collections.singletonList(target);
	}

	/**
	 * Spells may specify to be cast on the event target; this is dependent on
	 * the actual event. For example, a SummonEvent may return the summoned
	 * minion, a DamageEvent may return the damaged minion/hero, etc.
	 *
	 * @return
	 */
	public abstract Entity getEventTarget();

	public Entity getEventSource() {
		return null;
	}

	public abstract GameEventType getEventType();

	public GameContext getGameContext() {
		return context;
	}

	public int getTargetPlayerId() {
		return targetPlayerId;
	}

	public int getSourcePlayerId() {
		return sourcePlayerId;
	}

	@Override
	public String toString() {
		return "[EVENT " + getClass().getSimpleName() + "]";
	}

	@Override
	public boolean isPowerHistory() {
		return false;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return toString();
	}
}
