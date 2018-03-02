package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.CardRecord;
import com.hiddenswitch.spellsource.client.models.Entity;
import com.hiddenswitch.spellsource.models.*;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * The cards service. This manages the base card definitions.
 */
public interface Cards {
	/**
	 * Gets information about a specific card.
	 *
	 * @param request The ID of the card to query.
	 * @return A card catalogue record corresponding to the requested ID.
	 */
	@Suspendable
	GetCardResponse getCard(GetCardRequest request);

	/**
	 * Queries the card catalogue with the specified parameters and returns the corresponding card records. Useful for
	 * filtering through the card catalogue.
	 *
	 * @param request A variety of different filtering parameters for querying the card catalogue.
	 * @return Records which match the filters in the request.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	QueryCardsResponse queryCards(QueryCardsRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Inserts a card catalogue record.
	 *
	 * @param request A card catalogue record to insert.
	 * @return Information about the insertion (typically empty).
	 */
	@Suspendable
	InsertCardResponse insertCard(InsertCardRequest request);

	/**
	 * Updates a card catalogue record using its card ID.
	 *
	 * @param request The card catalogue record to update.
	 * @return Information about the update (typically empty).
	 */
	@Suspendable
	UpdateCardResponse updateCard(UpdateCardRequest request);

	/**
	 * Retrieves a freshly computed list containing all the collectible cards as client entities. Represents the current
	 * master collection of the game.
	 *
	 * @return The cards
	 */
	static List<CardRecord> getCards() {
		GameContext workingContext = GameContext.uninitialized();
		return CardCatalogue.getRecords().values()
				.parallelStream()
				.map(CardCatalogueRecord::getDesc)
				.filter(cd -> cd.collectible
						&& DeckFormat.CUSTOM.isInFormat(cd.set))
				.map(CardDesc::createInstance)
				.map(card -> Games.getEntity(workingContext, card, 0))
				.map(entity -> new CardRecord().entity(entity))
				.collect(toList());
	}
}
