package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.DamageType;

public final class DamageEvent extends GameEvent implements HasVictim, HasValue {

	private static final long serialVersionUID = -4145236779696122571L;
	private final Entity victim;
	private final int damage;
	private final DamageType damageType;
	private final Entity source;

	public DamageEvent(GameContext context, Entity victim, Entity source, int damage, DamageType damageType) {
		super(context, victim.getOwner(), source.getOwner());
		this.victim = victim;
		this.source = source;
		this.damage = damage;
		this.damageType = damageType;
	}

	public int getDamage() {
		return damage;
	}

	@Override
	public Entity getEventSource() {
		return getSource();
	}

	@Override
	public Entity getEventTarget() {
		return getVictim();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.DAMAGE;
	}

	@Override
	public Entity getSource() {
		return source;
	}

	@Override
	public Entity getVictim() {
		return victim;
	}

	@Override
	public int getValue() {
		return getDamage();
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	public DamageType getDamageType() {
		return damageType;
	}
}
