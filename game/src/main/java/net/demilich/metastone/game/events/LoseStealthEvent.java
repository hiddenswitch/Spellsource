package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public final class LoseStealthEvent extends GameEvent implements HasVictim {

	private final Entity eventTarget;

	public LoseStealthEvent(GameContext context, Entity loserOfStealth, int sourcePlayerId) {
		super(context, loserOfStealth.getId(), sourcePlayerId);
		eventTarget = loserOfStealth;
	}

	@Override
	public Entity getEventTarget() {
		return eventTarget;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.LOSE_STEALTH;
	}

	@Override
	public Entity getVictim() {
		return eventTarget;
	}
}
