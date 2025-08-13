package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.Matchmaking;
import com.hiddenswitch.framework.impl.ClusteredGames;
import com.hiddenswitch.framework.impl.RogueManager;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.schema.spellsource.enums.RogueRunState;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.RogueRun;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.core.*;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

			var cardsInDeck =
				await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));

			assertFalse(cardsInDeck.isEmpty(), "Deck should have cards");
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

			var cardsBefore =
				await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));

			var rogueChoice = await(RogueManager.addNewRogueChoice(rogueRun.getId(), new String[]{"rogue_chosen_test"}, -1,
				1));

			assertNotEquals(null, rogueChoice.getCards(), "rogue choice should have cards");

			await(RogueManager.updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.STATE, RogueRunState.CHOICE)));

			await(RogueManager.makeRogueChoice(rogueChoice.getId(), List.of(0)));

			var cardsAfter =
				await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));
			var deckAfter = await(Legacy.getDeck(RogueManager.cardCatalogue, rogueRun.getDeck(), rogueRun.getPlayer()));

			assertEquals(cardsBefore.size() + 2, cardsAfter.size(), "Expected 2 new cards to be added");
			assertFalse(cardsAfter.stream().anyMatch("rogue_chosen_test"::equals));
			assertEquals(cardsAfter.size(), deckAfter.getCollection().getInventoryCount(), "legacy getdeck gets all cards");


			var anotherChoice = await(RogueManager.addNewRogueChoice(rogueRun.getId(), new String[]{
				"rogue_another_choice_test"}, -1, 1));

			await(RogueManager.updateRogueRun(rogueRun.getId(), r -> r.set(ROGUE_RUN.STATE, RogueRunState.CHOICE)));
			await(RogueManager.makeRogueChoice(anotherChoice.getId(), List.of(0)));

			var cardsAfterAgain =
				await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));

			assertEquals(cardsAfter.size(), cardsAfterAgain.size(), "Same amount of cards");

			var newChoice =
				await(Environment.callRoutine(Routines.currentRogueChoice(rogueRun.getId())).execute(RogueManager.rogueChoiceMapper()));

			assertArrayEquals(new String[]{"minion_test_1_3", "spell_test_1_aoe", "weapon_test_1_1"}, newChoice.getCards());
			await(RogueManager.makeRogueChoice(newChoice.getId(), List.of(0)));

			var cardsAfterAgain2 =
				await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));

			assertEquals(cardsAfterAgain.size() + 1, cardsAfterAgain2.size(), "Now added another card");

			assertTrue(cardsAfterAgain2.stream().anyMatch("minion_test_1_3"::equals));
		});
	}

	@Test
	@Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
	public void testRogueRunMatch(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {
			RogueManager.initCardCatalogue();

			var client = new Client(vertx);
			await(startGateway(vertx));
			await(startMatchmakingServices(vertx));
			await(client.createAndLogin());
			var userId = client.getUserEntity().getId();

			var rogueRun = await(startRogueRun(userId));

			assertEquals(RogueRunState.PRE_MATCH, await(RogueManager.getRogueRun(rogueRun.getId())).getState(), "Rogue run " +
				"should be in in pre game state");

			var stream = await(client.matchmaking().enqueue(matchmaking ->
				matchmaking.write(Spellsource.MatchmakingQueuePutRequest.newBuilder()
					.setQueueId("rogueRun")
					.setDeckId(rogueRun.getDeck())
					.setBotDeckId(rogueRun.getOpponentDeck())
					.build())));

			var responded = Promise.<Spellsource.MatchmakingQueuePutResponse>promise();
			stream.handler(responded::complete);
			await(responded.future());

			await(Environment.sleep(200));

			assertEquals(RogueRunState.IN_MATCH, await(RogueManager.getRogueRun(rogueRun.getId())).getState(), "Rogue run " +
				"should now be in in game state");

			await(client.playUntilGameOver());

			await(Environment.sleep(200));

			assertNotEquals(RogueRunState.IN_MATCH, await(RogueManager.getRogueRun(rogueRun.getId())).getState(), "Rogue run" +
				" should now be in post game state");
		});
	}

	@Test
	public void testTrashCard(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {
			RogueManager.initCardCatalogue();

			var client = new Client(vertx);
			await(startGateway(vertx));
			await(client.createAndLogin());
			var userId = client.getUserEntity().getId();

			var rogueRun = await(startRogueRun(userId));

			var cardsInDeck =
				await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));

			var lengthBefore = cardsInDeck.size();
			var firstCard = cardsInDeck.getFirst();

			await(RogueManager.trashCard(rogueRun.getId(), firstCard));

			var newCardsInDeck =
				await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));

			assertEquals(lengthBefore - 1, newCardsInDeck.size());
		});
	}

	@Test
	public void testAfterMatchChoices(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {
			RogueManager.initCardCatalogue();

			var client = new Client(vertx);
			await(startGateway(vertx));
			await(startMatchmakingServices(vertx));
			await(client.createAndLogin());
			var userId = client.getUserEntity().getId();

			var rogueRun = await(startRogueRun(userId));

			// No cards to ensure opponent will lose
			await(Environment.callRoutine(Routines.setCardsInDeck(rogueRun.getOpponentDeck(), new String[0])).execute(RowMappers.getCardsInDeckMapper()));

			var stream = await(client.matchmaking().enqueue(matchmaking ->
				matchmaking.write(Spellsource.MatchmakingQueuePutRequest.newBuilder()
					.setQueueId("rogueRun")
					.setDeckId(rogueRun.getDeck())
					.setBotDeckId(rogueRun.getOpponentDeck())
					.build())));

			var responded = Promise.<Spellsource.MatchmakingQueuePutResponse>promise();
			stream.handler(responded::complete);
			await(responded.future());

			await(client.playUntilGameOver());
			await(Environment.sleep(200));

			var deckBefore =
				await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));

			for (int i = 0; i < 4; i++) {
				var choice =
					await(Environment.callRoutine(Routines.currentRogueChoice(rogueRun.getId())).execute(RogueManager.rogueChoiceMapper()));

				assertNotNull(choice, "Choice " + (i + 1) + " should not be null");

				await(RogueManager.makeRogueChoice(choice.getId(), List.of(0)));
			}

			var deckAfter =
				await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));

			assertEquals(deckBefore.size() + 4, deckAfter.size(), "1 equipment choice, then 3 of card choices");
		});
	}


	private Future<RogueRun> startRogueRun(String userId) {
		var rogueId = await(RogueManager.startRogueRun(HeroClass.TEST, 0L, userId));

		return RogueManager.getRogueRun(rogueId);
	}

	protected Future<Void> startMatchmakingServices(Vertx vertx) {
		await(vertx.deployVerticle(new Matchmaking(),
			new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)));
		await(vertx.deployVerticle(new ClusteredGames(),
			new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)));

		return Future.succeededFuture();
	}

}
