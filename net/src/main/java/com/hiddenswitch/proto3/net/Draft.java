package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.draft.PublicDraftState;
import com.hiddenswitch.proto3.net.client.models.DraftState;
import com.hiddenswitch.proto3.net.impl.util.DraftRecord;
import com.hiddenswitch.proto3.net.models.*;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;

import java.util.stream.Collectors;

/**
 * The drafts service.
 * <p>
 * Provides a way to start and resume drafts.
 */
public interface Draft {
	/**
	 * The collection name that contains a player's corresponding public and private draft state.
	 */
	String DRAFTS = "drafts";

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
	DraftRecord get(GetDraftRequest request);

	/**
	 * Choose a hero (power) or select one of three cards during a draft.
	 *
	 * @param request The action to perform.
	 * @return The new choices.
	 */
	@Suspendable
	DraftRecord doDraftAction(DraftActionRequest request) throws SuspendExecution, InterruptedException;

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

	/**
	 * Gets the client's draft state based on the given public draft state.
	 *
	 * @param inState The public draft state.
	 * @return A client-ready draft state view.
	 */
	static DraftState toDraftState(PublicDraftState inState) {
		GameContext workingContext = new GameContext(new Player(), new Player(), new GameLogic(), new DeckFormat().withCardSets(CardSet.MINIONATE));
		return new DraftState()
				.cardsRemaining(inState.getCardsRemaining())
				.currentCardChoices(inState.getCurrentCardChoices().stream()
						.map(CardCatalogue::getCardById)
						.map(c -> Games.getEntity(workingContext, c, 0))
						.collect(Collectors.toList()))
				.deckId(inState.getDeckId())
				.draftIndex(inState.getDraftIndex())
				.heroClass(inState.getHeroClass().toString())
				.heroClassChoices(inState.getHeroClassChoices().stream().map(HeroClass::toString).collect(Collectors.toList()))
				.losses(inState.getLosses())
				.selectedCards(inState.getSelectedCards().stream().map(CardCatalogue::getCardById)
						.map(c -> Games.getEntity(workingContext, c, 0))
						.collect(Collectors.toList()))
				.status(DraftState.StatusEnum.valueOf(inState.getStatus().toString()))
				.wins(inState.getWins());
	}
}
