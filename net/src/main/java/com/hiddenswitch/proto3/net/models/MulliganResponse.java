package com.hiddenswitch.proto3.net.models;

import net.demilich.metastone.game.cards.Card;

import java.io.Serializable;
import java.util.List;

/**
 * Contains the result of a bot mulliganing.
 */
public class MulliganResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * A list of which cards to discard.
	 */
	public List<Card> discardedCards;
}
