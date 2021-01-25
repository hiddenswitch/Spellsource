package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Summons a minion from the {@code target} card onto the board in a way that counts it as being played from the hand or
 * deck. Does not trigger battlecries even if played from the hand.
 *
 * @see RecruitSpell for the more common effect of summoning from the hand or deck that doesn't count as playing from
 * 		the hand or deck.
 */
public class PutMinionOnBoardSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = (Card) target;

		if (context.getLogic().summon(player.getId(), card.minion(), source, -1, false)) {
			card.getAttributes().put(Attribute.PLAYED_FROM_HAND_OR_DECK, context.getTurn());
			context.getLogic().removeCard(card);
		}
	}
}
