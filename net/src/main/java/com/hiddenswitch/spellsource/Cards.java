package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.CardRecord;
import com.hiddenswitch.spellsource.models.*;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * The cards service. This manages the base card definitions.
 */
public interface Cards {
	Random RANDOM = new Random();

	/**
	 * Queries the card catalogue with the specified parameters and returns the corresponding card records. Useful for
	 * filtering through the card catalogue.
	 *
	 * @param request A variety of different filtering parameters for querying the card catalogue.
	 * @return Records which match the filters in the request.
	 */
	@Suspendable
	static QueryCardsResponse query(QueryCardsRequest request) {
		// For now, just use the CardCatalogue
		CardCatalogue.loadCardsFromPackage();

		final QueryCardsResponse response;

		if (request.isBatchRequest()) {
			response = new QueryCardsResponse()
					.withRecords(new ArrayList<>());

			for (QueryCardsRequest request1 : request.getRequests()) {
				response.append(query(request1));
			}
		} else if (request.getCardIds() != null) {
			response = new QueryCardsResponse()
					.withRecords(request.getCardIds().stream().map(CardCatalogue.getRecords()::get).collect(toList()));
		} else {
			final EnumSet<CardSet> sets = EnumSet.noneOf(CardSet.class);
			sets.addAll(Arrays.asList(request.getSets()));

			List<CardCatalogueRecord> results = CardCatalogue.getRecords().values().stream().filter(r -> {
				boolean passes = true;

				final CardDesc desc = r.getDesc();

				passes &= desc.isCollectible();
				passes &= sets.contains(desc.getSet());

				if (request.getRarity() != null) {
					passes &= desc.getRarity().isRarity(request.getRarity());
				}

				return passes;
			}).collect(toList());

			int count = results.size();

			if (request.isRandomCountRequest()) {
				Collections.shuffle(results, getRandom());
				count = Math.min(request.getRandomCount(), count);
			}

			List<CardCatalogueRecord> cards = results;
			if (count != 0) {
				cards = new ArrayList<>(cards.subList(0, count));
			}

			response = new QueryCardsResponse()
					.withRecords(cards);
		}
		return response;
	}

	static Random getRandom() {
		return RANDOM;
	}

	/**
	 * Retrieves a freshly computed list containing all the collectible cards as client entities. Represents the current
	 * master collection of the game.
	 *
	 * @return The cards
	 */
	static List<CardRecord> getCards() {
		GameContext workingContext = GameContext.uninitialized();
		return CardCatalogue.getRecords().values()
				.stream()
				.map(CardCatalogueRecord::getDesc)
				.filter(cd -> cd.isCollectible()
						&& DeckFormat.CUSTOM.isInFormat(cd.getSet()))
				.map(CardDesc::create)
				.map(card -> Games.getEntity(workingContext, card, 0))
				.map(entity -> new CardRecord().entity(entity))
				.collect(toList());
	}
}
