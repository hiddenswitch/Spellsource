package com.hiddenswitch.proto3.net;

import com.hiddenswitch.proto3.net.models.*;

public interface Decks {
	DeckCreateResponse createDeck(DeckCreateRequest request);
	DeckUpdateResponse updateDeck(DeckUpdateRequest request);
	DeckDeleteResponse deleteDeck(DeckDeleteRequest request);
}
