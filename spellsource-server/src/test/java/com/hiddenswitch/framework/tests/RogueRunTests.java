package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.impl.RogueManager;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.schema.spellsource.enums.RogueRunState;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.RogueRun;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.hiddenswitch.framework.schema.spellsource.Tables.ROGUE_RUN;
import static io.vertx.await.Async.await;
import static org.junit.jupiter.api.Assertions.*;

public class RogueRunTests extends FrameworkTestBase {


	@Test
	public void testStartRogueRun(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {
			RogueManager.initCardCatalogue();

			var client = new Client(vertx);
			await(startGateway(vertx));
			await(client.createAndLogin());
			var userId = client.getUserEntity().getId();

			var rogueRun = await(startRogueRun(userId));

			assertEquals(0L, (long) rogueRun.getSeed(), "Seed should be 0");
			assertEquals(userId, rogueRun.getPlayer(), "UserId should match");
			assertEquals(RogueRunState.PRE_MATCH, rogueRun.getState(), "Should have pre match state");

			await(Environment.sleep(200));

			var cardsInDeck = await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck()), row -> row.getString(0)));

			assertFalse(cardsInDeck.isEmpty(), "Deck should have cards");
		});
	}

	@Test
	public void testRogueRunMatch(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {
			RogueManager.initCardCatalogue();

			var client = new Client(vertx);
			await(startGateway(vertx));
			await(client.createAndLogin());
			var userId = client.getUserEntity().getId();

			var rogueRun = await(startRogueRun(userId));

		});
	}

	@Test
	public void testRogueRunChoice(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {
			RogueManager.initCardCatalogue();

			var client = new Client(vertx);
			await(startGateway(vertx));
			await(client.createAndLogin());
			var userId = client.getUserEntity().getId();

			var rogueRun = await(startRogueRun(userId));

			var cardsBefore = await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck()), row -> row.getString(0)));

			var rogueChoice = await(RogueManager.addNewRogueChoice(rogueRun.getId(), new String[]{"rogue_chosen_test"}, -1, 1));

			assertNotEquals(null, rogueChoice.getCards(), "rogue choice should have cards");

			await(RogueManager.updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.STATE, RogueRunState.CHOICE)));

			await(RogueManager.makeRogueChoice(rogueChoice.getId(), List.of(0)));

			var cardsAfter = await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck()), row -> row.getString(0)));
			var deckAfter = await(Legacy.getDeck(RogueManager.cardCatalogue, rogueRun.getDeck(), rogueRun.getPlayer()));

			assertEquals(cardsBefore.size() + 2, cardsAfter.size(), "Expected 2 new cards to be added");
			assertFalse(cardsAfter.stream().anyMatch("rogue_chosen_test"::equals));
			assertEquals(cardsAfter.size(), deckAfter.getCollection().getInventoryCount(), "legacy getdeck gets all cards");


			var anotherChoice = await(RogueManager.addNewRogueChoice(rogueRun.getId(), new String[]{"rogue_another_choice_test"}, -1, 1));

			await(RogueManager.updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.STATE, RogueRunState.CHOICE)));
			await(RogueManager.makeRogueChoice(anotherChoice.getId(), List.of(0)));

			var cardsAfterAgain = await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck()), row -> row.getString(0)));

			assertEquals(cardsAfter.size(), cardsAfterAgain.size(), "Same amount of cards");

			var newChoice = await(Environment.callRoutine(Routines.currentRogueChoice(rogueRun.getId()), RogueManager.rogueChoiceMapper()));

			assertArrayEquals(new String[]{"minion_test_1_3", "spell_test_1_aoe", "weapon_test_1_1"}, newChoice.getCards());
			await(RogueManager.makeRogueChoice(newChoice.getId(), List.of(0)));

			var cardsAfterAgain2 = await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck()), row -> row.getString(0)));

			assertEquals(cardsAfterAgain.size() + 1, cardsAfterAgain2.size(), "Now added another card");

			assertTrue(cardsAfterAgain2.stream().anyMatch("minion_test_1_3"::equals));
		});
	}

	private Future<RogueRun> startRogueRun(String userId) {
		var rogueId = await(RogueManager.startRogueRun(HeroClass.TEST, 0L, userId));

		return RogueManager.returningRogueRun(dsl -> dsl.selectFrom(ROGUE_RUN).where(ROGUE_RUN.ID.eq(rogueId)).limit(1));
	}

	@NotNull
	private MatchmakingQueues createRogueQueue(String queueId) {
		return new MatchmakingQueues()
				.setId(queueId)
				.setAutomaticallyClose(false)
				.setLobbySize(1)
				.setAwaitingLobbyTimeout(0L)
				.setBotOpponent(true)
				.setEmptyLobbyTimeout(0L)
				.setName("single player test")
				.setPrivateLobby(false)
				.setOnce(false)
				.setStartsAutomatically(true)
				.setStillConnectedTimeout(0L);
	}

}
