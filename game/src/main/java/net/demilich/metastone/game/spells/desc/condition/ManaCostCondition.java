package net.demilich.metastone.game.spells.desc.condition;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.filter.ComparisonOperation;

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

		Card card = (Card) target;
		int value = desc.getValue(ConditionArg.VALUE, context, player, target, source, 0);
		ComparisonOperation operation = (ComparisonOperation) desc.getOrDefault(ConditionArg.OPERATION, ComparisonOperation.EQUAL);
		return SpellUtils.evaluateOperation(operation, context.getLogic().getModifiedManaCost(player, card), value);
	}

}
