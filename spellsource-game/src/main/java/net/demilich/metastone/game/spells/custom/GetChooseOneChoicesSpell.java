package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetChooseOneChoicesSpell extends Spell {

	private Logger logger = LoggerFactory.getLogger(GetChooseOneChoicesSpell.class);

	/**
	 * Spell used to interact with the different options of a Choose One card (spell)
	 * <p>
	 * Will either cast the {@link SpellArg#SPELL} on both options, or {@link SpellArg#SPELL1} on the first option and
	 * {@link SpellArg#SPELL2} on the second option
	 * <p>
	 * Implements Keeper Stalladris
	 *
	 * @param context The game context
	 * @param player  The casting player
	 * @param desc    The collection of {@link SpellArg} keys and values that are interpreted by the implementation of
	 *                this function to actually cause effects in a game
	 * @param source  The entity from which this effect is happening (typically a card or a minion if it's a battlecry).
	 * @param target  The particular target of this invocation of the spell. When a spell hits multiple targets, like an
	 *                AoE damage effect, this method is called once for each target in the list of targets.
	 */

	@Suspendable
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = SpellUtils.getCards(context, player, target, source, desc);

		for (Card card : cards) {
			if (!card.isChooseOne()) {
				logger.warn("Tried to get choices for card that's not a choose one");
				continue;
			}
			SpellDesc spell1;
			SpellDesc spell2;
			if (desc.containsKey(SpellArg.SPELL)) {
				spell1 = desc.getSpell().clone();
				spell2 = desc.getSpell().clone();
			} else {
				spell1 = ((SpellDesc) desc.get(SpellArg.SPELL1)).clone();
				spell2 = ((SpellDesc) desc.get(SpellArg.SPELL2)).clone();
			}
			spell1.put(SpellArg.CARD, card.getChooseOneCardIds()[0]);
			spell2.put(SpellArg.CARD, card.getChooseOneCardIds()[1]);

			SpellUtils.castChildSpell(context, player, spell1, source, target);
			SpellUtils.castChildSpell(context, player, spell2, source, target);
		}
	}
}
