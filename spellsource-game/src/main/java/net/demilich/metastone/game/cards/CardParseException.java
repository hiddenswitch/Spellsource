package net.demilich.metastone.game.cards;

import java.util.Arrays;
import java.util.List;

/**
 * Indicates the card was not able to be parsed.
 * <p>
 * Since the tests will fail if a card cannot be parsed, this can be a {@link RuntimeException} instead of a {@link
 * Exception}.
 */
public class CardParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	public CardParseException(List<String> badCards) {
		super(getMessage(badCards));
	}

	private static String getMessage(List<String> badCards) {
		String message = "The following card files contain errors:\n";
		message += Arrays.toString(badCards.toArray());
		message += "\n\nYou can either fix the errors manually or remove the 'cards.copied' entry from your metastone.properties file to restore all cards.";
		return message;
	}

}
