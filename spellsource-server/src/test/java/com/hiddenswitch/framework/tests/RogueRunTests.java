package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.impl.RogueManager;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.schema.spellsource.enums.RogueRunState;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.RogueRun;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

import static com.hiddenswitch.framework.schema.spellsource.Tables.ROGUE_RUN;
import static io.vertx.await.Async.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RogueRunTests extends FrameworkTestBase {


	@Test
	public void testStartRogueRun(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {
			var client = new Client(vertx);
			await(startGateway(vertx));
			await(client.createAndLogin());
			var userId = client.getUserEntity().getId();

			var rogueRun = await(startRogueRun(userId));

			assertEquals(0L, (long) rogueRun.getSeed(), "Seed should be 0");
			assertEquals(userId, rogueRun.getPlayer(), "UserId should match");
			assertEquals(RogueRunState.PRE_MATCH, rogueRun.getState(), "Should have pre match state");

			var deck = await(client.legacy().decksGet(Spellsource.DecksGetRequest.newBuilder()
					.setDeckId(rogueRun.getDeck())
					.build()));

			assertEquals(HeroClass.TEST, deck.getCollection().getHeroClass(), "Deck hero class should be correct");
			assertTrue(deck.getCollection().getInventoryCount() > 0, "Deck should have cards");
		});
	}

	@Test
	public void testRogueRunMatch(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {
			var client = new Client(vertx);
			await(startGateway(vertx));
			await(client.createAndLogin());
			var userId = client.getUserEntity().getId();

			var rogueRun = await(startRogueRun(userId));

		});
	}

	private Future<RogueRun> startRogueRun(String userId) {
		var jooq = Environment.jooqAkaDaoConfiguration();

		var setUserId = jooq.dsl().setLocal(DSL.name("user.id"), DSL.value(userId));
		var setRole = jooq.dsl().setLocal(DSL.name("role"), DSL.value("website"));
		var startRogueRun = jooq.dsl().select(Routines.startRogueRun(HeroClass.TEST, 0L));

		var sqlStatement = setUserId.getSQL(ParamType.INLINED) + ";" + setRole.getSQL(ParamType.INLINED) + ";" + startRogueRun.getSQL(ParamType.INLINED) + ";";

		await(Environment.sqlClient().query(sqlStatement).execute());

		// Make sure RogueManager has been notified
		await(Environment.sleep(200));

		return RogueManager.returningRogueRun(dsl -> dsl.selectFrom(ROGUE_RUN).limit(1));
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
