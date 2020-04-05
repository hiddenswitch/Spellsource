package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.RecastWhileSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * A base class for spells that have to repeat recasts based on whether or not minions have been destroyed.
 */
public class AbstractRepeatMinionsDestroyedSpell extends RecastWhileSpell {
	protected int minionsDestroyed = 0;

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
}
