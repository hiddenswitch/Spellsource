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

public class RecastIfMinionsDestroyedSpell extends RecastWhileSpell {
	private int getDestroyedMinionsCount(GameContext context) {
		return (int) context.getPlayers().stream()
				.flatMap(p -> p.getMinions().stream())
				.filter(Minion::isDestroyed)
				.count();
	}

	@Override
	protected void afterCast(GameContext context, SpellDesc desc) {
		desc.put(SpellArg.SECONDARY_VALUE, getDestroyedMinionsCount(context));
	}

	@Override
	@Suspendable
	protected boolean isFulfilled(GameContext context, Player player, Entity source, Entity target, Condition condition, SpellDesc desc) {
		return (int) desc.getOrDefault(SpellArg.SECONDARY_VALUE, 0) > 0;
	}
}
