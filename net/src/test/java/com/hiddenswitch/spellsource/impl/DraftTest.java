package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.proto3.draft.DraftStatus;
import com.hiddenswitch.spellsource.impl.util.DraftRecord;
import com.hiddenswitch.spellsource.models.CreateAccountRequest;
import com.hiddenswitch.spellsource.models.DraftActionRequest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Arrays;

public class DraftTest extends ServiceTest<DraftImpl> {
	AccountsImpl accounts = new AccountsImpl();

	@Test
	public void testDraftService(TestContext context) {
		wrapSync(context, () -> {
			// Create an account
			com.hiddenswitch.spellsource.models.CreateAccountResponse car = accounts.createAccount(new CreateAccountRequest()
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
