package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

/**
 * {@code true} if the {@code player} or {@link ConditionArg#TARGET_PLAYER} has a {@link
 * net.demilich.metastone.game.spells.trigger.secrets.Secret}.
 */
public class ControlsSecretCondition extends Condition {

	public ControlsSecretCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return !player.getSecrets().isEmpty();
	}

	@Override
	protected boolean targetConditionArgOverridesSuppliedTarget() {
		return false;
	}
}
