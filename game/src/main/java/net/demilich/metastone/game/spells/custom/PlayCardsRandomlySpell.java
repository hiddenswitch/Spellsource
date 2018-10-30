package net.demilich.metastone.game.spells.custom;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.cards.Attribute;

public final class PlayCardsRandomlySpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Retrieve all the cards
		CardList cards = SpellUtils.getCards(context, player, target, source, desc, Integer.MAX_VALUE);
		// Shuffle
		cards.shuffle(context.getLogic().getRandom());

		// Should not replay itself
		cards.remove(source.getSourceCard());
		cards.removeIf(c -> c.getCardId().equals(source.getSourceCard().getCardId()));
		// TODO: While invoking play cards randomly, should not play a play cards randomly. Use an environment stack.
		player.setAttribute(Attribute.RANDOM_CHOICES);

		// Replay
		for (int i = 0; i < cards.size(); i++) {
			Card card = cards.get(i);
			// TODO: Move the card temporarily to the set aside zone, so that effects apply to it correctly?

			/*
			card.setId(context.getLogic().generateId());
			card.setOwner(player.getId());
			card.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
			*/
			if (SpellUtils.playCardRandomly(context, player, card, source, true, false, false, false, false)) {
				context.getLogic().revealCard(player, card);
			}
			// context.getLogic().removeCard(card);
		}

		player.getAttributes().remove(Attribute.RANDOM_CHOICES);

	}
}
