package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

/**
 * Created by bberman on 12/6/16.
 */
public class MatchExpireResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	public boolean expired;
	public boolean matchNotFoundOrAlreadyExpired;
}
