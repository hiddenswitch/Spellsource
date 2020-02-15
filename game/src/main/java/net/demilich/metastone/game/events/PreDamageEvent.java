package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public final class PreDamageEvent extends GameEvent implements HasValue, HasVictim {

	private final Entity victim;
	private final Entity source;
	private final int amount;
	private final DamageTypeEnum damageType;

	public PreDamageEvent(GameContext context, Entity victim, Entity source, int amount, DamageTypeEnum damageType) {
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
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.PRE_DAMAGE;
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

	public DamageTypeEnum getDamageType() {
		return damageType;
	}
}
