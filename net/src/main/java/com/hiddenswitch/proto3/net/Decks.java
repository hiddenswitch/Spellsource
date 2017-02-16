package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;

public interface Decks {
	DeckCreateResponse createDeck(DeckCreateRequest request) throws SuspendExecution, InterruptedException;

	DeckUpdateResponse updateDeck(DeckUpdateRequest request) throws SuspendExecution, InterruptedException;

	@Suspendable
	DeckDeleteResponse deleteDeck(DeckDeleteRequest request);

	DeckUseResponse useDeck(DeckUseRequest request) throws SuspendExecution, InterruptedException;

	DeckReturnResponse returnDeck(DeckReturnRequest request);
}
