package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.UserId;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 12/6/16.
 */
public class MatchExpireRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public String gameId;
	public List<UserId> users;

	public MatchExpireRequest(String gameId) {
		this.gameId = gameId;
	}
}
