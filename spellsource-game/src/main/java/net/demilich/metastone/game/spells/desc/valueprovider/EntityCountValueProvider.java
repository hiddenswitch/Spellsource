package net.demilich.metastone.game.spells.desc.valueprovider;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

/**
 * Counts the number of entities specified in the {@link ValueProviderArg#TARGET} filtered by {@link
 * ValueProviderArg#FILTER}.
 */
public class EntityCountValueProvider extends ValueProvider {

	public EntityCountValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	@Override
	protected int provideValue(GameContext context, Player player, Entity target, Entity originalHost) {
		EntityReference descTarget = getDesc().getTarget();
		Entity host = originalHost;
		if (originalHost == null && descTarget != null && !descTarget.isTargetGroup()) {
			host = context.resolveSingleTarget(descTarget);
		}
		List<Entity> relevantEntities = getRelevantEntities(context, player, originalHost, descTarget != null ? descTarget : (target != null ? target.getReference() : null));
		int count = 0;
		for (Entity entity : relevantEntities) {
			if (matches(context, player, entity, host)) {
				count++;
			}
		}
		return count;
	}

	private List<Entity> getRelevantEntities(GameContext context, Player player, Entity host, EntityReference target) {
		return context.resolveTarget(player, host, target);
	}

	protected boolean matches(GameContext context, Player player, Entity entity, Entity host) {
		EntityFilter filter = (EntityFilter) getDesc().get(ValueProviderArg.FILTER);
		return filter == null || filter.matches(context, player, entity, host);
	}
}

