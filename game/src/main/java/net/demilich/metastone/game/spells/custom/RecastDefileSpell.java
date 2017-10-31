package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.RecastWhileSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;

public class RecastDefileSpell extends RecastWhileSpell {
	@Override
	protected void beforeCast(GameContext context, SpellDesc desc) {
		desc.put(SpellArg.SECONDARY_VALUE, getLivingMinionCount(context));
	}

	private int getLivingMinionCount(GameContext context) {
		return context.getPlayers().stream()
				.map(p -> p.getMinions().size())
				.reduce(Integer::sum)
				.orElseGet(() -> 0);
	}

	@Override
	@Suspendable
	protected boolean isFulfilled(GameContext context, Player player, Entity source, Entity target, Condition condition, SpellDesc desc) {
		return (int) desc.getOrDefault(SpellArg.SECONDARY_VALUE, 0) > getLivingMinionCount(context);
	}
}
