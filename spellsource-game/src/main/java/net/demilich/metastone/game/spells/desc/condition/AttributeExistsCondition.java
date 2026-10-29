package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.cards.Attribute;

/**
 * {@code true} if the player has the specified {@link ConditionArg#ATTRIBUTE} anywhere.
 */
public class AttributeExistsCondition extends Condition {

	public AttributeExistsCondition(ConditionDesc desc) {
		super(desc);
	}

	protected boolean isFulfilled(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			Attribute attribute,
			TargetPlayer targetPlayer) {
		if (targetPlayer != null) {
			return switch (targetPlayer) {
				case OPPONENT -> context.getLogic().hasAttribute(context.getOpponent(player), attribute);
				case SELF -> context.getLogic().hasAttribute(player, attribute);
				case ACTIVE -> context.getLogic().hasAttribute(context.getActivePlayer(), attribute);
				case INACTIVE -> context.getLogic().hasAttribute(context.getOpponent(context.getActivePlayer()), attribute);
				case PLAYER_1 -> context.getLogic().hasAttribute(context.getPlayer1(), attribute);
				case PLAYER_2 -> context.getLogic().hasAttribute(context.getPlayer2(), attribute);
				default -> context.getLogic().attributeExists(attribute);
			};
		}
		return context.getLogic().attributeExists(attribute);
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
