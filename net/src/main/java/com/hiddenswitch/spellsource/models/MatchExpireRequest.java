package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 12/6/16.
 */
public class MatchExpireRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public String gameId;

	public MatchExpireRequest(String gameId) {
		this.gameId = gameId;
	}
}
