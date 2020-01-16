package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.draft.DraftStatus;
import com.hiddenswitch.spellsource.net.Draft;
import com.hiddenswitch.spellsource.net.impl.util.DraftRecord;
import com.hiddenswitch.spellsource.net.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.net.models.DraftActionRequest;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static com.hiddenswitch.spellsource.net.impl.Sync.invoke;
import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;
import static org.junit.Assert.*;

public class DraftTest extends SpellsourceTestBase {
	@Test
	public void testDraftService(TestContext context) {
		sync(() -> {
			// Create an account
			CreateAccountResponse car = createRandomAccount();

			// Start a draft
			DraftRecord response = Draft.doDraftAction(new DraftActionRequest().withUserId(car.getUserId()));

			// Choose a hero
			assertNotNull(response.getPublicDraftState().getHeroClassChoices());
			assertEquals(HeroClass.getBaseClasses(DeckFormat.spellsource()).size(), response.getPublicDraftState().getHeroClassChoices().size());
			assertFalse(response.getPublicDraftState().getHeroClassChoices().contains(HeroClass.ANY));
			assertFalse(response.getPublicDraftState().getHeroClassChoices().contains(HeroClass.TEST));

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
		}, context);
	}


	@Test
	public void testDraftAPI(TestContext context) throws ApiException {
		sync(() -> {
			DefaultApi api = getApi();

			String name = RandomStringUtils.randomAlphanumeric(32) + "username";
			String email = name + "@hiddenswitch.com";
			com.hiddenswitch.spellsource.client.models.CreateAccountResponse car = invoke(api::createAccount, new com.hiddenswitch.spellsource.client.models.CreateAccountRequest()
					.name(name)
					.email(email)
					.password("testpass"));

			api.getApiClient().setApiKey(car.getLoginToken());

			try {
				api.draftsGet();
				context.fail("A draft should not yet exist.");
			} catch (ApiException e) {
				context.assertEquals(404, e.getCode(), "The exception codes for drafts get do not match.");
			}


			DraftState state = invoke(api::draftsPost, new DraftsPostRequest().startDraft(true));
			context.assertEquals(DraftState.StatusEnum.SELECT_HERO, state.getStatus(), "The result of starting a draft is unexpectedly not select hero.");
			try {
				api.draftsChooseCard(new DraftsChooseCardRequest().cardIndex(1));
				context.fail("The client should fail to advance a draft without choosing a hero first.");
			} catch (ApiException e) {
				context.assertEquals(400, e.getCode(), "Unexpectedly the client successfully chose a card instead of a hero.");
			}


			state = invoke(api::draftsChooseHero, new DraftsChooseHeroRequest().heroIndex(1));
			context.assertNotNull(state.getHeroClass());

			while (state.getCurrentCardChoices() != null
					&& state.getStatus() == DraftState.StatusEnum.IN_PROGRESS) {
				context.assertEquals(3, state.getCurrentCardChoices().size(), "The number of card choices should always be three");
				Entity card = state.getCurrentCardChoices().get(1);
				context.assertNotNull(card, "The draft service should provide a full card definition.");
				context.assertNotNull(card.getCardId(), "The draft service should at least provide a card ID.");
				state = invoke(api::draftsChooseCard, new DraftsChooseCardRequest().cardIndex(1));
				context.assertEquals(card.getCardId(), state.getSelectedCardIds().get(state.getSelectedCardIds().size() - 1), "The card didn't appear to be selected correctly");
			}

			context.assertEquals(DraftState.StatusEnum.COMPLETE, state.getStatus(), "The status of the draft should be complete.");
			context.assertNotNull(state.getDeckId(), "The draft state should contain a deck ID when it is complete.");

			final String deckId = state.getDeckId();


			try (UnityClient client = new UnityClient(context,car.getLoginToken())) {
				invoke0(client::ensureConnected);
				client.matchmakeQuickPlay(deckId);
				invoke0(client::waitUntilDone);
				context.assertTrue(client.isGameOver());
				context.assertTrue(client.getTurnsPlayed() > 0);
			}

			DraftState newState = invoke(api::draftsPost, new DraftsPostRequest().retireEarly(true));
			context.assertEquals(DraftState.StatusEnum.RETIRED, newState.getStatus(), "Expected a status of retired.");

			newState = invoke(api::draftsPost, new DraftsPostRequest().startDraft(true));
			context.assertEquals(DraftState.StatusEnum.SELECT_HERO, newState.getStatus(), "A draft was not correctly started anew.");
		}, context);
	}
}
