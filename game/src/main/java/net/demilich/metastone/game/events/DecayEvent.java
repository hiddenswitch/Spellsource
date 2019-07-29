package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public final class DecayEvent extends GameEvent implements HasValue {

	private Entity target;

	public DecayEvent(GameContext context, int targetPlayerId, Entity target) {
		super(context, targetPlayerId, -1);
		this.target = target;
	}

	@Override
	public Entity getEventTarget() {
		return target;
	}

	@Override
	public Entity getTarget() {
		return target;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.DECAY;
	}

	@Override
	public int getValue() {
		return 1;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s took 1 fatigue damage", context.getPlayer(playerId).getName());
	}
}
