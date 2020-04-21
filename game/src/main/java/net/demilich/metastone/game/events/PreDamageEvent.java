package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;
import com.hiddenswitch.spellsource.client.models.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

import java.util.EnumSet;

/**
 * Just before damage is dealt.
 * <p>
 * Gives a change to modify damage using {@link net.demilich.metastone.game.spells.ModifyDamageSpell}.
 */
public final class PreDamageEvent extends AbstractDamageEvent {
	public PreDamageEvent(GameContext context, Entity victim, Entity source, int damage, EnumSet<DamageTypeEnum> damageType) {
		super(EventTypeEnum.PRE_DAMAGE, context, victim, source, damage, damageType);
	}
}
