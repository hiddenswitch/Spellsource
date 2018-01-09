package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.DeckId;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.Matchmaker;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckWithId;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Created by bberman on 4/3/17.
 */
public class MatchCreateRequest implements Serializable {
	private UserId userId1;
	private UserId userId2;
	private GameId gameId;
	private DeckId deckId1;
	private DeckId deckId2;
	private boolean bot1;
	private boolean bot2;

	public MatchCreateRequest withUserId1(final UserId userId1) {
		this.userId1 = userId1;
		return this;
	}

	public MatchCreateRequest withUserId2(final UserId userId2) {
		this.userId2 = userId2;
		return this;
	}

	public MatchCreateRequest withGameId(final GameId gameId) {
		this.gameId = gameId;
		return this;
	}

	public MatchCreateRequest withDeckId1(final DeckId deckId1) {
		this.deckId1 = deckId1;
		return this;
	}

	public MatchCreateRequest withDeckId2(final DeckId deckId2) {
		this.deckId2 = deckId2;
		return this;
	}

	public MatchCreateRequest withBot1(final boolean bot1) {
		this.bot1 = bot1;
		return this;
	}

	public MatchCreateRequest withBot2(final boolean bot2) {
		this.bot2 = bot2;
		return this;
	}

	public UserId getUserId1() {
		return userId1;
	}

	public UserId getUserId2() {
		return userId2;
	}

	public GameId getGameId() {
		return gameId;
	}

	public DeckId getDeckId1() {
		return deckId1;
	}

	public DeckId getDeckId2() {
		return deckId2;
	}

	public boolean isBot1() {
		return bot1;
	}

	public boolean isBot2() {
		return bot2;
	}

	public static MatchCreateRequest botMatch(GameId gameId, UserId userId, UserId botId, DeckId deckId, DeckId botDeckId) {
		return new MatchCreateRequest()
				.withGameId(gameId)
				.withUserId1(userId)
				.withUserId2(botId)
				.withDeckId1(deckId)
				.withDeckId2(botDeckId)
				.withBot2(true);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
