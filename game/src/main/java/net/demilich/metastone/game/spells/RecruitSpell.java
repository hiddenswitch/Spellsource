package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.Zones;

public class RecruitSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		Zones cardLocation = (Zones) desc.get(SpellArg.CARD_LOCATION);
		if (cardLocation == null) {
			cardLocation = Zones.DECK;
		}
		int numberToSummon = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < numberToSummon; i++) {
			putRandomMinionFromDeckOnBoard(context, player, cardFilter, cardLocation, source);
		}
	}

	@Suspendable
	private void putRandomMinionFromDeckOnBoard(GameContext context, Player player, EntityFilter cardFilter, Zones cardLocation, Entity source) {
		Card card = null;
		CardList collection = cardLocation == Zones.HAND ? player.getHand() : player.getDeck();
		if (cardFilter == null) {
			card = context.getLogic().getRandom(collection.filtered(f -> f.getCardType() == CardType.MINION));
		} else {
			card = context.getLogic().getRandom(collection.filtered(c -> cardFilter.matches(context, player, c, source)));
		}

		if (card == null) {
			return;
		}

		// we need to remove the card temporarily here, because there are card interactions like Starving Buzzard + Desert Camel
		// which could result in the card being drawn while a minion is summoned
		if (cardLocation == Zones.DECK) {
			player.getDeck().move(card, player.getSetAsideZone());
		}

		boolean summonSuccess = context.getLogic().summon(player.getId(), card.summon(), null, -1, false);

		// re-add the card here if we removed it before
		if (cardLocation == Zones.DECK) {
			player.getSetAsideZone().move(card, player.getDeck());
		}

		if (summonSuccess) {
			context.getLogic().removeCard(card);
		}
	}

}
