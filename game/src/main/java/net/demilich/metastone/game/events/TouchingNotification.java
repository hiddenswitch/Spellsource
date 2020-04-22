package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Collections;
import java.util.List;

/**
 * An entity is being "touched" by the client.
 */
public class TouchingNotification implements Notification {
	private final EntityReference entityReference;
	private final boolean touched;
	private final int playerId;

	public TouchingNotification(int playerId, int entityId, boolean touched) {
		this.entityReference = new EntityReference(entityId);
		this.playerId = playerId;
		this.touched = touched;
	}

	@Override
	public Entity getSource() {
		throw new UnsupportedOperationException("use playerId");
	}

	@Override
	public Entity getSource(GameContext context) {
		return context.resolveSingleTarget(entityReference);
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		return Collections.singletonList(context.resolveSingleTarget(entityReference));
	}

	@Override
	public boolean isPowerHistory() {
		return false;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return null;
	}

	public EntityReference getEntityReference() {
		return entityReference;
	}

	public boolean isTouched() {
		return touched;
	}

	public int getPlayerId() {
		return playerId;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
