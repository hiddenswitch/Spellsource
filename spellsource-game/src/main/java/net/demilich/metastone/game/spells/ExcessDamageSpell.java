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
 * <p>
 * If a {@link SpellArg#SPELL} is provided, cast it with a {@link SpellArg#VALUE} equal to the excess damage dealt
 * (ignoring spell damage). Leave {@link SpellArg#SECONDARY_TARGET} undefined unless the spell should be cast on every
 * secondary target.
 */
public final class ExcessDamageSpell extends DamageSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc toExcess;
		if (desc.getSpell() != null) {
			toExcess = desc.getSpell().clone();
		} else {
			toExcess = DamageSpell.create();
		}
		List<Entity> excessDealtTo;
		excessDealtTo = context.resolveTarget(player, source, desc.getSecondaryTarget());
		int damage = context.getLogic().applySpellpower(player, source, desc.getValue(SpellArg.VALUE, context, player, target, source, 6));
		Actor targetActor = (Actor) target;
		int hp = targetActor.getHp();
		int damageToTarget = Math.max(0, Math.min(damage, hp));
		int damageToExcess = Math.min(damage, Math.max(0, damage - hp));

		SpellDesc toTarget = DamageSpell.create(target.getReference(), damageToTarget);
		toTarget.put(SpellArg.IGNORE_SPELL_DAMAGE, true);

		toExcess.put(SpellArg.VALUE, damageToExcess);
		toExcess.put(SpellArg.IGNORE_SPELL_DAMAGE, true);

		if (!desc.getBool(SpellArg.EXCLUSIVE)) {
			toTarget.remove(SpellArg.SECONDARY_TARGET);
			super.onCast(context, player, toTarget, source, target);
		}

		if (excessDealtTo != null) {
			for (Entity excessTarget : excessDealtTo) {
				SpellUtils.castChildSpell(context, player, toExcess, source, excessTarget);
			}
		} else {
			SpellUtils.castChildSpell(context, player, toExcess, source, null);
		}
	}
}
