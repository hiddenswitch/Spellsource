package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Collections;
import java.util.List;

public class TouchingNotification implements Notification {
	private EntityReference entityReference;
	private boolean touched;
	private int playerId;

	public TouchingNotification(int playerId, int entityId, boolean touched) {
		this.entityReference = new EntityReference(entityId);
		this.playerId = playerId;
		this.touched = touched;
	}

	@Override
	public Entity getSource(GameContext context) {
		return context.getPlayer(playerId);
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
		return "Touching.";
	}

	public EntityReference getEntityReference() {
		return entityReference;
	}

	public void setEntityReference(EntityReference entityReference) {
		this.entityReference = entityReference;
	}

	public boolean isTouched() {
		return touched;
	}

	public void setTouched(boolean touched) {
		this.touched = touched;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getPlayerId() {
		return playerId;
	}
}
