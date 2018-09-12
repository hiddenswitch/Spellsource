package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
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
 * Reveals a card from a filter, {@link SpellArg#CARD} or the target if neither is specified.
 */
public class RevealCardSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(RevealCardSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList filteredCards = SpellUtils.getCards(context, player, target, source, desc, 1);
		if (filteredCards.isEmpty()) {
			logger.warn("onCast {} {}: Tried to reveal a card but none was specified.", context.getGameId(), source);
			return;
		}

		Card cardToReveal = filteredCards.get(0);
		context.getLogic().revealCard(player, cardToReveal);
		SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		if (subSpell == null) {
			return;
		}
		SpellUtils.castChildSpell(context, player, subSpell, source, target, cardToReveal);
	}
}
