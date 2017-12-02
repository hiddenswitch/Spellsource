package com.hiddenswitch.spellsource.models;

import java.util.List;
import java.util.stream.Collectors;

public class DeckListParsingException extends Exception {
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
