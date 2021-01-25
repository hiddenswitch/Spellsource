package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Returns {@code true} if the card's {@link Attribute#INVOKE} effect's cost was met.
 */
public class InvokeCondition extends Condition {

	public InvokeCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		int invoke = source.getAttributeValue(Attribute.INVOKE);
		if (source.hasAttribute(Attribute.BEING_PLAYED) || source instanceof Minion) {
			return player.getMana() >= invoke;
		} else if (source instanceof Card) {
			return player.getMana() - context.getLogic().getModifiedManaCost(player, ((Card) source)) >= invoke;
		}
		return false;
	}

	@Override
	protected boolean singleTargetOnly() {
		return true;
	}
}
