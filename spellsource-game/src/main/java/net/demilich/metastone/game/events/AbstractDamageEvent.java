package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.DamageTypeMessage.DamageType;
import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

import java.util.EnumSet;

/**
 * Base class for damage events.
 */
public abstract class AbstractDamageEvent extends ValueEvent {
	private final EnumSet<DamageType> damageType;

	AbstractDamageEvent(GameEventType eventType, GameContext context, Entity victim, Entity source, int damage, EnumSet<DamageType> damageType) {
		super(eventType, true, context, context.getPlayer(source.getOwner()), source, victim, damage);
		this.damageType = damageType;
	}

	public EnumSet<DamageType> getDamageType() {
		return damageType;
	}
}
