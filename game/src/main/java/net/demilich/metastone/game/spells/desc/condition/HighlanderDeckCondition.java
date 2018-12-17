package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class HighlanderDeckCondition extends Condition {

	private static final long serialVersionUID = -10295782074838303L;

	public HighlanderDeckCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		return player.getDeck().stream().map(Card::getCardId).distinct().count() == (long) player.getDeck().getCount();
	}
}

