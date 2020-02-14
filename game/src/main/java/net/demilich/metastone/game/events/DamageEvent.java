package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;

public final class DamageEvent extends GameEvent implements HasVictim, HasValue {

	private final Entity victim;
	private final int damage;
	private final DamageTypeEnum damageType;
	private final Entity source;

	public DamageEvent(GameContext context, Entity victim, Entity source, int damage, DamageTypeEnum damageType) {
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
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.DAMAGE;
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

	public DamageTypeEnum getDamageType() {
		return damageType;
	}
}
