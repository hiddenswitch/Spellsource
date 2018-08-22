package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

/**
 * Deals {@link SpellArg#VALUE} damage to the {@code target} and any excess to the {@link SpellArg#SECONDARY_TARGET}.
 * <p>
 * If {@link SpellArg#EXCLUSIVE} is {@code true}, only deals excess damage.
 */
public final class ExcessDamageSpell extends DamageSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc toTarget = desc.clone();
		SpellDesc toExcess = desc.clone();
		List<Entity> excessDealtTo = context.resolveTarget(player, source, desc.getSecondaryTarget());
		int damage = desc.getValue(SpellArg.VALUE, context, player, target, source, 6);
		Actor targetActor = (Actor) target;
		int hp = targetActor.getHp();
		int damageToTarget = Math.max(0, Math.min(damage, hp));
		int damageToExcess = Math.min(damage, Math.max(0, damage - hp));
		toTarget.put(SpellArg.VALUE, damageToTarget);
		toExcess.put(SpellArg.VALUE, damageToExcess);
		if (!desc.getBool(SpellArg.EXCLUSIVE)) {
			super.onCast(context, player, toTarget, source, target);
		}
		for (Entity excessTarget : excessDealtTo) {
			super.onCast(context, player, toExcess, source, excessTarget);
		}
	}
}
