package com.hiddenswitch.spellsource.draft;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;

import java.util.function.Consumer;

/**
 * Stores data and logic relating to drafting cards.
 * <p>
 * This context does not itself start a game. Use a {@link net.demilich.metastone.game.GameContext} for that.
 * <p>
 * Create a draft context using {@link #DraftContext()}, then set its behaviour using {@link
 * #setBehaviour(DraftBehaviour)}. The default behaviour <b>never</b> replies to any choices.
 * <p>
 * To start the draft, call {@link #accept(Handler)} with a handler when the draft is done.
 * <p>
 * Retrieve the current state of the draft using {@link #getPublicState()}.
 *
 * @see DraftLogic for the specific rules regarding which cards are chosen to show to a player during a draft.
 */
public class DraftContext implements Consumer<Handler<AsyncResult<DraftContext>>> {
	private DraftLogic logic = new DraftLogic(this);
	private PublicDraftState publicState = new PublicDraftState();
	private PrivateDraftState privateState = new PrivateDraftState();
	private DraftBehaviour behaviour = new NullDraftBehaviour();
	private CardCatalogue cardCatalogue = ClasspathCardCatalogue.INSTANCE;
	private Handler<AsyncResult<DraftContext>> handleDone;

	/**
	 * Creates a new draft context with a {@link NullDraftBehaviour} as the default.
	 */
	public DraftContext() {
	}

	/**
	 * Starts a draft.
	 *
	 * @param done Called when all the draft choices have been made and the deck the player has been constructing is ready
	 *             to play.
	 */
	public void accept(Handler<AsyncResult<DraftContext>> done) {
		if (handleDone != null) {
			throw new RuntimeException("Stale draft context.");
		}

		handleDone = done;
		// Resume from the correct spot
		switch (getPublicState().getStatus()) {
			case NOT_STARTED:
				getLogic().initializeDraft();
				selectHero();
				break;
			case SELECT_HERO:
				selectHero();
				break;
			case IN_PROGRESS:
				selectCard();
				break;
			case COMPLETE:
				done.handle(Future.succeededFuture(this));
				break;
		}
	}

	public CardCatalogue getCardCatalogue() {
		return cardCatalogue;
	}

	protected void selectHero() {
		getBehaviour().chooseHeroAsync(getPublicState().getHeroClassChoices(), this::onHeroSelected);
	}

	/**
	 * This handler should be called with the player's champion selection.
	 *
	 * @param choice The champion selected by the player.
	 */
	public void onHeroSelected(AsyncResult<String> choice) {
		if (choice.failed()) {
			// TODO: Retry
			return;
		}

		getLogic().startDraft(choice.result());
		selectCard();
	}

	protected void selectCard() {
		getBehaviour().chooseCardAsync(getLogic().getCardChoices(), this::onCardSelected);
	}

	/**
	 * This handler should be called whenever the player makes a card choice.
	 *
	 * @param selectedCardResult The index of the card chosen in {@link PublicDraftState#getCurrentCardChoices()}.
	 */
	public void onCardSelected(AsyncResult<Integer> selectedCardResult) {
		getLogic().selectCard(selectedCardResult.result());

		if (getLogic().isDraftOver()) {
			// TODO: What do we do when we're done? We should create a deck and populate it
			if (handleDone != null) {
				handleDone.handle(Future.succeededFuture(this));
			}

		} else {
			selectCard();
		}
	}

	/**
	 * Gets the draft logic.
	 *
	 * @return
	 */
	public DraftLogic getLogic() {
		return logic;
	}

	public void setLogic(DraftLogic logic) {
		this.logic = logic;
	}

	/**
	 * Gets the public draft state. This state is safe to share with the end user and does not leak any information that
	 * would advantage the player unfairly in the drafting process.
	 *
	 * @return
	 */
	public PublicDraftState getPublicState() {
		return publicState;
	}

	public void setPublicState(PublicDraftState publicState) {
		this.publicState = publicState;
	}

	/**
	 * Gets the private draft state. In practice, this is the pre-generated list of cards the player will see, according
	 * to the current rules in {@link DraftLogic}. This should never be shared with an end-user, since it can be used to
	 * cheat.
	 *
	 * @return
	 */
	public PrivateDraftState getPrivateState() {
		return privateState;
	}

	public void setPrivateState(PrivateDraftState privateState) {
		this.privateState = privateState;
	}

	/**
	 * Gets the behaviour to which draft requests are delegated.
	 *
	 * @return
	 */
	public DraftBehaviour getBehaviour() {
		return behaviour;
	}

	/**
	 * Sets the behaviour. Beware of the {@link NullDraftBehaviour}--it does not ever call its callbacks, so the {@link
	 * DraftContext} is never completed by it.
	 *
	 * @param behaviour
	 */
	public void setBehaviour(DraftBehaviour behaviour) {
		this.behaviour = behaviour;
	}

	public DraftContext withLogic(final DraftLogic logic) {
		this.setLogic(logic);
		return this;
	}

	public DraftContext withPublicState(final PublicDraftState publicState) {
		this.setPublicState(publicState);
		return this;
	}

	public DraftContext withPrivateState(final PrivateDraftState privateState) {
		this.setPrivateState(privateState);
		return this;
	}

	public DraftContext withBehaviour(final DraftBehaviour behaviour) {
		this.setBehaviour(behaviour);
		return this;
	}

	protected void notifyPublicStateChanged() {
		getBehaviour().notifyDraftState(getPublicState());
	}
}
