package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;

public interface Decks {
	@Suspendable
	DeckCreateResponse createDeck(DeckCreateRequest request);

	@Suspendable
	DeckUpdateResponse updateDeck(DeckUpdateRequest request);

	@Suspendable
	DeckDeleteResponse deleteDeck(DeckDeleteRequest request);

	@Suspendable
	DeckUseResponse useDeck(DeckUseRequest request) throws SuspendExecution, InterruptedException;

	DeckReturnResponse returnDeck(DeckReturnRequest request);
}
