package net.demilich.metastone.game.decks;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when the deck list sent in a {@link DeckCreateRequest} has errors.
 *
 * The errors in the {@link #getInnerExceptions()} list compromise all the errors in the {@link DeckCreateRequest#fromDeckList(String)} argument
 */
public class DeckListParsingException extends IllegalArgumentException {
	private final List<Throwable> innerExceptions;
	private final String deckList;

	public DeckListParsingException(List<Throwable> innerExceptions, String deckList) {
		super();
		this.innerExceptions = innerExceptions;
		this.deckList = deckList;
	}

	@Override
	public String getMessage() {
		return "Your deck list contains errors:\n"
				+ (innerExceptions != null ? String.join("\n", innerExceptions.stream()
				.map(Throwable::getMessage)
				.collect(Collectors.toList())) : "");
	}

	public String getDeckList() {
		return deckList;
	}

	public List<Throwable> getInnerExceptions() {
		return innerExceptions;
	}
}
