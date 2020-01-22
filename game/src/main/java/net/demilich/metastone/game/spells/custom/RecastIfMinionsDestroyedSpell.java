package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.RecastWhileSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

/**
 * Recasts the {@link SpellArg#SPELL} if any minions were destroyed.
 */
public final class RecastIfMinionsDestroyedSpell extends RecastWhileSpell {
	private int minionsDestroyed = 0;

	@Override
	protected boolean isNativeStateful() {
		return true;
	}

	private int getDestroyedMinionsCount(GameContext context) {
		return (int) context.getPlayers().stream()
				.flatMap(p -> p.getMinions().stream())
				.filter(Minion::isDestroyed)
				.count();
	}

	@Override
	protected void afterCast(GameContext context, SpellDesc desc) {
		minionsDestroyed = getDestroyedMinionsCount(context);
	}

	@Override
	@Suspendable
	protected boolean isFulfilled(GameContext context, Player player, Entity source, Entity target, Condition condition, SpellDesc desc) {
		return minionsDestroyed > 0;
	}
}
