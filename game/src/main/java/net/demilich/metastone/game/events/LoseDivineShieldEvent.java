package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public final class LoseDivineShieldEvent extends GameEvent implements HasVictim {
	private static final long serialVersionUID = 5439584401228970258L;
	private final Entity eventTarget;

	public LoseDivineShieldEvent(GameContext context, Entity loserOfShield, int targetPlayerId, int sourcePlayerId) {
		super(context, targetPlayerId, sourcePlayerId);
		this.eventTarget = loserOfShield;
	}

	@Override
	public Entity getEventTarget() {
		return eventTarget;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.LOSE_DIVINE_SHIELD;
	}

	@Override
	public Entity getVictim() {
		return getEventTarget();
	}
}

