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

			var cardsInDeck =
				await(Environment.callRoutine(Routines.getCardsInDeck(rogueRun.getDeck())).execute(row -> row.getString(0)));

			assertFalse(cardsInDeck.isEmpty(), "Deck should have cards");


			rogueRun = await(RogueManager.makeAnyChoices(rogueRun.getId()));

			assertEquals(RogueRunState.PRE_MATCH, rogueRun.getState(), "Should have pre match state");
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

			await(RogueManager.makeAnyChoices(rogueRun.getId()));

			assertEquals(RogueRunState.PRE_MATCH, await(RogueManager.getRogueRun(rogueRun.getId())).getState(), "Rogue run " +
				"should be in in pre game state");

			var stream =
				await(client.matchmaking().enqueue(matchmaking -> matchmaking.write(Spellsource.MatchmakingQueuePutRequest.newBuilder().setQueueId("rogueRun").setDeckId(rogueRun.getDeck()).setBotDeckId(rogueRun.getOpponentDeck()).build())));

			var responded = Promise.<Spellsource.MatchmakingQueuePutResponse>promise();
			stream.handler(responded::complete);
			await(responded.future());

			await(Environment.sleep(200));

			assertEquals(RogueRunState.IN_MATCH, await(RogueManager.getRogueRun(rogueRun.getId())).getState(),
				"Rogue run should now be in in game state");

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

			var stream =
				await(client.matchmaking().enqueue(matchmaking -> matchmaking.write(Spellsource.MatchmakingQueuePutRequest.newBuilder().setQueueId("rogueRun").setDeckId(rogueRun.getDeck()).setBotDeckId(rogueRun.getOpponentDeck()).build())));

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

	@Test
	public void testRoutineCallerFromEventLoopAndUpgradeAndProgressIsolation(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {
			RogueManager.initCardCatalogue();

			var client = new Client(vertx);
			await(startGateway(vertx));
			await(client.createAndLogin());
			var userId = client.getUserEntity().getId();

			// 1. Calling RoutineCaller with userId from a real EventLoop context must not crash with "Cannot be called on a Vert.x event-loop thread"
			var eventLoopDone = Promise.<RogueRun>promise();
			var eventLoopCtx = ((io.vertx.core.internal.VertxInternal) vertx).createEventLoopContext();
			eventLoopCtx.runOnContext(_ -> {
				Environment.callRoutine(Routines.startRogueRun(HeroClass.TEST, 0L))
						.withUserId(userId)
						.execute(RowMappers.getRogueRunMapper())
						.onComplete(eventLoopDone);
			});
			var elRun = await(eventLoopDone.future());
			assertNotNull(elRun.getId(), "RoutineCaller with userId must succeed from EventLoop context");

			// Verify RoutineCaller rolls back cleanly when SET LOCAL role fails inside transaction
			var rollbackFuture = Environment.callRoutine(Routines.startRogueRun(HeroClass.TEST, 0L))
					.withRole("nonexistent_role_for_rollback_test")
					.execute(RowMappers.getRogueRunMapper());
			assertThrows(Exception.class, () -> await(rollbackFuture));

			// 2. Two Rogue runs must have isolated level_X cards when one progresses
			var client2 = new Client(vertx);
			await(client2.createAndLogin());
			var userId2 = client2.getUserEntity().getId();

			var run1 = await(startRogueRun(userId));
			var run2 = await(startRogueRun(userId2));

			await(RogueManager.skipBoss(run1.getId()));

			var run1Cards = await(Environment.callRoutine(Routines.getCardsInDeck(run1.getDeck())).execute(row -> row.getString(0)));
			var run2Cards = await(Environment.callRoutine(Routines.getCardsInDeck(run2.getDeck())).execute(row -> row.getString(0)));
			assertTrue(run1Cards.contains("level_2"), "Run 1 should advance to level_2");
			assertTrue(run2Cards.contains("level_1"), "Run 2 must still have level_1 (not overwritten by Run 1's progressRun)");
			assertFalse(run2Cards.contains("level_2"), "Run 2 must not be corrupted with level_2");

			// 3. upgradeCard must actually replace the card in run1's deck and invalidate the Legacy deck cache
			var upgradableCard = RogueManager.cardCatalogue.getAll().stream()
					.filter(c -> c.hasAttribute(net.demilich.metastone.game.cards.Attribute.UPGRADE))
					.findFirst()
					.orElseThrow();
			var upgradedCardId = (String) upgradableCard.getAttribute(net.demilich.metastone.game.cards.Attribute.UPGRADE);
			await(Environment.callRoutine(Routines.setCardsInDeck(run1.getDeck(), new String[]{upgradableCard.getCardId(), "level_2"}))
					.execute(RowMappers.getCardsInDeckMapper()));
			await(RogueManager.updateRogueRun(run1.getId(), r -> r.set(ROGUE_RUN.GOLD, 20)));
			// Warm Legacy deck cache before upgrading
			await(Legacy.getDeck(RogueManager.cardCatalogue, run1.getDeck(), run1.getPlayer()));

			await(RogueManager.upgradeCard(run1.getId(), upgradableCard.getCardId()));

			var cardsAfterUpgrade = await(Environment.callRoutine(Routines.getCardsInDeck(run1.getDeck())).execute(row -> row.getString(0)));
			assertTrue(cardsAfterUpgrade.contains(upgradedCardId), "Deck must contain the upgraded card ID");
			assertFalse(cardsAfterUpgrade.contains(upgradableCard.getCardId()), "Original un-upgraded card should be replaced");
			var cachedDeckAfterUpgrade = await(Legacy.getDeck(RogueManager.cardCatalogue, run1.getDeck(), run1.getPlayer()));
			assertTrue(cachedDeckAfterUpgrade.getCollection().getInventoryList().stream().anyMatch(c -> upgradedCardId.equals(c.getEntity().getCardId())),
					"Legacy deck cache must be invalidated on upgradeCard");
		});
	}

}
