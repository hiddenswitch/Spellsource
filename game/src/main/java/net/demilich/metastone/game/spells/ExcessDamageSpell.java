package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
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
		SpellDesc toExcess;
		if (desc.containsKey(SpellArg.SPELL)) {
			 toExcess = (SpellDesc) desc.get(SpellArg.SPELL);
		} else {
			toExcess = desc.clone();
		}
		List<Entity> excessDealtTo = context.resolveTarget(player, source, desc.getSecondaryTarget());
		int damage = context.getLogic().applySpellpower(player, source, desc.getValue(SpellArg.VALUE, context, player, target, source, 6));
		Actor targetActor = (Actor) target;
		int hp = targetActor.getHp();
		int damageToTarget = Math.max(0, Math.min(damage, hp));
		int damageToExcess = Math.min(damage, Math.max(0, damage - hp));
		toTarget.put(SpellArg.VALUE, damageToTarget);
		toTarget.put(SpellArg.IGNORE_SPELL_DAMAGE, true);
		toExcess.put(SpellArg.VALUE, damageToExcess);
		toExcess.put(SpellArg.IGNORE_SPELL_DAMAGE, true);
		if (!desc.getBool(SpellArg.EXCLUSIVE)) {
			super.onCast(context, player, toTarget, source, target);
		}
		for (Entity excessTarget : excessDealtTo) {
			SpellUtils.castChildSpell(context, player, toExcess, source, excessTarget);
		}
	}
}
