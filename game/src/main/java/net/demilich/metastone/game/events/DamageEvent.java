package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;

import java.util.EnumSet;

/**
 * {@link #getValue()} damage has been dealt to the {@link #getTarget()} by the {@link #getSource()}.
 * <p>
 * {@link #getDamageType()} includes the kind of damage.
 */
public final class DamageEvent extends AbstractDamageEvent {

	public DamageEvent(GameContext context, Entity victim, Entity source, int damage, EnumSet<DamageTypeEnum> damageType) {
		super(EventTypeEnum.DAMAGE, context, victim, source, damage, damageType);
	}
}