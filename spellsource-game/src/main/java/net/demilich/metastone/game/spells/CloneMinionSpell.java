package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Clones a specified minion. Use  {@link SummonSpell} instead.
 * <pre>
 * {
 *   "class": "SummonSpell",
 *   "target": The target that you would like to clone.
 * }
 * </pre>
 *
 * @deprecated Use SummonSpell instead.
 */
@Deprecated
public class CloneMinionSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Special case Lord Jaraxxus / Mirror Image interaction
		if (target instanceof Hero) {
			target = context.resolveSingleTarget(context.getSummonReferenceStack().peek());
			target.getAttributes().remove(Attribute.DESTROYED);
		}
		Minion template = (Minion) target;
		Minion clone = template.getCopy();
		if (context.getLogic().summon(player.getId(), clone, source, -1, false)) {
			clone.setAttribute(Attribute.SUMMONING_SICKNESS);
		}
	}

}
