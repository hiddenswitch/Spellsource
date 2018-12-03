package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SimultaneousGamesTest extends SpellsourceTestBase {
	private static Logger logger = LoggerFactory.getLogger(SimultaneousGamesTest.class);

	@Test(timeout = 185000L)
	public void testSimultaneousGames(TestContext context) throws InterruptedException, SuspendExecution {
		// Make sure the queues are empty when this starts
		sync(() -> {
			Set<Map.Entry<UserId, String>> queuedUsers = Matchmaking.getUsersInQueues().entrySet();
			Set<Map.Entry<UserId, GameId>> games = Games.getUsersInGames().entrySet();
			assertEquals(queuedUsers.size(), 0);
			assertEquals(games.size(), 0);
		});

		int count = Math.max((Runtime.getRuntime().availableProcessors() / 2 - 1) * 2, 2) * 3;
		int checkpointTotal = count * 6;

		CountDownLatch latch = new CountDownLatch(count);
		List<Thread> threads = new ArrayList<>();
		AtomicInteger checkpoints = new AtomicInteger(0);
		for (int i = 0; i < count; i++) {
			Thread thread = new Thread(() -> {
				try {
					UnityClient client = new UnityClient(context) {
						@Override
						protected int getActionIndex(ServerToClientMessage message) {
							// Always return end turn so that we end the game in a fatigue duel
							if (message.getActions().getEndTurn() != null) {
								return message.getActions().getEndTurn();
							} else {
								return super.getActionIndex(message);
							}
						}
					};

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
				} catch (ApiException t) {
					context.exceptionHandler().handle(t);
				} catch (Throwable any) {
					context.fail(any);
				}
			});
			thread.start();
			thread.setUncaughtExceptionHandler((eh, ex) -> {
				ExceptionUtils.printRootCauseStackTrace(ex);
			});
			threads.add(thread);
		}

		// Random games can take quite a long time to finish so be patient...
		latch.await(185L, TimeUnit.SECONDS);
		assertEquals(0L, latch.getCount());
		for (Thread thread : threads) {
			if (thread.isAlive()) {
				thread.interrupt();
			}
		}
	}
}
