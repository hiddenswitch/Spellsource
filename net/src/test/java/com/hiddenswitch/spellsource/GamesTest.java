package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.models.ConfigurationRequest;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.behaviour.Behaviour;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class GamesTest extends SpellsourceTestBase {
	private static Logger logger = LoggerFactory.getLogger(GamesTest.class);


	/*
	@Test
	public void testReceivesCorrectMulliganAndActions(TestContext context) {
		Supplier<Behaviour> oldBehaviour = Bots.BEHAVIOUR.getAndSet(() -> {
			// TODO: Return bot that will be used to ensure the right data comes in
		});
		UnityClient client = new UnityClient(context);
		client.createUserAccount();
		sync(()-> {
			Games.createGame(ConfigurationRequest.botMatch(GameId.create(), client.getUserId(), Bots.pollBotId(),));
		});
	}
	*/

	@Test
	public void testReconnectsResumesMulligan(TestContext context) throws InterruptedException {
		AtomicInteger mulligans = new AtomicInteger(0);
		UnityClient client = new UnityClient(context) {
			@Override
			protected void onMulligan(ServerToClientMessage message) {
				super.onMulligan(message);
				mulligans.incrementAndGet();
			}
		};
		client.createUserAccount();
		client.setShouldDisconnect(true);
		client.getTurnsToPlay().set(0);
		client.matchmakeQuickPlay(null);
		client.waitUntilDone();
		Thread.sleep(100L);
		context.assertFalse(client.isConnected());
		sync(() -> {
			Strand.sleep(100L);
			// Game should still be running
			context.assertTrue(Games.getGames().containsKey(new UserId(client.getAccount().getId())));
			Strand.sleep(100L);
		});
		// Reconnect
		client.getTurnsToPlay().set(999);
		client.play();
		client.waitUntilDone();
		context.assertTrue(client.isGameOver());
		context.assertEquals(mulligans.get(), 2);
	}

	@Test
	public void testReconnectsResumesNormalActions(TestContext context) throws InterruptedException {
		AtomicInteger requests = new AtomicInteger();
		List<Integer> actions = new ArrayList<>();
		UnityClient client = new UnityClient(context) {
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
		};

		client.createUserAccount();
		client.matchmakeQuickPlay(null);
		client.waitUntilDone();
		Thread.sleep(100L);
		context.assertFalse(client.isConnected());
		sync(() -> {
			Strand.sleep(100L);
			// Game should still be running
			context.assertTrue(Games.getGames().containsKey(new UserId(client.getAccount().getId())));
			Strand.sleep(100L);
		});
		// Reconnect
		client.play();
		client.waitUntilDone();
		context.assertTrue(client.isGameOver());
	}
}
