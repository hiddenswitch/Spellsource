package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.draft.DraftContext;
import com.hiddenswitch.spellsource.draft.DraftStatus;
import com.hiddenswitch.spellsource.draft.PrivateDraftState;
import com.hiddenswitch.spellsource.draft.PublicDraftState;
import com.hiddenswitch.spellsource.Accounts;
import com.hiddenswitch.spellsource.Decks;
import com.hiddenswitch.spellsource.Draft;
import com.hiddenswitch.spellsource.impl.util.DraftRecord;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.Registration;
import com.hiddenswitch.spellsource.util.RpcClient;
import com.hiddenswitch.spellsource.models.*;
import io.vertx.core.Future;
import io.vertx.ext.mongo.UpdateOptions;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.json;

public class DraftImpl extends AbstractService<DraftImpl> implements Draft {
	private RpcClient<Decks> decks;
	private RpcClient<Accounts> accounts;
	private Registration registration;

	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		super.start();
		decks = Rpc.connect(Decks.class, vertx.eventBus());
		accounts = Rpc.connect(Accounts.class, vertx.eventBus());
		// Create the collection if necessary
		if (!mongo().getCollections().contains(DRAFTS)) {
			mongo().createCollection(DRAFTS);
		}
		registration = Rpc.register(this, Draft.class, vertx.eventBus());
	}

	@Override
	@Suspendable
	public DraftRecord get(GetDraftRequest request) {
		return getRecord(request.userId);
	}

	@Suspendable
	private DraftRecord getRecord(String userId) {
		return mongo().findOne(DRAFTS, json("_id", userId), DraftRecord.class);
	}

	@Override
	@Suspendable
	public DraftRecord doDraftAction(DraftActionRequest request) throws SuspendExecution, InterruptedException, NullPointerException {
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
			UserRecord user = accounts.sync().get(request.getUserId());

			DeckCreateResponse deck = decks.sync().createDeck(
					new DeckCreateRequest()
							.withName(String.format("%s's Draft Deck", user.getProfile().getDisplayName()))
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

	@Override
	@Suspendable
	public MatchDraftResponse matchDraft(MatchDraftRequest request) {
		return null;
	}

	@Override
	@Suspendable
	public RetireDraftResponse retireDraftEarly(RetireDraftRequest request) {
		final DraftRecord record = mongo().findOneAndUpdate(DRAFTS, json("_id", request.getUserId()), json("$set", json("publicDraftState.status", DraftStatus.RETIRED.toString())), DraftRecord.class);
		record.getPublicDraftState().setStatus(DraftStatus.RETIRED);
		return new RetireDraftResponse()
				.withRecord(record);
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		Rpc.unregister(registration);
	}
}
