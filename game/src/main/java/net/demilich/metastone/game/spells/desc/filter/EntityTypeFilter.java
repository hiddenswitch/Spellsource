package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;

public final class EntityTypeFilter extends EntityFilter {

	public EntityTypeFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		EntityType entityType = (EntityType) getDesc().get(EntityFilterArg.ENTITY_TYPE);
		if (entityType == EntityType.ANY) {
			return true;
		}
		return entity.getEntityType() == entityType;
	}
}
