package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.cards.Attribute;

public class ComboCondition extends Condition {

	public static final ComboCondition INSTANCE = new ComboCondition(new ConditionDesc(ComboCondition.class));
	public ComboCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return player.getAttributeValue(Attribute.COMBO) > 0;
	}

}
