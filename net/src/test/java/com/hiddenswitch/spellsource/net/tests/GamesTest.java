package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.net.Games;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hiddenswitch.spellsource.net.impl.Sync.invoke;
import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;

public class GamesTest extends SpellsourceTestBase {

	@Test(timeout = 15000L)
	public void testReconnectsResumesMulligan(TestContext context) {
		sync(() -> {
			AtomicInteger mulligans = new AtomicInteger(0);
			try (UnityClient client = new UnityClient(context) {
				@Override
				protected void onMulligan(ServerToClientMessage message) {
					super.onMulligan(message);
					mulligans.incrementAndGet();
				}
			}) {
				invoke0(client::createUserAccount);
				client.setShouldDisconnect(true);
				client.getTurnsToPlay().set(0);
				invoke0(client::matchmakeQuickPlay, null);
				invoke0(client::waitUntilDone);
				Strand.sleep(100L);
				context.assertFalse(client.isConnected());
//				Strand.sleep(100L);
				// Game should still be running
				context.assertTrue(Games.getUsersInGames().containsKey(new UserId(client.getAccount().getId())));
//				Strand.sleep(100L);
				// Reconnect
				client.getTurnsToPlay().set(999);
				client.play();
				invoke0(client::waitUntilDone);
				context.assertTrue(client.getTurnsPlayed() > 0);
				context.assertTrue(client.isGameOver());
				context.assertEquals(mulligans.get(), 1);
			}
		}, context);

	}

	@Test(timeout = 15000L)
	public void testReconnectsResumesNormalActions(TestContext context) throws InterruptedException {
		AtomicInteger requests = new AtomicInteger();
		List<Integer> actions = new ArrayList<>();
		sync(() -> {
			try (UnityClient client = new UnityClient(context) {
				@Override
				protected boolean onRequestAction(ServerToClientMessage message) {
					int reqs = requests.getAndIncrement();
					if (reqs == 0) {
						actions.addAll(message.getActions().getCompatibility());
						gameOverLatch.countDown();
						disconnect();
						return false;
					} else if (reqs == 1) {
						context.assertEquals(message.getActions().getCompatibility().size(), actions.size());
					}

					return true;
				}
			}) {
				invoke0(client::createUserAccount);
				invoke0(client::matchmakeQuickPlay, null);
				invoke0(client::waitUntilDone);
				Strand.sleep(100L);
				context.assertFalse(client.isConnected());
				Strand.sleep(100L);
				// Game should still be running
				context.assertTrue(Games.getUsersInGames().containsKey(new UserId(client.getAccount().getId())));
				Strand.sleep(100L);
				// Reconnect
				invoke0(client::play);
				invoke0(client::waitUntilDone);
				context.assertTrue(client.getTurnsPlayed() > 0);
				context.assertTrue(client.isGameOver());
			}
		}, context);
	}
}
