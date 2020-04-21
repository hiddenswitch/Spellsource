package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A condition that is fulfilled if all the entities in {@link ConditionArg#TARGET}, when evaluated as the {@code
 * target} to {@link ConditionArg#VALUE1} modulo {@link ConditionArg#VALUE2} equals zero.
 * <p>
 * To test if all the cards in the hand and deck are odd, use this example:
 * <pre>
 *     "condition": {
 *          "class": "AndCondition",
 *          "conditions": [
 *              {
 *                  "class": "AllModuloValueEqualsZeroCondition",
 *                  "target": "FRIENDLY_HAND",
 *                  "value1": {
 *                      "class": "AttributeValueProvider",
 *                      "attribute": "BASE_MANA_COST",
 *                      "offset": 1
 *                  },
 *                  "value2": 2
 *              },
 *              {
 *                  "class": "AllModuloValueEqualsZeroCondition",
 *                  "target": "FRIENDLY_DECK",
 *                  "value1": {
 *                      "class": "AttributeValueProvider",
 *                      "attribute": "BASE_MANA_COST",
 *                      "offset": 1
 *                  },
 *                  "value2": 2
 *              }
 *          ]
 *      }
 * </pre>
 * For even, remove the offset:
 * <pre>
 *     "condition": {
 *          "class": "AndCondition",
 *          "conditions": [
 *              {
 *                  "class": "AllModuloValueEqualsZeroCondition",
 *                  "target": "FRIENDLY_HAND",
 *                  "value1": {
 *                      "class": "AttributeValueProvider",
 *                      "attribute": "BASE_MANA_COST"
 *                  },
 *                  "value2": 2
 *              },
 *              {
 *                  "class": "AllModuloValueEqualsZeroCondition",
 *                  "target": "FRIENDLY_DECK",
 *                  "value1": {
 *                      "class": "AttributeValueProvider",
 *                      "attribute": "BASE_MANA_COST"
 *                  },
 *                  "value2": 2
 *              }
 *          ]
 *      }
 * </pre>
 */
public class AllModuloValueEqualsZeroCondition extends Condition {

	public AllModuloValueEqualsZeroCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		var attributeValue = desc.getValue(ConditionArg.VALUE1, context, player, target, source, 0);
		var moduloValue = desc.getValue(ConditionArg.VALUE2, context, player, target, source, 2);
		return attributeValue % moduloValue == 0;
	}

	@Override
	protected boolean singleTargetOnly() {
		return false;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsOr() {
		return false;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsAnd() {
		return true;
	}

	@Override
	protected boolean usesFilter() {
		return true;
	}
}
