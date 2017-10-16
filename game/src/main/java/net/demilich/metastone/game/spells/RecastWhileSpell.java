package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

public class RecastWhileSpell extends Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int limit = 100;
		// case 1 - only one condition
		Condition condition = (Condition) desc.get(SpellArg.CONDITION);
		if (condition != null) {
			SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
			// Cast the spell at least once
			do {
				SpellUtils.castChildSpell(context, player, spell, source, target);
				limit--;
				if (limit < 0) {
					throw new RuntimeException("RecastWhileSpell infinite loop detected.");
				}
			} while (condition.isFulfilled(context, player, source, target));
		} else {
			throw new RuntimeException("RecastWhileSpell specified without a condition.");
		}
	}
}
