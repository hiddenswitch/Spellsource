package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;

/**
 * The cards service. This manages the base card definitions.
 */
public interface Cards {
	/**
	 * Gets information about a specific card.
	 * @param request The ID of the card to query.
	 * @return A card catalogue record corresponding to the requested ID.
	 */
	@Suspendable
	GetCardResponse getCard(GetCardRequest request);

	/**
	 * Queries the card catalogue with the specified parameters and returns the corresponding card records. Useful for
	 * filtering through the card catalogue.
	 * @param request A variety of different filtering parameters for querying the card catalogue.
	 * @return Records which match the filters in the request.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	QueryCardsResponse queryCards(QueryCardsRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Inserts a card catalogue record.
	 * @param request A card catalogue record to insert.
	 * @return Information about the insertion (typically empty).
	 */
	@Suspendable
	InsertCardResponse insertCard(InsertCardRequest request);

	/**
	 * Updates a card catalogue record using its card ID.
	 * @param request The card catalogue record to update.
	 * @return Information about the update (typically empty).
	 */
	@Suspendable
	UpdateCardResponse updateCard(UpdateCardRequest request);
}
