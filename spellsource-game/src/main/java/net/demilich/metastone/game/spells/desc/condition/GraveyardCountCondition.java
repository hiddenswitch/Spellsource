package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * Counts the entities in the graveyard of {@code player} optionally matching {@link ConditionArg#FILTER}.
 */
public class GraveyardCountCondition extends CountCondition {

	public GraveyardCountCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected int getCountForPlayer(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityFilter filter,
			EntityFilter cardFilter) {
		return (int) player.getGraveyard()
				.stream()
				.filter(e -> filter == null || filter.matches(context, player, e, source))
				.count();
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
