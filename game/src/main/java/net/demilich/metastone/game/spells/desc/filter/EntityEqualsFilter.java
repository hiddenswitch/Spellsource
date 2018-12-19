package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

public class EntityEqualsFilter extends EntityFilter {

	public EntityEqualsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		List<Entity> secondaries = context.resolveTarget(player, host, (EntityReference) getDesc().get(EntityFilterArg.SECONDARY_TARGET));
		if (getDesc().containsKey(EntityFilterArg.FILTERS)) {
			EntityFilter[] filters = (EntityFilter[]) getDesc().get(EntityFilterArg.FILTERS);
			for (EntityFilter filter : filters) {
				secondaries.removeIf(entity1 -> !filter.test(context, player, entity1, host));
			}
		}
		if (secondaries.isEmpty()) {
			return false;
		}
		return secondaries.stream().anyMatch(e -> e.getId() == entity.getId() || e.getId() == entity.getSourceCard().getId());
	}
}
