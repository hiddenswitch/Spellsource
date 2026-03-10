package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Race;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Matches an entity if it has the same race as the entity resolved from {@link EntityFilterArg#SECONDARY_TARGET}.
 * <p>
 * For example, to deal damage to all minions that share a race with the battlecry target:
 * <pre>
 *   "filter": {
 *     "class": "RaceFromSecondaryTargetFilter",
 *     "secondaryTarget": "TARGET"
 *   }
 * </pre>
 */
public class RaceFromSecondaryTargetFilter extends RaceFilter {

	public RaceFromSecondaryTargetFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		EntityReference secondaryTarget = (EntityReference) getDesc().get(EntityFilterArg.SECONDARY_TARGET);
		if (secondaryTarget == null) {
			return false;
		}
		Entity comparedTo = context.resolveSingleTarget(player, host, secondaryTarget);
		if (comparedTo == null) {
			return false;
		}
		String race = comparedTo.getRace();
		return Race.hasRace(context, entity, race);
	}
}
