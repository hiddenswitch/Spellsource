package com.hiddenswitch.spellsource.net.tests;

import com.amazonaws.util.Throwables;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.draft.DraftStatus;
import com.hiddenswitch.spellsource.net.Draft;
import com.hiddenswitch.spellsource.net.models.DraftActionRequest;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static com.hiddenswitch.spellsource.net.impl.Sync.invoke;
import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;
import static org.junit.jupiter.api.Assertions.*;

public class DraftTest extends SpellsourceTestBase {
	@Test
	public void testDraftService(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			// Create an account
			var car = createRandomAccount();

			// Start a draft
			var response = Draft.doDraftAction(new DraftActionRequest().withUserId(car.getUserId()));

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
		}, context, vertx);
	}


	@Test
	public void testDraftAPI(Vertx vertx, VertxTestContext context) throws ApiException {
		runOnFiberContext(() -> {
			var api = getApi();

			var name = RandomStringUtils.randomAlphanumeric(32) + "username";
			var email = name + "@hiddenswitch.com";
			var car = invoke(api::createAccount, new com.hiddenswitch.spellsource.client.models.CreateAccountRequest()
					.name(name)
					.email(email)
					.password("testpass"));

			api.getApiClient().setApiKey(car.getLoginToken());

			try {
				invoke(api::draftsGet);
				fail("A draft should not yet exist.");
			} catch (Throwable e) {
				var e1 = Throwables.getRootCause(e);
				if (e1 instanceof ApiException) {
					var t = (ApiException) e1;
					assertEquals(404, t.getCode(), "The exception codes for drafts get do not match.");
				} else {
					fail("Wrong exception");
				}
			}


			var state = invoke(api::draftsPost, new DraftsPostRequest().startDraft(true));
			assertEquals(DraftState.StatusEnum.SELECT_HERO, state.getStatus(), "The result of starting a draft is unexpectedly not select hero.");
			try {
				invoke(api::draftsChooseCard, new DraftsChooseCardRequest().cardIndex(1));
				fail("The client should fail to advance a draft without choosing a hero first.");
			} catch (Throwable e) {
				var e1 = Throwables.getRootCause(e);
				if (e1 instanceof ApiException) {
					var t = (ApiException) e1;
					assertEquals(400, t.getCode(), "Unexpectedly the client successfully chose a card instead of a hero.");
				} else {
					fail("Wrong exception");
				}
			}


			state = invoke(api::draftsChooseHero, new DraftsChooseHeroRequest().heroIndex(1));
			assertNotNull(state.getHeroClass());

			while (state.getCurrentCardChoices() != null
					&& state.getStatus() == DraftState.StatusEnum.IN_PROGRESS) {
				assertEquals(3, state.getCurrentCardChoices().size(), "The number of card choices should always be three");
				var card = state.getCurrentCardChoices().get(1);
				assertNotNull(card, "The draft service should provide a full card definition.");
				assertNotNull(card.getCardId(), "The draft service should at least provide a card ID.");
				state = invoke(api::draftsChooseCard, new DraftsChooseCardRequest().cardIndex(1));
				assertEquals(card.getCardId(), state.getSelectedCardIds().get(state.getSelectedCardIds().size() - 1), "The card didn't appear to be selected correctly");
			}

			assertEquals(DraftState.StatusEnum.COMPLETE, state.getStatus(), "The status of the draft should be complete.");
			assertNotNull(state.getDeckId(), "The draft state should contain a deck ID when it is complete.");

			final var deckId = state.getDeckId();

			try (var client = new UnityClient(context, car.getLoginToken())) {
				invoke0(client::ensureConnected);
				invoke0(client::matchmakeQuickPlay, deckId);
				invoke0(client::waitUntilDone);
				assertTrue(client.isGameOver());
				assertTrue(client.getTurnsPlayed() > 0);
			}

			var newState = invoke(api::draftsPost, new DraftsPostRequest().retireEarly(true));
			assertEquals(DraftState.StatusEnum.RETIRED, newState.getStatus(), "Expected a status of retired.");

			newState = invoke(api::draftsPost, new DraftsPostRequest().startDraft(true));
			assertEquals(DraftState.StatusEnum.SELECT_HERO, newState.getStatus(), "A draft was not correctly started anew.");
		}, context, vertx);
	}
}
