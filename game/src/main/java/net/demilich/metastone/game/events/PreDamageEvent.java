package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.DamageType;

public final class PreDamageEvent extends GameEvent implements HasValue, HasVictim {

	private final Entity victim;
	private final Entity source;
	private final int amount;
	private final DamageType damageType;

	public PreDamageEvent(GameContext context, Entity victim, Entity source, int amount, DamageType damageType) {
		super(context, victim.getOwner(), source.getOwner());
		this.victim = victim;
		this.source = source;
		this.amount = amount;
		this.damageType = damageType;
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

	public DamageType getDamageType() {
		return damageType;
	}
}
