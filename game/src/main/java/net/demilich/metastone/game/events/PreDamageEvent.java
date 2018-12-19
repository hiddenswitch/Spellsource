package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public final class PreDamageEvent extends GameEvent implements HasValue, HasVictim {

	private final Entity victim;
	private final Entity source;
	private final int amount;

	public PreDamageEvent(GameContext context, Entity victim, Entity source, int amount) {
		super(context, victim.getOwner(), source.getOwner());
		this.victim = victim;
		this.source = source;
		this.amount = amount;
	}

	@Override
	public Entity getEventTarget() {
		return getVictim();
	}

	@Override
	public Entity getEventSource() {
		return getSource();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.PRE_DAMAGE;
	}

	public Entity getSource() {
		return source;
	}

	public Entity getVictim() {
		return victim;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	@Override
	public int getValue() {
		return amount;
	}
}
