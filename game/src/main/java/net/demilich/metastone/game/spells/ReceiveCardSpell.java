package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CatalogueSource;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;

import java.util.Map;

public class ReceiveCardSpell extends Spell {
	@Suspendable
	public static void castSomethingSpell(GameContext context, Player player, SpellDesc spell, Entity source, Card card) {
		context.getLogic().receiveCard(player.getId(), card);
		// card may be null (i.e. try to draw from deck, but already in
		// fatigue)
		if (card == null || card.getZone() == Zones.GRAVEYARD) {
			return;
		}
		if (spell == null) {
			return;
		}
		context.setEventCard(card);
		SpellUtils.castChildSpell(context, player, spell, source, card);
		context.setEventCard(null);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
		CardList cards = CardCatalogue.query(context.getDeckFormat());
		if (cardSource != null) {
			cards = cardSource.getCards(context, player).getCopy();
		}
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		// If a card is being received from a filter, we're creating new cards
		if (cardFilter != null
				|| cardSource != null) {
			CardList result = new CardArrayList();

			if (cardFilter != null) {
				result = cards.filtered(cardFilter.matcher(context, player, source));
			}

			String replacementCard = (String) desc.get(SpellArg.CARD);
			for (int i = 0; i < count; i++) {
				Card card = null;
				if (!result.isEmpty()) {
					card = context.getLogic().removeRandom(result);
				} else if (replacementCard != null) {
					card = context.getCardById(replacementCard);
				}
				if (card != null) {
					ReceiveCardSpell.castSomethingSpell(context, player, subSpell, source, card);
				}
			}
		} else if (desc.containsKey(SpellArg.CARD) || desc.containsKey(SpellArg.CARDS)) {
			// If a card isn't received from a filter, it's coming from a description
			// These cards should always be copies
			for (Card card : SpellUtils.getCards(context, desc)) {
				// Move at most one card from discover or create a card. Handled by get cards.
				for (int i = 0; i < count; i++) {
					card = card.getCopy();
					ReceiveCardSpell.castSomethingSpell(context, player, subSpell, source, card);

				}
			}
		} else if (target instanceof Card && target.getOwner() == player.getId()) {
			// The card is being moved into the hand from somewhere
			ReceiveCardSpell.castSomethingSpell(context, player, subSpell, source, (Card) target);
		}
	}

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReceiveCardSpell.class);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(String cardId) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReceiveCardSpell.class);
		arguments.put(SpellArg.CARD, cardId);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = SpellDesc.build(ReceiveCardSpell.class);
		arguments.put(SpellArg.CARD, target);
		return new SpellDesc(arguments);
	}
}
