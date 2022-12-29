package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.PutMinionOnBoardFromDeckSpell;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes cards from the top of the caster's deck until a minion is found. Then, summons it.
 * <p>
 * If the deck doesn't have a minion, removes <b>all</b> cards from the deck.
 * <p>
 * Implements Shanga's Spirit Brew.
 */
public final class RemoveTopUntilMinionAndSummonSpell extends Spell {
	private static Logger LOGGER = LoggerFactory.getLogger(RemoveTopUntilMinionAndSummonSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Card card = null;

		int i = 0;
		while (!player.getDeck().isEmpty() && i < GameLogic.MAX_DECK_SIZE) {
			i++;
			if (i == GameLogic.MAX_DECK_SIZE) {
				LOGGER.error("onCast {} {}: Deck never emptying loop check!", context.getGameId(), source);
				throw new RuntimeException("infinite loop");
			}
			card = player.getDeck().peek();
			if (card.getCardType() == CardType.MINION) {
				SpellUtils.castChildSpell(context, player, PutMinionOnBoardFromDeckSpell.create(card.getReference()), source, target, card);
				return;
			}
			context.getLogic().removeCard(card);
		}
	}
}
