package com.hiddenswitch.proto3.net.models;

/**
 * Created by bberman on 2/4/17.
 */
public enum DeckInstanceType {
	/**
	 * Get a copy of this deck suitable for matches. It will include a lot of extra data for entity instances.
	 */
	MATCH,
	/**
	 * Get a copy of this deck for editing.
	 */
	EDITING
}
