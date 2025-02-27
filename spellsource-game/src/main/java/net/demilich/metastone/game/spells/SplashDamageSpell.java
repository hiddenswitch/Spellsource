package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.AfterPhysicalAttackTrigger;
import net.demilich.metastone.game.spells.trigger.DamageCausedTrigger;
import net.demilich.metastone.game.spells.trigger.DamageReceivedTrigger;
import com.hiddenswitch.spellsource.rpc.Spellsource.DamageTypeMessage.DamageType;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.EnumSet;

/**
 * Like a {@link DamageSpell}, except the {@code source} of the damage is changed to the {@link
 * EntityReference#EVENT_SOURCE}.
 * <p>
 * Appropriate to use with a {@link DamageCausedTrigger}, {@link AfterPhysicalAttackTrigger} or {@link
 * DamageReceivedTrigger} to correctly account for a {@link Attribute#POISONOUS} or {@link Attribute#LIFESTEAL} source.
 * <p>
 * Splash damage from an {@link net.demilich.metastone.game.entities.Actor} deals {@link DamageType#PHYSICAL}.
 */
public final class SplashDamageSpell extends DamageSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityReference eventSource = context.getEventSourceStack().peek();
		if (eventSource != null) {
			source = context.resolveSingleTarget(eventSource);
		}
		super.onCast(context, player, desc, source, target);
	}

	@Override
	protected EnumSet<DamageType> getDamageType(GameContext context, Player player, Entity source) {
		if (GameLogic.isEntityType(source.getEntityType(), EntityType.ACTOR)) {
			return EnumSet.of(DamageType.PHYSICAL, DamageType.SPLASH);
		}
		var damageType = super.getDamageType(context, player, source);
		damageType.add(DamageType.SPLASH);
		return damageType;
	}
}
