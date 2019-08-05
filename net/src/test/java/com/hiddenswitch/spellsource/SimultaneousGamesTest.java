package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.vertx.ext.unit.TestContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class SimultaneousGamesTest extends SpellsourceTestBase {
	private static Logger logger = LoggerFactory.getLogger(SimultaneousGamesTest.class);

	@Test(timeout = 400000L)
	public void testSimultaneousGames(TestContext context) throws InterruptedException, SuspendExecution {
		Tracer tracer = Tracing.initialize("test");
		Span span = tracer.buildSpan("testSimultaneousGames").start();
		// Make sure the queues are empty when this starts
		sync(() -> {
			Set<Map.Entry<UserId, String>> queuedUsers = Matchmaking.getUsersInQueues().entrySet();
			Set<Map.Entry<UserId, GameId>> games = Games.getUsersInGames().entrySet();
			context.assertEquals(queuedUsers.size(), 0);
			context.assertEquals(games.size(), 0);
		}, 4, context);

		Thread testThread = Thread.currentThread();
		int count = Math.max((Runtime.getRuntime().availableProcessors() / 2 - 1) * 2, 2) * 3;
		int checkpointTotal = count * 6;

		CountDownLatch latch = new CountDownLatch(count);
		List<Thread> threads = new ArrayList<>();
		AtomicInteger checkpoints = new AtomicInteger(0);
		for (int i = 0; i < count; i++) {
			Thread thread = new Thread(() -> {
				try (UnityClient client = new UnityClient(context) {
					@Override
					protected int getActionIndex(ServerToClientMessage message) {
						// Always return end turn so that we end the game in a fatigue duel
						if (message.getActions().getEndTurn() != null) {
							return message.getActions().getEndTurn();
						} else {
							return super.getActionIndex(message);
						}
					}
				}) {
					client.createUserAccount(null);
					String userId = client.getAccount().getId();
					logger.trace("testSimultaneousGames: {} 1st Matchmaking on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.matchmakeConstructedPlay(null);
					logger.trace("testSimultaneousGames: {} 1st Starts on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.waitUntilDone();
					context.assertTrue(client.getTurnsPlayed() > 0);
					context.assertTrue(client.isGameOver());
					context.assertFalse(client.getApi().getAccount(userId).getAccounts().get(0).isInMatch());
					logger.trace("testSimultaneousGames: {} 1st Finished {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);

					// Try two games in a row
					logger.trace("testSimultaneousGames: {} 2nd Matchmaking on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.matchmakeConstructedPlay(null);
					logger.trace("testSimultaneousGames: {} 2nd Starts on {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					client.waitUntilDone();
					context.assertTrue(client.getTurnsPlayed() > 0);
					context.assertTrue(client.isGameOver());
					context.assertFalse(client.getApi().getAccount(userId).getAccounts().get(0).isInMatch());
					logger.trace("testSimultaneousGames: {} 2nd Finished {}/{} checkpoints", userId, checkpoints.incrementAndGet(), checkpointTotal);
					logger.info("testSimultaneousGames: {} finished", userId);
					latch.countDown();
				} catch (Throwable any) {
					context.fail(any);
				}
			});
			thread.start();
			thread.setUncaughtExceptionHandler((eh, ex) -> {
				Tracing.error(ex, span, false);
				testThread.interrupt();
			});
			threads.add(thread);
		}

		// Random games can take quite a long time to finish so be patient...
		try {
			latch.await(400L, TimeUnit.SECONDS);
		} catch (InterruptedException interrupted) {
			context.fail(interrupted);
		} finally {
			span.finish();
		}
		context.assertEquals(0L, latch.getCount());
		for (Thread thread : threads) {
			if (thread.isAlive()) {
				thread.interrupt();
			}
		}
	}
}
