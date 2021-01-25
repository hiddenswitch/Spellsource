package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

/**
 * The base class for game events, or things that happen during the execution of a {@link
 * net.demilich.metastone.game.actions.GameAction} that other game rules may react to.
 * <p>
 * A {@code null} source will resolve to the {@link net.demilich.metastone.game.targeting.EntityReference#TRIGGER_HOST}
 * when evaluating queueing and firing conditions.
 *
 * @see net.demilich.metastone.game.spells.trigger.Trigger for how to use game events to implement card text.
 * @see net.demilich.metastone.game.logic.GameLogic#fireGameEvent(GameEvent) for how game events are fired.
 */
public abstract class GameEvent implements Notification, Cloneable {
	private transient final WeakReference<GameContext> context;
	private transient final WeakReference<Entity> source;
	private transient final WeakReference<Entity> target;
	private final int targetPlayerId;
	private final int sourcePlayerId;

	public GameEvent(@NotNull GameContext context, Player player, Entity source, Entity target) {
		this.context = new WeakReference<>(context);
		this.source = new WeakReference<>(source);
		this.target = new WeakReference<>(target);
		sourcePlayerId = player.getId();
		targetPlayerId = target == null ? -1 : target.getOwner();
	}

	public GameEvent(@NotNull GameContext context, Entity source, Entity target, int sourcePlayerId, int targetPlayerId) {
		this.context = new WeakReference<>(context);
		this.source = new WeakReference<>(source);
		this.target = new WeakReference<>(target);
		this.sourcePlayerId = sourcePlayerId;
		this.targetPlayerId = targetPlayerId;
	}


	@Override
	public Entity getSource() {
		return source.get();
	}

	public Entity getTarget() {
		return target.get();
	}


	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		final Entity target = getTarget();
		return target == null ? Collections.emptyList() : Collections.singletonList(target);
	}

	public abstract GameEventType getEventType();

	public GameContext getGameContext() {
		return context.get();
	}

	public int getTargetPlayerId() {
		return targetPlayerId;
	}

	public int getSourcePlayerId() {
		return sourcePlayerId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
				.append("eventType", getEventType())
				.append("source", getSource())
				.append("target", getTarget())
				.toString();
	}

	@Override
	public boolean isPowerHistory() {
		return false;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return toString();
	}

	@Override
	protected GameEvent clone() {
		try {
			return (GameEvent) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}

