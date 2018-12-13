package com.hiddenswitch.spellsource.common;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when the deck list sent in a {@link DeckCreateRequest} has errors.
 */
public class DeckListParsingException extends RuntimeException {
	private List<Throwable> innerExceptions;

	public DeckListParsingException(List<Throwable> innerExceptions) {
		super();
		this.innerExceptions = innerExceptions;
	}

	@Override
	public String getMessage() {
		return "Your deck list contains errors:\n"
				+ (innerExceptions != null ? String.join("\n", innerExceptions.stream()
				.map(Throwable::getMessage)
				.collect(Collectors.toList())) : "");
	}

	public List<Throwable> getInnerExceptions() {
		return innerExceptions;
	}
}
