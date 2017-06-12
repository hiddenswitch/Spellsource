package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.*;

/**
 * The drafts service.
 * <p>
 * Provides a way to start and resume drafts.
 */
public interface Draft {
	/**
	 * Gets the public and private state of the draft.
	 * <p>
	 * A public state should be shared with the client. The private state contains all the cards the player actually
	 * drew for the draft.
	 *
	 * @param request A request containing the user ID of the player whose draft should be fetched (players only have
	 *                only draft going at a time).
	 * @return The public and private state of the draft. Includes the choices the player has for draft actions.
	 */
	@Suspendable
	GetDraftResponse get(GetDraftRequest request);

	/**
	 * Choose a hero (power) or select one of three cards during a draft.
	 *
	 * @param request The action to perform.
	 * @return The new choices.
	 */
	@Suspendable
	DraftActionResponse doDraftAction(DraftActionRequest request);

	/**
	 * Enters matchmaking with the deck built with a draft.
	 * <p>
	 * When the player loses this match, they lose a life.
	 *
	 * @param request The user ID of the player who should be entered into the draft.
	 * @return Connection or retry information for the draft matchmaking.
	 */
	@Suspendable
	MatchDraftResponse matchDraft(MatchDraftRequest request);

	/**
	 * Quits a draft early.
	 * <p>
	 * If the player has lost fewer than 3 matches with his current draft deck, the player can quit the draft early. The
	 * player spends the difference in lives to quit early. If the player has lost 2 times, the player must pay an
	 * additional 1 life to retire early.
	 *
	 * @param request The user ID of the player to retire
	 * @return Possibly statistics related to the ending of the draft.
	 */
	@Suspendable
	RetireDraftResponse retireDraftEarly(RetireDraftRequest request);
}
