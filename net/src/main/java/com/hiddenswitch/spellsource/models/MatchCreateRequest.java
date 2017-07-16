package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.util.Matchmaker;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckWithId;

import java.io.Serializable;

/**
 * Created by bberman on 4/3/17.
 */
public class MatchCreateRequest implements Serializable {
	private String deckId2;
	private String userId1;
	private String userId2;
	private String gameId;
	private String deckId1;
	private Deck deck1;
	private Deck deck2;
	private boolean bot1;
	private boolean bot2;

	public MatchCreateRequest(Matchmaker.Match match) {
		// Create a game session.
		Deck deck1 = match.entry1.deck;
		Deck deck2 = match.entry2.deck;

		deckId1 = ((DeckWithId) deck1).getDeckId();
		deckId2 = ((DeckWithId) deck2).getDeckId();
		userId1 = match.entry1.userId;
		userId2 = match.entry2.userId;
		gameId = match.gameId;
	}

	public MatchCreateRequest(String gameId, String userId1, String userId2, boolean bot2, String deckId1, String deckId2) {
		this.deckId2 = deckId2;
		this.userId1 = userId1;
		this.userId2 = userId2;
		this.gameId = gameId;
		this.deckId1 = deckId1;
		this.bot2 = bot2;
	}

	public String getDeckId1() {
		return deckId1;
	}

	public void setDeckId1(String deckId1) {
		this.deckId1 = deckId1;
	}

	public String getDeckId2() {
		return deckId2;
	}

	public void setDeckId2(String deckId2) {
		this.deckId2 = deckId2;
	}

	public String getUserId1() {
		return userId1;
	}

	public void setUserId1(String userId1) {
		this.userId1 = userId1;
	}

	public String getUserId2() {
		return userId2;
	}

	public void setUserId2(String userId2) {
		this.userId2 = userId2;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public Deck getDeck1() {
		return deck1;
	}

	public void setDeck1(Deck deck1) {
		this.deck1 = deck1;
	}

	public Deck getDeck2() {
		return deck2;
	}

	public void setDeck2(Deck deck2) {
		this.deck2 = deck2;
	}

	public boolean isBot1() {
		return bot1;
	}

	public void setBot1(boolean bot1) {
		this.bot1 = bot1;
	}

	public boolean isBot2() {
		return bot2;
	}

	public void setBot2(boolean bot2) {
		this.bot2 = bot2;
	}
}
