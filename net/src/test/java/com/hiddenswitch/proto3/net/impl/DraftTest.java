package com.hiddenswitch.proto3.net.impl;

import com.hiddenswitch.proto3.draft.DraftStatus;
import com.hiddenswitch.proto3.net.client.ApiClient;
import com.hiddenswitch.proto3.net.client.ApiException;
import com.hiddenswitch.proto3.net.client.ApiResponse;
import com.hiddenswitch.proto3.net.client.api.DefaultApi;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.impl.util.DraftRecord;
import com.hiddenswitch.proto3.net.models.CreateAccountRequest;
import com.hiddenswitch.proto3.net.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.models.DraftActionRequest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import static com.hiddenswitch.proto3.net.client.models.DraftState.StatusEnum.COMPLETE;
import static com.hiddenswitch.proto3.net.client.models.DraftState.StatusEnum.RETIRED;
import static com.hiddenswitch.proto3.net.client.models.DraftState.StatusEnum.SELECT_HERO;

public class DraftTest extends ServiceTest<DraftImpl> {
	ServerImpl server;

	@Test
	public void testDraftService(TestContext context) {
		wrapSync(context, () -> {
			// Create an account
			CreateAccountResponse car = server.accounts.createAccount(new CreateAccountRequest()
					.withEmailAddress("test@hiddenswitch.com")
					.withName("testuser")
					.withPassword("testpass"));

			// Start a draft
			DraftRecord response = service.doDraftAction(new DraftActionRequest().withUserId(car.getUserId()));

			// Choose a hero
			getContext().assertNotNull(response.getPublicDraftState().getHeroClassChoices());
			getContext().assertEquals(3, response.getPublicDraftState().getHeroClassChoices().size());

			response = service.doDraftAction(new DraftActionRequest()
					.withUserId(car.getUserId())
					.withHeroIndex(1));

			// Choose cards until none can be chosen anymore
			while (response.getPublicDraftState().getStatus() != DraftStatus.COMPLETE) {
				getContext().assertEquals(3, response.getPublicDraftState().getCurrentCardChoices().size());
				response = service.doDraftAction(new DraftActionRequest()
						.withUserId(car.getUserId())
						.withCardIndex(1));
			}

			getContext().assertNotNull(response.getPublicDraftState().getDeckId());
		});
	}

	@Test
	public void testDraftAPI(TestContext context) throws ApiException {
		DefaultApi api = new DefaultApi(new ApiClient().setBasePath("http://localhost:8080/v1"));

		com.hiddenswitch.proto3.net.client.models.CreateAccountResponse car = api.createAccount(new com.hiddenswitch.proto3.net.client.models.CreateAccountRequest()
				.name("testuser")
				.email("testemail@email.com")
				.password("testpassword"));

		api.getApiClient().setApiKey(car.getLoginToken());

		try {
			api.draftsGet();
		} catch (ApiException e) {
			context.assertEquals(404, e.getCode(), "The exception codes for drafts get do not match.");
		}


		DraftState state = api.draftsPost(new DraftsPostRequest().startDraft(true));
		context.assertEquals(SELECT_HERO, state.getStatus(), "The result of starting a draft is unexpectedly not select hero.");
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

		context.assertEquals(COMPLETE, state.getStatus(), "The status of the draft should be complete.");
		context.assertNotNull(state.getDeckId(), "The draft state should contain a deck ID when it is complete.");

		state = api.draftsPost(new DraftsPostRequest().retireEarly(true));
		context.assertEquals(RETIRED, state.getStatus(), "Expected a status of retired.");

		try {
			api.draftsGet();
		} catch (ApiException e) {
			context.assertEquals(404, e.getCode(), "There should be no draft if we retired the draft early.");
		}
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<DraftImpl>> done) {
		server = new ServerImpl();
		DraftImpl instance = server.drafts;
		vertx.deployVerticle(server, then -> done.handle(Future.succeededFuture(instance)));
	}
}
