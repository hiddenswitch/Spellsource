package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

public class OverloadedManaCrystalsCondition extends Condition {
	private static final long serialVersionUID = 5804400860108090260L;

	public OverloadedManaCrystalsCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return player.getLockedMana() > 0;
	}
}
