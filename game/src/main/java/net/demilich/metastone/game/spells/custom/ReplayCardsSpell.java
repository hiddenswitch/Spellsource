package net.demilich.metastone.game.spells.custom;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.*;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.utils.Attribute;

public class ReplayCardsSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Retrieve all the cards
		CardList cards = SpellUtils.getCards(context, player, target, source, desc, Integer.MAX_VALUE);
		// Shuffle
		cards.shuffle(context.getLogic().getRandom());

		// Should not replay itself
		cards.remove(source.getSourceCard());
		player.setAttribute(Attribute.RANDOM_CHOICES);

		// Replay
		for (int i = 0; i < cards.size(); i++) {
			Card card = cards.get(i);
			if (!SpellUtils.playCardRandomly(context, player, card, source, true, false, false, false, false)) {
				break;
			}
			context.getLogic().revealCard(player, card);
		}

		player.getAttributes().remove(Attribute.RANDOM_CHOICES);

	}
}
