package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * Recasts the {@link SpellArg#SPELL} if any minions were destroyed.
 */
public final class RecastIfMinionsDestroyedSpell extends AbstractRepeatMinionsDestroyedSpell {

	@Override
	protected boolean isFulfilled(GameContext context, Player player, Entity source, Entity target, Condition condition, SpellDesc desc) {
		return minionsDestroyed > 0;
	}
}
