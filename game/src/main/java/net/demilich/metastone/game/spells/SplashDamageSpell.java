package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.AfterPhysicalAttackTrigger;
import net.demilich.metastone.game.spells.trigger.DamageCausedTrigger;
import net.demilich.metastone.game.spells.trigger.DamageReceivedTrigger;
import com.hiddenswitch.spellsource.client.models.DamageTypeEnum;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Like a {@link DamageSpell}, except the {@code source} of the damage is changed to the {@link
 * EntityReference#EVENT_SOURCE}.
 * <p>
 * Appropriate to use with a {@link DamageCausedTrigger}, {@link AfterPhysicalAttackTrigger} or {@link
 * DamageReceivedTrigger} to correctly account for a {@link Attribute#POISONOUS} or {@link Attribute#LIFESTEAL} source.
 * <p>
 * Splash damage from an {@link net.demilich.metastone.game.entities.Actor} deals {@link DamageTypeEnum#PHYSICAL}.
 */
public final class SplashDamageSpell extends DamageSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityReference eventSource = context.getEventSourceStack().peek();
		if (eventSource != null) {
			source = context.resolveSingleTarget(eventSource);
		}
		super.onCast(context, player, desc, source, target);
	}

	@Override
	protected DamageTypeEnum getDamageType(GameContext context, Player player, Entity source) {
		if (Entity.hasEntityType(source.getEntityType(), EntityType.ACTOR)) {
			return DamageTypeEnum.PHYSICAL;
		}
		return super.getDamageType(context, player, source);
	}
}
