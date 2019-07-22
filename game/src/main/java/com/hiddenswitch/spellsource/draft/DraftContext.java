package com.hiddenswitch.spellsource.draft;

import com.hiddenswitch.spellsource.util.Result;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.function.Consumer;

/**
 * Stores data and logic relating to drafting cards.
 */
public class DraftContext implements Consumer<Handler<AsyncResult<DraftContext>>> {
	private DraftLogic logic = new DraftLogic(this);
	private PublicDraftState publicState = new PublicDraftState();
	private PrivateDraftState privateState = new PrivateDraftState();
	private DraftBehaviour behaviour = new NullDraftBehaviour();
	private Handler<AsyncResult<DraftContext>> handleDone;

	public DraftContext() {
	}

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

	protected void selectHero() {
		getBehaviour().chooseHeroAsync(getPublicState().getHeroClassChoices(), this::onHeroSelected);
	}

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

	public void onCardSelected(AsyncResult<Integer> selectedCardResult) {
		getLogic().selectCard(selectedCardResult.result());

		if (getLogic().isDraftOver()) {
			// TODO: What do we do when we're done? We should create a deck and populate it
			if (handleDone != null) {
				handleDone.handle(new Result<>(this));
			}

		} else {
			selectCard();
		}
	}

	public DraftLogic getLogic() {
		return logic;
	}

	public void setLogic(DraftLogic logic) {
		this.logic = logic;
	}

	public PublicDraftState getPublicState() {
		return publicState;
	}

	public void setPublicState(PublicDraftState publicState) {
		this.publicState = publicState;
	}

	public PrivateDraftState getPrivateState() {
		return privateState;
	}

	public void setPrivateState(PrivateDraftState privateState) {
		this.privateState = privateState;
	}

	public DraftBehaviour getBehaviour() {
		return behaviour;
	}

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
