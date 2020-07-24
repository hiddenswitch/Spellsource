package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.net.impl.GameId;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.impl.TimerId;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.impl.server.Configuration;
import com.hiddenswitch.spellsource.net.impl.util.Scheduler;
import com.hiddenswitch.spellsource.net.impl.util.ServerGameContext;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.decks.Deck;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static net.demilich.metastone.tests.util.TestBase.assertThrows;

public class ServerGameContextTest extends SpellsourceTestBase {
	private static class TestScheduler implements Scheduler {
		AtomicLong id = new AtomicLong();

		public TestScheduler() {

		}

		@Override
		public TimerId setTimer(long delay, Handler<Long> handler) {
			return new TimerId(id.getAndIncrement());
		}

		@Override
		public TimerId setInterval(long delay, Handler<Long> handler) {
			return new TimerId(id.getAndIncrement());
		}

		@Override
		public boolean cancelTimer(TimerId id) {
			return true;
		}
	}

	@Test
	public void testConstructor(Vertx vertx, VertxTestContext context) {
		vertx.runOnContext(v -> {
			verify(context, () -> {
				Configuration configuration1 = new Configuration()
						.setBot(true)
						.setDeck(Deck.randomDeck())
						.setName("bot name")
						.setPlayerId(0)
						.setUserId(new UserId("1"));

				Configuration configuration2 = new Configuration()
						.setBot(false)
						.setDeck(Deck.randomDeck())
						.setName("human name")
						.setPlayerId(1)
						.setUserId(new UserId("2"));

				List<Configuration> playerConfigurations = Arrays.asList(configuration1, configuration2);

				ServerGameContext context1 = new ServerGameContext(new GameId("test"), new TestScheduler(), playerConfigurations);

				assertThrows(IllegalArgumentException.class, () -> {
					new ServerGameContext(new GameId("test"), new TestScheduler(), Collections.singletonList(configuration1));
				});

				assertThrows(IllegalArgumentException.class, () -> {
					new ServerGameContext(new GameId("test"), new TestScheduler(), Collections.singletonList(configuration2));
				});
			});
			context.completeNow();
		});
	}
}
