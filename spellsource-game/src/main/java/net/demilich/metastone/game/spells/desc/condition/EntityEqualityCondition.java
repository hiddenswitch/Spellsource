package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * The base condition for entity equality comparison conditions.
 */
public abstract class EntityEqualityCondition extends Condition {

	public EntityEqualityCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected final boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return compare(lhs(context, player, desc, source, target), rhs(context, player, desc, source, target));
	}

	protected List<Entity> lhs(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return source == null ? Collections.emptyList() : Collections.singletonList(source);
	}

	protected List<Entity> rhs(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return target == null ? Collections.emptyList() : Collections.singletonList(target);
	}

	protected boolean compare(@NotNull List<Entity> lhs, @NotNull List<Entity> rhs) {
		return lhs.containsAll(rhs);
	}
}
