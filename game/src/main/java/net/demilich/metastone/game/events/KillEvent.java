package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public final class KillEvent extends GameEvent implements HasVictim {

	private final Entity victim;

	public KillEvent(GameContext context, Entity victim) {
		super(context, victim.getOwner(), -1);
		this.victim = victim;
	}

	@Override
	public Entity getEventSource() {
		return getVictim();
	}

	@Override
	public Entity getEventTarget() {
		return getVictim();
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.KILL;
	}

	@Override
	public Entity getVictim() {
		return victim;
	}

}
