package com.hiddenswitch.spellsource;

import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.draft.DraftContext;
import com.hiddenswitch.spellsource.draft.DraftStatus;
import com.hiddenswitch.spellsource.draft.PrivateDraftState;
import com.hiddenswitch.spellsource.draft.PublicDraftState;
import com.hiddenswitch.spellsource.client.models.DraftState;
import com.hiddenswitch.spellsource.impl.util.DraftRecord;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.MatchmakingQueueConfiguration;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.ext.mongo.UpdateOptions;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;

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
	 * A public state should be shared with the client. The private state contains all the cards the player actually drew
	 * for the draft.
	 *
	 * @param request A request containing the user ID of the player whose draft should be fetched (players only have only
	 *                draft going at a time).
	 * @return The public and private state of the draft. Includes the choices the player has for draft actions.
	 */
	@Suspendable
	static DraftRecord get(GetDraftRequest request) {
		return getRecord(request.userId);
	}

	/**
	 * Choose a hero power or a card action during the draft.
	 *
	 * @param request The appropriate choice given the state of the draft.
	 * @return The new state of this draft.
	 * @throws NullPointerException when an invalid hero or card choice was made despite one being expected.
	 */
	@Suspendable
	static DraftRecord doDraftAction(DraftActionRequest request) throws SuspendExecution, InterruptedException, NullPointerException {
		DraftRecord record = getRecord(request.getUserId());

		if (record == null
				|| (record.getPublicDraftState().getStatus() == DraftStatus.RETIRED
				|| record.getPublicDraftState().getStatus() == DraftStatus.COMPLETE)
				&& (request.getCardIndex() == -1
				&& request.getHeroIndex() == -1)) {
			// Start a new draft

			// TODO: Deduct lives.
			record = new DraftRecord();
			record.setPublicDraftState(new PublicDraftState());
			record.setPrivateDraftState(new PrivateDraftState());
		}

		DraftContext context = new DraftContext()
				.withPrivateState(record.getPrivateDraftState())
				.withPublicState(record.getPublicDraftState());

		switch (context.getPublicState().getStatus()) {
			case NOT_STARTED:
				context.accept(null);
				break;
			case SELECT_HERO:
				if (request.getHeroIndex() == -1) {
					throw new NullPointerException("No hero index was provided.");
				}

				context.onHeroSelected(Future.succeededFuture(record.getPublicDraftState().getHeroClassChoices().get(request.getHeroIndex())));
				break;
			case IN_PROGRESS:
				if (request.getCardIndex() == -1) {
					throw new NullPointerException("No card index was provided.");
				}

				context.onCardSelected(Future.succeededFuture(request.getCardIndex()));
				break;
			case COMPLETE:
				break;
		}

		// If the draft is now complete, create a deck
		if (record.getPublicDraftState().getDeckId() == null
				&& record.getPublicDraftState().getStatus() == DraftStatus.COMPLETE) {
			UserRecord user = Accounts.get(request.getUserId());

			DeckCreateResponse deck = Decks.createDeck(
					new DeckCreateRequest()
							.withName(String.format("%s's Draft Deck", user.getUsername()))
							.withHeroClass(record.getPublicDraftState().getHeroClass())
							.withUserId(request.getUserId())
							.withDraft(true)
							.withCardIds(record.getPublicDraftState().getSelectedCards()));

			record.getPublicDraftState().setDeckId(deck.getDeckId());
		}

		// For now just do a big upsert.
		mongo().updateCollectionWithOptions(DRAFTS, json("_id", request.getUserId()), json("$set", json(record)), new UpdateOptions().setUpsert(true));

		return record;
	}

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
	static RetireDraftResponse retireDraftEarly(RetireDraftRequest request) {
		final DraftRecord record = mongo().findOneAndUpdate(DRAFTS, json("_id", request.getUserId()), json("$set", json("publicDraftState.status", DraftStatus.RETIRED.toString())), DraftRecord.class);
		record.getPublicDraftState().setStatus(DraftStatus.RETIRED);
		return new RetireDraftResponse()
				.withRecord(record);
	}

	/**
	 * Gets the client's draft state based on the given public draft state.
	 *
	 * @param inState The public draft state.
	 * @return A client-ready draft state view.
	 */
	@Suspendable
	static DraftState toDraftState(PublicDraftState inState) {
		GameContext workingContext = GameContext.uninitialized(inState.getHeroClass() == null ? HeroClass.RED : inState.getHeroClass(), HeroClass.RED);
		return new DraftState()
				.cardsRemaining(inState.getCardsRemaining())
				.currentCardChoices(inState.getCurrentCardChoices() == null ? null :
						IntStream.range(0, inState.getCurrentCardChoices().size())
								.mapToObj(i -> Games.getEntity(workingContext, CardCatalogue.getCardById(inState.getCurrentCardChoices().get(i)), 0).id(i))
								.collect(Collectors.toList()))
				.deckId(inState.getDeckId())
				.draftIndex(inState.getDraftIndex())
				.heroClass(inState.getHeroClass() == null ? null : Games.getEntity(workingContext, HeroClass.getHeroCard(inState.getHeroClass()), 0).id(0))
				.heroClassChoices(inState.getHeroClassChoices() == null ? null :
						IntStream.range(0, inState.getHeroClassChoices().size())
								.mapToObj(i -> Games.getEntity(workingContext, HeroClass.getHeroCard(inState.getHeroClassChoices().get(i)), 0).id(i))
								.collect(Collectors.toList()))
				.losses(inState.getLosses())
				.selectedCards(inState.getSelectedCards() == null ? null :
						IntStream.range(0, inState.getSelectedCards().size())
								.mapToObj(i -> Games.getEntity(workingContext, CardCatalogue.getCardById(inState.getSelectedCards().get(i)), 0).id(i))
								.collect(Collectors.toList()))
				.status(DraftState.StatusEnum.valueOf(inState.getStatus().toString()))
				.wins(inState.getWins());
	}

	@Suspendable
	static DraftRecord getRecord(String userId) {
		return mongo().findOne(DRAFTS, json("_id", userId), DraftRecord.class);
	}

	@Suspendable
	static Closeable startDraftQueue() throws SuspendExecution {
		return Matchmaking.startMatchmaker("draft", new MatchmakingQueueConfiguration()
				.setStartsAutomatically(true)
				.setStillConnectedTimeout(1000L)
				.setRules(new CardDesc[0])
				.setRanked(true)
				.setPrivateLobby(false)
				.setOnce(false)
				.setName("Draft")
				.setLobbySize(2)
				.setBotOpponent(false));
	}
}
