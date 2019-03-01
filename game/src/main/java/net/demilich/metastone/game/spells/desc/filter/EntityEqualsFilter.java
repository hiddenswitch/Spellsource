package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

/**
 * Matches an entity if the entity is in the list of entities returned by resolving the {@link
 * EntityFilterArg#SECONDARY_TARGET}.
 * <p>
 * For example, to implement the text, "Choose a minion. It attacks a random friendly minion."
 * <pre>
 *   "spell": {
 *     "class": "DuelSpell",
 *     "target": "FRIENDLY_MINIONS",
 *     "filter": {
 *       "class": "EntityEqualsFilter",
 *       "secondaryTarget": "TARGET",
 *       "invert": true
 *     },
 *     "randomTarget": true,
 *     "secondaryTarget": "TARGET"
 *   }
 * </pre>
 * Observe that the filter takes a {@link EntityFilterArg#SECONDARY_TARGET} of {@link EntityReference#TARGET}, i.e., the
 * actual minion chosen. We want to choose a random target that does <b>not</b> include the minion chosen.
 */
public class EntityEqualsFilter extends EntityFilter {

	public EntityEqualsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	public static EntityFilter create(EntityReference equalTo) {
		EntityFilterDesc desc = new EntityFilterDesc(EntityEqualsFilter.class);
		desc.put(EntityFilterArg.SECONDARY_TARGET, equalTo);
		return desc.create();
	}

	public static EntityFilter create(EntityReference equalTo, boolean invert) {
		EntityFilterDesc desc = new EntityFilterDesc(EntityEqualsFilter.class);
		desc.put(EntityFilterArg.SECONDARY_TARGET, equalTo);
		desc.put(EntityFilterArg.INVERT, invert);
		return desc.create();
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
