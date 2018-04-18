package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.client.ApiClient;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.draft.DraftStatus;
import com.hiddenswitch.spellsource.impl.*;
import com.hiddenswitch.spellsource.impl.util.DraftRecord;
import com.hiddenswitch.spellsource.models.CreateAccountRequest;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.models.DraftActionRequest;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class DraftTest extends SpellsourceTestBase {
	@Test
	public void testDraftService() {
		sync(() -> {
			// Create an account
			CreateAccountResponse car = createRandomAccount();

			// Start a draft
			DraftRecord response = Draft.doDraftAction(new DraftActionRequest().withUserId(car.getUserId()));

			// Choose a hero
			assertNotNull(response.getPublicDraftState().getHeroClassChoices());
			assertEquals(3, response.getPublicDraftState().getHeroClassChoices().size());

			response = Draft.doDraftAction(new DraftActionRequest()
					.withUserId(car.getUserId())
					.withHeroIndex(1));

			// Choose cards until none can be chosen anymore
			while (response.getPublicDraftState().getStatus() != DraftStatus.COMPLETE) {
				assertEquals(3, response.getPublicDraftState().getCurrentCardChoices().size());
				response = Draft.doDraftAction(new DraftActionRequest()
						.withUserId(car.getUserId())
						.withCardIndex(1));
			}

			assertNotNull(response.getPublicDraftState().getDeckId());
		});
	}


	@Test
	public void testDraftAPI(TestContext context) throws ApiException {
		DefaultApi api = getApi();

		String name = RandomStringUtils.randomAlphanumeric(32) + "username";
		String email = name + "@hiddenswitch.com";
		com.hiddenswitch.spellsource.client.models.CreateAccountResponse car = api.createAccount(new com.hiddenswitch.spellsource.client.models.CreateAccountRequest()
				.name(name)
				.email(email)
				.password("testpass"));

		api.getApiClient().setApiKey(car.getLoginToken());

		try {
			api.draftsGet();
		} catch (ApiException e) {
			context.assertEquals(404, e.getCode(), "The exception codes for drafts get do not match.");
		}


		DraftState state = api.draftsPost(new DraftsPostRequest().startDraft(true));
		context.assertEquals(DraftState.StatusEnum.SELECT_HERO, state.getStatus(), "The result of starting a draft is unexpectedly not select hero.");
		try {
			api.draftsChooseCard(new DraftsChooseCardRequest().cardIndex(1));
		} catch (ApiException e) {
			context.assertEquals(400, e.getCode(), "Unexpectedly the client successfully chose a card instead of a hero.");
		}


		state = api.draftsChooseHero(new DraftsChooseHeroRequest().heroIndex(1));
		context.assertNotNull(state.getHeroClass());

		while (state.getCurrentCardChoices() != null
				&& state.getStatus() == DraftState.StatusEnum.IN_PROGRESS) {
			context.assertEquals(3, state.getCurrentCardChoices().size(), "The number of card choices should always be three");
			Entity card = state.getCurrentCardChoices().get(1);
			context.assertNotNull(card, "The draft service should provide a full card definition.");
			context.assertNotNull(card.getCardId(), "The draft service should at least provide a card ID.");
			state = api.draftsChooseCard(new DraftsChooseCardRequest().cardIndex(1));
			context.assertEquals(card.getCardId(), state.getSelectedCards().get(state.getSelectedCards().size() - 1).getCardId(), "The card didn't appear to be selected correctly");
		}

		context.assertEquals(DraftState.StatusEnum.COMPLETE, state.getStatus(), "The status of the draft should be complete.");
		context.assertNotNull(state.getDeckId(), "The draft state should contain a deck ID when it is complete.");

		final String deckId = state.getDeckId();
		vertx.executeBlocking(done -> {
			new UnityClient(context).loginWithUserAccount(name, "testpass").matchmakeAndPlayAgainstAI(deckId).waitUntilDone();
			done.handle(Future.succeededFuture());
		}, context.asyncAssertSuccess(then -> {
			DraftState newState = null;
			try {
				newState = api.draftsPost(new DraftsPostRequest().retireEarly(true));
			} catch (ApiException e) {
				context.fail();
			}
			context.assertEquals(DraftState.StatusEnum.RETIRED, newState.getStatus(), "Expected a status of retired.");

			try {
				api.draftsGet();
			} catch (ApiException e) {
				context.assertEquals(404, e.getCode(), "There should be no draft if we retired the draft early.");
			}


			try {
				newState = api.draftsPost(new DraftsPostRequest().startDraft(true));
			} catch (ApiException e) {
				context.fail();
			}
			context.assertEquals(DraftState.StatusEnum.SELECT_HERO, newState.getStatus(), "A draft was not correctly started anew.");
		}));
	}
}
