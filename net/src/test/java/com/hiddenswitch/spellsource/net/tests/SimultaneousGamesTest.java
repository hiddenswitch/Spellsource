package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.ActionType;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.client.models.SpellAction;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class SimultaneousGamesTest extends SpellsourceTestBase {
	private static Logger LOGGER = LoggerFactory.getLogger(SimultaneousGamesTest.class);

	@Test(timeout = 80000L)
	public void testSimultaneousGames(TestContext context) throws InterruptedException, SuspendExecution {
		Tracer tracer = Tracing.initialize("test");
		Span span = tracer.buildSpan("testSimultaneousGames").start();
		int count = Math.max((Runtime.getRuntime().availableProcessors() / 2 - 1) * 2, 2) * 3;
		int checkpointTotal = count * 6;

		Vertx vertx = contextRule.vertx();
		WorkerExecutor executor = vertx.createSharedWorkerExecutor("simultaneousGames", count, 80000L, TimeUnit.MILLISECONDS);
		AtomicInteger checkpoints = new AtomicInteger(0);
		for (int i = 0; i < count; i++) {
			executor.executeBlocking((fut) -> {
				try (UnityClient client = new UnityClient(context) {
					@Override
					protected int getActionIndex(ServerToClientMessage message) {
						// Always return end turn so that we end the game in a fatigue duel
						Optional<SpellAction> endTurn = message.getActions().getAll().stream()
								.filter(ga -> ga.getActionType().equals(ActionType.END_TURN)).findFirst();
						if (endTurn.isPresent()) {
							return endTurn.get().getAction();
						}
						return super.getActionIndex(message);
					}
				}) {
					client.createUserAccount(null);
					String userId = client.getAccount().getId();
					LOGGER.trace("testSimultaneousGames: {} 1st Matchmaking on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.matchmakeConstructedPlay(null);
					LOGGER.trace("testSimultaneousGames: {} 1st Starts on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.waitUntilDone();
					context.assertTrue(client.getTurnsPlayed() > 0);
					context.assertTrue(client.isGameOver());
					LOGGER.trace("testSimultaneousGames: {} 1st Finished {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);

					// Try two games in a row
					LOGGER.trace("testSimultaneousGames: {} 2nd Matchmaking on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.matchmakeConstructedPlay(null);
					LOGGER.trace("testSimultaneousGames: {} 2nd Starts on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.waitUntilDone();
					context.assertTrue(client.getTurnsPlayed() > 0);
					context.assertTrue(client.isGameOver());
					LOGGER.trace("testSimultaneousGames: {} 2nd Finished {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					LOGGER.info("testSimultaneousGames: {} finished", userId);

					fut.complete();
				} catch (Throwable any) {
					Tracing.error(any, span, true);
					fut.fail(any);
				}
			}, false, context.asyncAssertSuccess());
		}
	}
}
