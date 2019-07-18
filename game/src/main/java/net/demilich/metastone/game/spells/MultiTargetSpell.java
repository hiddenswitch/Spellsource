package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.List;
import java.util.Map;

/**
 * Casts {@link SpellArg#SPELL} on a random target from the list of targets in {@link SpellArg#TARGET} at most {@link
 * SpellArg#VALUE} times or until random targets are exhausted, whichever comes first.
 * <p>
 * Valid targets are those that are not mortally wounded. However, deathrattles are evaluated at the end of the sequence
 * as normal.
 */
public class MultiTargetSpell extends Spell {

	public static SpellDesc create(int targets) {
		Map<SpellArg, Object> arguments = new SpellDesc(MultiTargetSpell.class);
		arguments.put(SpellArg.VALUE, targets);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		int number = desc.getValue(SpellArg.VALUE, context, player, null, source, 1);
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		EntityFilter filter = (EntityFilter) desc.get(SpellArg.FILTER);
		List<Entity> validTargets = SpellUtils.getValidRandomTargets(SpellUtils.getValidTargets(context, player, targets, filter, source));
		for (int i = 0; i < number; i++) {
			if (validTargets.isEmpty()) {
				return;
			}
			Entity randomTarget = context.getLogic().getRandom(validTargets);
			validTargets.remove(randomTarget);
			SpellUtils.castChildSpell(context, player, spell, source, randomTarget);
		}
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
	}
}

