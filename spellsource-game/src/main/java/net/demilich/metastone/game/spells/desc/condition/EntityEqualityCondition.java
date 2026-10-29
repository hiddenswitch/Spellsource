package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for conditions that check whether two sets of entities are equal.
 */
public abstract class EntityEqualityCondition extends Condition {
	public EntityEqualityCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityReference targetReference,
			EntityReference secondaryTarget) {
		var lhs = lhs(context, player, source, target, targetReference, secondaryTarget);
		var rhs = rhs(context, player, source, target, targetReference, secondaryTarget);
		if (lhs == null || rhs == null) {
			return false;
		}
		if (lhs.isEmpty() || rhs.isEmpty()) {
			return false;
		}
		return compare(lhs, rhs);
	}

	/**
	 * Compares the non-empty {@code lhs} and {@code rhs} entity lists. By default, the lists must be equal.
	 */
	protected boolean compare(List<Entity> lhs, List<Entity> rhs) {
		return Objects.equals(lhs, rhs);
	}

	protected List<Entity> lhs(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityReference targetReference,
			EntityReference secondaryTarget) {
		return List.of(target);
	}

	protected abstract List<Entity> rhs(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			EntityReference targetReference,
			EntityReference secondaryTarget);
}
