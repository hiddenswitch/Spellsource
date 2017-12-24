package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.targeting.Zones;

public class StealCardSpell extends CopyCardSpell {
	@Override
	protected void peek(Card random, GameContext context, Player player) {
		random.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
		context.getLogic().removeCard(random);
	}
}
