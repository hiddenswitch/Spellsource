package net.demilich.metastone.game.spells.desc.valueprovider;

import java.util.List;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Provide the count of entities resolved using {@link ValueProviderArg#TARGET} matching {@link
 * ValueProviderArg#FILTER}.
 */
public class EntityCountValueProvider extends ValueProvider {

	public EntityCountValueProvider(ValueProviderDesc desc) {
		super(desc);
	}

	protected int provideValue(
			GameContext context,
			Player player,
			Entity target,
			Entity originalHost,
			EntityReference targetReference,
			EntityFilter filter) {
		List<Entity> relevantEntities;
		if (targetReference == null && target != null) {
			relevantEntities = context.resolveTarget(player, originalHost, target.getReference());
		} else {
			Entity source = originalHost;
			if (source == null) {
				source = player;
			}
			relevantEntities = context.resolveTarget(player, source, targetReference);
		}
		if (relevantEntities == null) {
			return 0;
		}
		int count = 0;
		final Entity host = originalHost;
		for (Entity entity : relevantEntities) {
			if (matches(context, player, entity, host, filter)) {
				count++;
			}
		}
		return count;
	}

	protected boolean matches(GameContext context, Player player, Entity entity, Entity host, EntityFilter filter) {
		return filter == null || filter.matches(context, player, entity, host);
	}
}
