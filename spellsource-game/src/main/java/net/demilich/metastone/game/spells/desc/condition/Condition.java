package net.demilich.metastone.game.spells.desc.condition;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.desc.Desc;
import net.demilich.metastone.game.cards.desc.HasDesc;
import net.demilich.metastone.game.cards.desc.HasDescSerializer;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A condition is used for true or false comparisons in the {@link net.demilich.metastone.game.cards.desc.CardDesc} card
 * JSON.
 * <p>
 * The core function is {@link #isFulfilled(GameContext, Player, Entity, Entity)}.
 */
@JsonSerialize(using = HasDescSerializer.class)
public abstract class Condition implements Serializable, HasDesc<ConditionDesc> {

	private ConditionDesc desc;

	public Condition(ConditionDesc desc) {
		this.desc = desc;
	}

	@Suspendable
	protected abstract boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target);

	/**
	 * Is the condition fulfilled given the specified {@code source} and {@code target}?
	 * <p>
	 * Uses the {@link #getDesc()} {@code desc} this was constructed with for many parameters.
	 * <p>
	 * {@code target} is context-sensitive. When used in {@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#QUEUE_CONDITION}
	 * or {@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#FIRE_CONDITION}, {@code source} is the
	 * {@link GameEvent#getSource()} and {@code target} is the {@link GameEvent#getTarget()}. The trigger's host
	 * is typically accessed using {@link EntityReference#TRIGGER_HOST}.
	 *
	 * @param context
	 * @param player
	 * @param source
	 * @param target
	 * @return
	 */
	@Suspendable
	public final boolean isFulfilled(GameContext context, Player player, Entity source, Entity target) {
		var invert = desc.getBool(ConditionArg.INVERT);
		var targetPlayer = (TargetPlayer) desc.getOrDefault(ConditionArg.TARGET_PLAYER, TargetPlayer.SELF);
		var targets = new ArrayList<Entity>();
		if (targetConditionArgOverridesSuppliedTarget() && desc.containsKey(ConditionArg.TARGET)) {
			var targetKey = (EntityReference) desc.get(ConditionArg.TARGET);
			if (source == null) {
				if (targetKey.isTargetGroup()) {
					throw new UnsupportedOperationException(String.format("isFulfilled %s: %s requested group key %s with null source", context.getGameId(), desc, targetKey));
				}
				targets.add(context.resolveSingleTarget(targetKey));
			} else {
				targets.addAll(context.resolveTarget(player, source, targetKey));
			}
		} else {
			targets.add(target);
		}

		if (targetConditionArgOverridesSuppliedTarget() && usesFilter() && desc.containsKey(ConditionArg.FILTER)) {
			var filter = (EntityFilter) desc.get(ConditionArg.FILTER);
			var iterator = targets.listIterator();
			while (iterator.hasNext()) {
				var innerTarget = iterator.next();
				if (innerTarget == null) {
					continue;
				}
				if (filter != null && !filter.matches(context, player, innerTarget, source)) {
					iterator.remove();
				}
			}
		}

		if ((multipleTargetsEvaluatedAsAnd() && multipleTargetsEvaluatedAsOr()) || (!multipleTargetsEvaluatedAsAnd() && !multipleTargetsEvaluatedAsOr()) && targets.size() > 1) {
			throw new UnsupportedOperationException(String.format("isFulfilled %s: %s requested incompatible multi-target evaluation", context.getGameId(), desc));
		}

		if (singleTargetOnly() && targets.size() > 1) {
			throw new UnsupportedOperationException(String.format("isFulfilled %s: %s requested incompatible single target for multi target spec", context.getGameId(), desc));
		}

		var res = multipleTargetsEvaluatedAsAnd() && (!requiresAtLeastOneTarget() || !targets.isEmpty());

		for (var innerTarget : targets) {
			if (multipleTargetsEvaluatedAsAnd()) {
				res &= isFulfilledForTarget(context, player, source, innerTarget, targetPlayer);
			} else if (multipleTargetsEvaluatedAsOr()) {
				res |= isFulfilledForTarget(context, player, source, innerTarget, targetPlayer);
			} else {
				res = isFulfilledForTarget(context, player, source, innerTarget, targetPlayer);
			}
		}
		return res != invert;
	}

	protected boolean isFulfilledForTarget(GameContext context, Player player, Entity source, Entity target, TargetPlayer targetPlayer) {
		switch (targetPlayer) {
			default:
			case SELF:
				return isFulfilled(context, player, desc, source, target);
			case ACTIVE:
				return isFulfilled(context, context.getActivePlayer(), desc, source, target);
			case PLAYER_1:
				return isFulfilled(context, context.getPlayer1(), desc, source, target);
			case PLAYER_2:
				return isFulfilled(context, context.getPlayer2(), desc, source, target);
			case OWNER:
				return isFulfilled(context, context.getPlayer(target.getOwner()), desc, source, target);
			case OPPONENT:
				return isFulfilled(context, context.getOpponent(player), desc, source, target);
			case INACTIVE:
				return isFulfilled(context, context.getPlayer(context.getNonActivePlayerId()), desc, source, target);
			case BOTH:
				return (isFulfilled(context, player, desc, source, target) && isFulfilled(context, context.getOpponent(player), desc, source, target));
			case EITHER:
				return (isFulfilled(context, player, desc, source, target) || isFulfilled(context, context.getOpponent(player), desc, source, target));
		}
	}

	@Override
	public void setDesc(Desc<?, ?> desc) {
		this.desc = (ConditionDesc) desc;
	}

	@Override
	public ConditionDesc getDesc() {
		return desc;
	}

	/**
	 * When {@code true}, {@link ConditionArg#TARGET} is evaluated instead of being interpreted by the subclass, replacing
	 * {@code target} if it is specified.
	 * <p>
	 * When {@code false}, only {@code target} is passed.
	 *
	 * @return
	 */
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return true;
	}

	/**
	 * When {@link #targetConditionArgOverridesSuppliedTarget()} is {@code true}, the result of the condition on each of
	 * the resolved targets is {@code and}-ed if this is {@code true}.
	 * <p>
	 * Should not be {@code true} when {@link #multipleTargetsEvaluatedAsOr()} is also {@code true}.
	 *
	 * @return
	 */
	protected boolean multipleTargetsEvaluatedAsAnd() {
		return true;
	}

	/**
	 * When {@link #targetConditionArgOverridesSuppliedTarget()} is {@code true}, the result of the condition on each of
	 * the resolved targets is {@code or}-ed if this is {@code true}.
	 * <p>
	 * Should not be {@code true} when {@link #multipleTargetsEvaluatedAsAnd()} is also {@code true}.
	 *
	 * @return
	 */
	protected boolean multipleTargetsEvaluatedAsOr() {
		return false;
	}

	/**
	 * Expects a single target only. Used in conjunction with {@link #targetConditionArgOverridesSuppliedTarget()}.
	 *
	 * @return
	 */
	protected boolean singleTargetOnly() {
		return false;
	}

	/**
	 * When {@code true} and {@link #multipleTargetsEvaluatedAsAnd()}, there must be at least one target in the resolved
	 * targets.
	 *
	 * @return
	 */
	protected boolean requiresAtLeastOneTarget() {
		return false;
	}

	/**
	 * Filters the {@code target} or {@link ConditionArg#TARGET} with the {@link ConditionArg#FILTER}.
	 *
	 * @return
	 */
	protected boolean usesFilter() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Condition)) return false;
		Condition condition = (Condition) o;
		return desc.equals(condition.desc);
	}

	@Override
	public int hashCode() {
		return Objects.hash(desc);
	}
}

