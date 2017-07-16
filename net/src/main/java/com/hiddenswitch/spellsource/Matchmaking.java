package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.models.*;

/**
 * The matchmaking service is the primary entry point into ranked games for clients.
 */
public interface Matchmaking {
	/**
	 * Enter the matchmaking queue hosted by this instance of the Matchmaking service. The queue right now is a global
	 * queue across all Elo scores.
	 *
	 * In the future, this matchmaking method should try to match players with the closest Elo scores. As the player
	 * waits longer, their tolerance for higher or lower Elo scores than theirs should grow. Two players should be
	 * matched if their tolerances (windows from low Elo to high Elo) match each other.
	 *
	 * The request should be repeated if the retry property of the response is not null.
	 * @param matchmakingRequest The user ID and deck ID to enter into the queue with. Casual games are currently
	 *                           matched against bots. Otherwise, they should match two non-competitive opponents.
	 * @return Connection information or the request to retry with.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	MatchmakingResponse matchmakeAndJoin(MatchmakingRequest matchmakingRequest) throws SuspendExecution, InterruptedException;

	/**
	 * Gets information about the current match the user is in. This is used for reconnecting.
	 * @param request The user's ID.
	 * @return The current match information. This may be a queue entry, the match, or nothing (but not null) if the
	 * user is not in a match.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	CurrentMatchResponse getCurrentMatch(CurrentMatchRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Ends a match and allows the user to re-enter the queue.
	 *
	 * The Games service, which also has an end game session function, is distinct from this one. This method allows
	 * the user to enter the queue again (typically users can only be in one queue at a time, either playing a game
	 * inside the matchmaking queue or waiting to be matched into a game). Typical users should not be able to play
	 * multiple games at once.
	 * @param request The user or game ID to exit from a match.
	 * @return Information about the expiration/cancellation requested.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	MatchExpireResponse expireOrEndMatch(MatchExpireRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Cancels the user's matchmaking queue entry.
	 * @param matchCancelRequest The user's ID.
	 * @return Information about the cancellation.
	 */
	MatchCancelResponse cancel(MatchCancelRequest matchCancelRequest) throws SuspendExecution, InterruptedException;

	/**
	 * Creates a match without entering a queue entry between two users.
	 * @param request All the required information to create a game.
	 * @return Connection information for both users.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	MatchCreateResponse createMatch(MatchCreateRequest request) throws SuspendExecution, InterruptedException;
}
