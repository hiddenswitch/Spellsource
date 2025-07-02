package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.schema.spellsource.enums.RogueRunState;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

import static io.vertx.await.Async.await;
import static org.junit.jupiter.api.Assertions.*;

public class RogueRunTests extends FrameworkTestBase {


	@Test
	public void testStartRogueRun(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {

			var client = new Client(vertx);
			await(startGateway(vertx));
			await(client.createAndLogin());
			var userId = client.getUserEntity().getId();

			var jooq = Environment.jooqAkaDaoConfiguration();

			var setUserId = jooq.dsl().setLocal(DSL.name("user.id"), DSL.value(userId));
			var setRole = jooq.dsl().setLocal(DSL.name("role"), DSL.value("website"));

			jooq.dsl().execute(setUserId + ";" + setRole + ";");

			var rogueRun = Routines.startRogueRun(jooq, HeroClass.TEST, 0L);

			assertEquals(0L, (long) rogueRun.getSeed());
			assertEquals(userId, rogueRun.getPlayer());
			assertEquals(RogueRunState.INITIAL, rogueRun.getState());

			// Make sure RogueManager has been notified
			await(Environment.sleep(100));

			var deck = await(client.legacy().decksGet(Spellsource.DecksGetRequest.newBuilder()
					.setDeckId(rogueRun.getDeck())
					.build()));

			assertEquals(HeroClass.TEST, deck.getCollection().getHeroClass());
			assertTrue(deck.getCollection().getInventoryCount() > 0);

			jooq.dsl().execute("reset all");
		});
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
