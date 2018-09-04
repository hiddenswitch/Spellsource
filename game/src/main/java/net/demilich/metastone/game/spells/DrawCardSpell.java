package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draws {@link SpellArg#VALUE} cards from the top of the player's deck.
 * <p>
 * Casts the {@link SpellArg#SPELL} sub-spell with the newly drawn card as the {@link
 * net.demilich.metastone.game.targeting.EntityReference#OUTPUT}.
 * <p>
 * The method used to draw cards from the deck will trigger fatigue damage if the deck is empty. If an effect puts a
 * card into the deck on the (n-1)th sub spell just before attempting the n-th draw, this spell will draw it correctly.
 *
 * @see FromDeckToHandSpell to draw a specific card from the deck.
 */
public class DrawCardSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(DrawCardSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.VALUE);
		int cardCount = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < cardCount; i++) {
			Card card = context.getLogic().drawCard(player.getId(), source);

			if (card == null) {
				continue;
			}

			SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
			SpellUtils.castChildSpell(context, player, subSpell, source, target, card);
		}
	}
}
