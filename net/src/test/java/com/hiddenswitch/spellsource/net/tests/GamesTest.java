package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.net.Games;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;
import static org.junit.jupiter.api.Assertions.*;

public class GamesTest extends SpellsourceTestBase {

	@Test
	@Timeout(24000)
	public void testReconnectsResumesMulligan(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
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
				assertFalse(client.isConnected());
				assertTrue(Games.isInGame(new UserId(client.getAccount().getId())));
				// Reconnect
				client.getTurnsToPlay().set(999);
				client.play();
				invoke0(client::waitUntilDone);
				assertTrue(client.getTurnsPlayed() > 0);
				assertTrue(client.isGameOver());
				assertEquals(mulligans.get(), 1);
			}
		}, context, vertx);

	}

	@Test
	@Timeout(15000)
	public void testReconnectsResumesNormalActions(Vertx vertx, VertxTestContext context) throws InterruptedException {
		AtomicInteger requests = new AtomicInteger();
		List<Integer> actions = new ArrayList<>();
		runOnFiberContext(() -> {
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
						assertEquals(message.getActions().getCompatibility().size(), actions.size());
					}

					return true;
				}
			}) {
				invoke0(client::createUserAccount);
				invoke0(client::matchmakeQuickPlay, null);
				invoke0(client::waitUntilDone);
				Strand.sleep(100L);
				assertFalse(client.isConnected());
				Strand.sleep(100L);
				// Game should still be running
				assertTrue(Games.isInGame(new UserId(client.getAccount().getId())));
				Strand.sleep(100L);
				// Reconnect
				invoke0(client::play);
				invoke0(client::waitUntilDone);
				assertTrue(client.getTurnsPlayed() > 0);
				assertTrue(client.isGameOver());
			}
		}, context, vertx);
	}
}
