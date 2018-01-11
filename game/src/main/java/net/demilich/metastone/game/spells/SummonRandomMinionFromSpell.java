package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

@Deprecated
public class SummonRandomMinionFromSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card fromCard = SpellUtils.getCard(context, desc);
		CardList allMinions = CardCatalogue.query(context.getDeckFormat(), CardType.MINION);
		CardList relevantMinions = new CardArrayList();
		for (Card card : allMinions) {
			if (context.getLogic().getModifiedManaCost(player, fromCard) == card.getBaseManaCost()) {
				relevantMinions.addCard(card);
			}
		}
		
		int boardPosition = SpellUtils.getBoardPosition(context, player, desc, source);
		MinionCard minionCard = (MinionCard) context.getLogic().getRandom(relevantMinions);
		context.getLogic().summon(player.getId(), minionCard.summon(), null, boardPosition, false);
	}

}
