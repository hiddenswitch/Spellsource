package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.Zones;

public class CopyCardSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(CopyCardSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int numberOfCardsToCopy = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		if (target != null) {
			Card targetCard = target.getSourceCard();
			for (int i = 0; i < numberOfCardsToCopy; i++) {
				final Card clone = targetCard.getCopy();
				context.getLogic().receiveCard(player.getId(), clone);
				final SpellDesc subSpell = (SpellDesc) desc.get(SpellArg.SPELL);
				SpellUtils.castChildSpell(context, player, subSpell, source, target, clone);
			}
			return;
		}

		CardList sourceCollection = null;
		Zones cardLocation = (Zones) desc.get(SpellArg.CARD_LOCATION);
		if (cardLocation != null) {
			sourceCollection = getCardsFromLocation(context, player, cardLocation);
		}

		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		if (cardSource != null) {
			sourceCollection = cardSource.getCards(context, player);
		}

		if (sourceCollection == null) {
			logger.error("Trying to access a null source collection.");
			return;
		}

		EntityFilter filter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);

		if (filter != null) {
			sourceCollection = sourceCollection.filtered(filter.matcher(context, player, source));
		}

		for (int i = 0; i < numberOfCardsToCopy; i++) {
			if (sourceCollection.isEmpty()) {
				return;
			}
			Card random = context.getLogic().getRandom(sourceCollection);

			peek(random, context, player);

			Card clone = random.getCopy();
			context.getLogic().receiveCard(player.getId(), clone);
		}
	}

	protected void peek(final Card random, GameContext context, Player player) {
	}

	@Deprecated
	private CardList getCardsFromLocation(GameContext context, Player player, Zones cardLocation) {
		if (cardLocation == null) {
			return null;
		}
		// By default, CopyCardSpell actually uses the OPPONENT'S card locations.
		Player targetPlayer = context.getOpponent(player);
		switch (cardLocation) {
			case DECK:
				return targetPlayer.getDeck();
			case HAND:
				return targetPlayer.getHand();
			default:
				logger.error("Trying to copy cards from invalid cardLocation {}", cardLocation);
				return null;
		}
	}

}
