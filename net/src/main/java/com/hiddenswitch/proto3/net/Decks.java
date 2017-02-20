package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;

public interface Decks {
	DeckCreateResponse createDeck(DeckCreateRequest request) throws SuspendExecution, InterruptedException;

	DeckUpdateResponse updateDeck(DeckUpdateRequest request) throws SuspendExecution, InterruptedException;

	DeckDeleteResponse deleteDeck(DeckDeleteRequest request) throws SuspendExecution, InterruptedException;
}
