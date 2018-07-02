package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.utils.Attribute;

/**
 * Returns {@code true} if the card's {@link Attribute#INVOKE} effect's cost was met.
 */
public class InvokeCondition extends Condition {

	public InvokeCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return source.getSourceCard().hasAttribute(Attribute.INVOKED);
	}
}
