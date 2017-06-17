package com.hiddenswitch.proto3.net.impl;

import com.hiddenswitch.proto3.draft.DraftStatus;
import com.hiddenswitch.proto3.net.client.ApiClient;
import com.hiddenswitch.proto3.net.client.ApiException;
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

import java.util.Arrays;

import static com.hiddenswitch.proto3.net.client.models.DraftState.StatusEnum.COMPLETE;
import static com.hiddenswitch.proto3.net.client.models.DraftState.StatusEnum.RETIRED;
import static com.hiddenswitch.proto3.net.client.models.DraftState.StatusEnum.SELECT_HERO;

public class DraftTest extends ServiceTest<DraftImpl> {
	AccountsImpl accounts = new AccountsImpl();

	@Test
	public void testDraftService(TestContext context) {
		wrapSync(context, () -> {
			// Create an account
			CreateAccountResponse car = accounts.createAccount(new CreateAccountRequest()
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

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<DraftImpl>> done) {
		deploy(Arrays.asList(
				accounts,
				new CardsImpl(),
				new InventoryImpl(),
				new DecksImpl(),
				new LogicImpl()
		), new DraftImpl(), done);
	}
}
