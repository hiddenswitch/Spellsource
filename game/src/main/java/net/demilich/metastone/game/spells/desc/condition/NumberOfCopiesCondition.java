package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProviderArg;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Evaluates to {@code true} when the number of distinct card IDs in the {@link ConditionArg#TARGET} when filtered by
 * {@link ConditionArg#FILTER} passes the {@link ConditionArg#OPERATION} with {@link ConditionArg#VALUE}.
 * <p>
 * For example, a condition that is {@code true} when there are two or less copies of all cards:
 * <pre>
 *   {@code
 *       {
 *         "description": "Decks can't have more than 2 copies of a card",
 *         "class": "NumberOfCopiesCondition",
 *         "target": "FRIENDLY_DECK",
 *         "operation": "LESS_OR_EQUAL",
 *         "value": 2
 *       }
 *   }
 * </pre>
 */
public class NumberOfCopiesCondition extends Condition {

	public NumberOfCopiesCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		List<Entity> targets;
		if (desc.containsKey(ConditionArg.TARGET)) {
			targets = context.resolveTarget(player, source, (EntityReference) desc.get(ConditionArg.TARGET));
		} else {
			targets = Collections.singletonList(target);
		}

		if (desc.containsKey(ConditionArg.FILTER)) {
			targets = targets.stream()
					.filter(((EntityFilter) desc.get(ConditionArg.FILTER)).matcher(context, player, source))
					.collect(Collectors.toList());
		}

		var targetValue = desc.getInt(ConditionArg.VALUE);
		var operation = (ComparisonOperation) desc.get(ConditionArg.OPERATION);

		var ids = targets.stream().map(entity -> entity.getSourceCard().getCardId()).collect(Collectors.toList());

		var result = true;

		for (var id : ids.stream().distinct().collect(Collectors.toList())) {
			var copies = (int) ids.stream().filter(s -> s.equals(id)).count();
			if (!SpellUtils.evaluateOperation(operation, copies, targetValue)) {
				result = false;
			}
		}

		return result;
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}

	@Override
	protected boolean usesFilter() {
		return false;
	}
}
