package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * @deprecated Use {@link SummonSpell} instead:
 * <pre>
 *   {
 *     "class": "SummonSpell",
 *     "target": The target that you would like to clone.
 *   }
 * </pre>
 * <p>
 * Clones a specified minion.
 */
@Deprecated
public class CloneMinionSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Special case Lord Jaraxxus / Mirror Image interaction
		if (target instanceof Hero) {
			target = context.resolveSingleTarget(context.getSummonReferenceStack().peek());
		}
		Minion template = (Minion) target;
		Minion clone = template.getCopy();
		context.getLogic().summon(player.getId(), clone, null, -1, false);
	}

}
