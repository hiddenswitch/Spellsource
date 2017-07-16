package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 4/3/17.
 */
public class MatchCreateResponse implements Serializable {
	private final CreateGameSessionResponse createGameSessionResponse;

	public MatchCreateResponse(CreateGameSessionResponse createGameSessionResponse) {
		this.createGameSessionResponse = createGameSessionResponse;
	}

	public CreateGameSessionResponse getCreateGameSessionResponse() {
		return createGameSessionResponse;
	}
}
