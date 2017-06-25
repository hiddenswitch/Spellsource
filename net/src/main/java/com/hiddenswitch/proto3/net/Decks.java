package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.io.Resources;
import com.hiddenswitch.proto3.net.models.*;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The deck management service.
 */
public interface Decks {
	enum DeckType {
		CONSTRUCTED,
		DRAFT
	}

	/**
	 * Creates a deck with convenient arguments.
	 *
	 * @param request A request that specifies the contents of the deck either as card IDs or inventory IDs.
	 * @return The deck ID of the new deck. Conveniently matches a collection ID.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	DeckCreateResponse createDeck(DeckCreateRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Processes a command to add or remove cards from a deck.
	 *
	 * @param request A possibly complex deck update command for a given user and deck ID.
	 * @return A non-null value if the deck was successfully updated.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	DeckUpdateResponse updateDeck(DeckUpdateRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Trashes a deck. When a deck is in the trash, it will not appear in the user's account but it will still
	 * exist for analytics purposes.
	 *
	 * @param request The deck to delete.
	 * @return The result of trashing the deck.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	DeckDeleteResponse deleteDeck(DeckDeleteRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Replace all constructed decks with a series of decklists
	 *
	 * @param request
	 * @return
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	DeckListUpdateResponse updateAllDecks(DeckListUpdateRequest request) throws SuspendExecution, InterruptedException;
}
