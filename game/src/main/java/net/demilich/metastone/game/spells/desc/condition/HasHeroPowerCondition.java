package net.demilich.metastone.game.spells.desc.condition;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class HasHeroPowerCondition extends Condition {

	public HasHeroPowerCondition(ConditionDesc desc) {
		super(desc);
	}

	@Override
	protected boolean isFulfilled(GameContext context, Player player, ConditionDesc desc, Entity source, Entity target) {
		Card heroPower = player.getHero().getHeroPower();
		if (heroPower.getCardId() == null) {
			return false;
		}
		String cardName = (String) desc.get(ConditionArg.CARD);
		return heroPower.getCardId().equalsIgnoreCase(cardName);
	}

}
