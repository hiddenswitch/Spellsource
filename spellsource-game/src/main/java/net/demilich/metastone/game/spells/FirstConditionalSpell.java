package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * Like {@link ConditionalSpell}, except executes the first matching condition.
 */
public final class FirstConditionalSpell extends ConditionalSpell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Condition[] conditions = (Condition[]) desc.get(SpellArg.CONDITIONS);
		SpellDesc[] spells = (SpellDesc[]) desc.get(SpellArg.SPELLS);
		for (int i = 0; i < conditions.length; i++) {
			if (conditions[i].isFulfilled(context, player, source, target)) {
				SpellUtils.castChildSpell(context, player, spells[i], source, target);
				return;
			}
		}
	}
}
