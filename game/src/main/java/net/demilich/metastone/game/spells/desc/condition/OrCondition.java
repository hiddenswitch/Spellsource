package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * This condition passes if any of the conditions in {@link ConditionArg#CONDITIONS} also pass.
 * <p>
 * If no conditions pass, or no conditions are specified, this condition does not pass (returns {@code false}).
 * <p>
 * This can be used to disable hero powers from being cast, to create a passive hero power. For example:
 *
 * <pre>
 *   {
 *     "type": "HERO_POWER",
 *     "passiveTriggers": [{
 *       "eventTrigger": { ... },
 *       "spell": {
 *         "class": "HeroPowerSpell",
 *         "spell": { ... }
 *       }
 *     }],
 *     "condition": {
 *       "class": "OrCondition"
 *     }
 *   }
 * </pre>
 * <p>
 * This card cannot be "played" but its passive trigger contains its text.
 */
public class OrCondition extends Condition {

	public OrCondition(ConditionDesc desc) {
		super(desc);
	}

	public static Condition create(Condition... conditions) {
		ConditionDesc desc = new ConditionDesc(OrCondition.class);
		desc.put(ConditionArg.CONDITIONS, conditions);
		return desc.create();
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		Condition[] conditions = (Condition[]) desc.get(ConditionArg.CONDITIONS);
		if (conditions == null) {
			return false;
		}

		for (Condition condition : conditions) {
			if (condition.isFulfilled(context, player, source, target)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsAnd() {
		return false;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsOr() {
		return true;
	}
}
