package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reveals a card from a filter, {@link SpellArg#CARD} or the {@code target} if neither is specified.
 * <p>
 * Both players will see the card in their interfaces.
 * <p>
 * If a subspell is specified in {@link SpellArg#SPELL}, it is cast with the revealed card as the {@link
 * net.demilich.metastone.game.targeting.EntityReference#OUTPUT}.
 * <p>
 * For <b>example,</b> to reveal the top card on the opponent's deck and get a copy of it:
 * <pre>
 *   {
 *     "class": "RevealCardSpell",
 *     "target": "ENEMY_TOP_CARD",
 *     "spell": {
 *       "class": "CopyCardSpell",
 *       "target": "OUTPUT"
 *     }
 *   }
 * </pre>
 */
public class RevealCardSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(RevealCardSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList filteredCards = SpellUtils.getCards(context, player, target, source, desc, 30);
		if (filteredCards.isEmpty()) {
			logger.warn("onCast {} {}: Tried to reveal a card but none was specified.", context.getGameId(), source);
			return;
		}
		filteredCards.shuffle(context.getLogic().getRandom());
		Card cardToReveal = filteredCards.get(0);
		context.getLogic().revealCard(player, cardToReveal);
		SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		if (subSpell == null) {
			return;
		}
		SpellUtils.castChildSpell(context, player, subSpell, source, target, cardToReveal);
	}
}
