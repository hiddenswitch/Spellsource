package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;
import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

import java.util.EnumSet;

/**
 * Base class for damage events.
 */
public abstract class AbstractDamageEvent extends ValueEvent {
	private final EnumSet<DamageTypeEnum> damageType;

	AbstractDamageEvent(GameEvent.EventTypeEnum eventType, GameContext context, Entity victim, Entity source, int damage, EnumSet<DamageTypeEnum> damageType) {
		super(eventType, true, context, context.getPlayer(source.getOwner()), source, victim, damage);
		this.damageType = damageType;
	}

	public EnumSet<DamageTypeEnum> getDamageType() {
		return damageType;
	}
}
