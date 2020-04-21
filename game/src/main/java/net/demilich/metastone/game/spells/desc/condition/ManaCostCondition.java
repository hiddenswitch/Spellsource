package net.demilich.metastone.game.spells.desc.condition;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;

/**
 * {@code true} if all the targets' modified mana costs (as per {@link net.demilich.metastone.game.logic.GameLogic#getModifiedManaCost(Player,
 * Card)} satisfies the {@link ConditionArg#OPERATION} with the {@link ConditionArg#VALUE}.
 *
 * @see SpellUtils#evaluateOperation(ComparisonOperation, int, int) for more about comparisons
 */
public class ManaCostCondition extends Condition {

	public ManaCostCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	@Suspendable
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		if (!(target instanceof Card)) {
			target = target.getSourceCard();
			if (target == null) {
				return false;
			}
		}

		var card = (Card) target;
		var value = desc.getValue(ConditionArg.VALUE, context, player, target, source, 0);
		var operation = (ComparisonOperation) desc.getOrDefault(ConditionArg.OPERATION, ComparisonOperation.EQUAL);
		return SpellUtils.evaluateOperation(operation, context.getLogic().getModifiedManaCost(player, card), value);
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsAnd() {
		return true;
	}

	@Override
	protected boolean multipleTargetsEvaluatedAsOr() {
		return false;
	}
}
