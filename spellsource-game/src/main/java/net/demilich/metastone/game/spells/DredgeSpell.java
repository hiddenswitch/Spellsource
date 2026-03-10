package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Implements the <b>Dredge</b> keyword: look at the bottom 3 cards of your deck and choose one to put on top.
 * <p>
 * If a {@link SpellArg#SPELL} is specified, it is cast on the chosen card after it is moved to the top.
 * <p>
 * For <b>example</b>, a simple Dredge:
 * <pre>
 *   {
 *     "class": "DredgeSpell"
 *   }
 * </pre>
 */
public class DredgeSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		var targetPlayerEnum = desc.getTargetPlayer();
		var resolvedPlayer = targetPlayerEnum != null
				? SpellUtils.determineCastingPlayer(context, player, source, targetPlayerEnum).getCastingPlayer()
				: player;
		var deck = resolvedPlayer.getDeck();
		if (deck.isEmpty()) {
			return;
		}

		int count = Math.min(3, deck.size());
		CardList bottomCards = new CardArrayList(deck.subList(0, count));

		if (bottomCards.isEmpty()) {
			return;
		}

		// Use discover mechanic to present bottom cards as choices
		SpellDesc putOnTopSpell = new SpellDesc(PutDeckTopSpell.class);
		SpellDesc discoverDesc = new SpellDesc(DiscoverSpell.class);
		discoverDesc.put(SpellArg.SPELL, putOnTopSpell);
		discoverDesc.put(SpellArg.EXCLUSIVE, true);
		discoverDesc.put(SpellArg.HOW_MANY, count);

		DiscoverAction chosen = SpellUtils.discoverCard(context, player, source, discoverDesc, bottomCards);
		if (chosen == null || chosen.getCard() == null) {
			return;
		}

		Card chosenCard = chosen.getCard();

		// Find the actual card in the deck (discover works with copies)
		Card actualCard = null;
		for (int i = 0; i < Math.min(count, deck.size()); i++) {
			if (deck.get(i).getCardId().equals(chosenCard.getCardId())) {
				actualCard = deck.get(i);
				break;
			}
		}

		if (actualCard == null) {
			return;
		}

		// Move from bottom to top of deck
		deck.remove(actualCard);
		deck.addLast(actualCard);

		// Cast sub-spell on the chosen card if specified
		for (SpellDesc subSpell : desc.subSpells(0)) {
			SpellUtils.castChildSpell(context, player, subSpell, source, actualCard);
		}
	}
}
