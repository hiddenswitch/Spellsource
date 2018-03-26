package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		final EntityReference targetKey = (EntityReference) desc.get(ConditionArg.TARGET);
		List<Entity> targets;
		if (targetKey == null) {
			targets = Collections.singletonList(target);
		} else {
			targets = context.resolveTarget(player, source, targetKey);
		}

		boolean passes = true;
		for (Entity entity : targets) {
			final int attributeValue = desc.getValue(ConditionArg.VALUE1, context, player, entity, source, 0);
			final int moduloValue = desc.getValue(ConditionArg.VALUE, context, player, entity, source, 2);
			passes &= attributeValue % moduloValue == 0;
		}
		return passes;
	}
}
