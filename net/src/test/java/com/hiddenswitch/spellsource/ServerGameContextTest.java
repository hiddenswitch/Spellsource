package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.client.models.ClientToServerMessage;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.common.Server;
import com.hiddenswitch.spellsource.common.UnityClientBehaviour;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.TimerId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.server.Configuration;
import com.hiddenswitch.spellsource.impl.util.Scheduler;
import com.hiddenswitch.spellsource.impl.util.ServerGameContext;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.test.fakestream.FakeStream;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static net.demilich.metastone.tests.util.TestBase.assertThrows;
import static org.junit.Assert.*;

@PrepareForTest(Server.class)
@PowerMockRunnerDelegate(VertxUnitRunner.class)
public class ServerGameContextTest extends SpellsourceTestBase {

	@BeforeClass
	public static void startUp() {
		CardCatalogue.loadCardsFromPackage();
	}

	private static class TestScheduler implements Scheduler {
		AtomicLong id = new AtomicLong();

		public TestScheduler() {

		}

		@Override
		public TimerId setTimer(long delay, Handler<Long> handler) {
			return new TimerId(id.getAndIncrement());
		}

		@Override
		public boolean cancelTimer(TimerId id) {
			return true;
		}
	}

	@Test
	public void testConstructor(TestContext context) {
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

		Async async = context.async();
		vertx.runOnContext(v -> {
			ServerGameContext context1 = new ServerGameContext(new GameId("test"), new TestScheduler(), playerConfigurations);

			assertThrows(IllegalArgumentException.class, () -> {
				new ServerGameContext(new GameId("test"), new TestScheduler(), Collections.singletonList(configuration1));
			});

			assertThrows(IllegalArgumentException.class, () -> {
				new ServerGameContext(new GameId("test"), new TestScheduler(), Collections.singletonList(configuration2));
			});

			assertThrows(IllegalArgumentException.class, () -> {
				new ServerGameContext(null, new TestScheduler(), playerConfigurations);
			});

			async.complete();
		});

		async.awaitSuccess();
	}

	@Test
	public void testUnityClientBehaviourConstructor(TestContext context) {
		Server mockServer = PowerMockito.mock(Server.class);
		Scheduler testScheduler = new TestScheduler();
		FakeStream<ClientToServerMessage> reader = new FakeStream<>();
		FakeStream<ServerToClientMessage> writer = new FakeStream<>();

		assertThrows(IllegalStateException.class, () -> {
			UnityClientBehaviour unityClientBehaviour1 = new UnityClientBehaviour(mockServer, testScheduler, reader, writer, new UserId("1"), 0, 0L);
		});


		Async async = context.async(1);
		vertx.runOnContext(v -> {
			context.verify(v2 -> {
				UnityClientBehaviour unityClientBehaviour2 = new UnityClientBehaviour(mockServer, testScheduler, reader, writer, new UserId("1"), 0, 0L);
				assertNotNull(unityClientBehaviour2);

				UnityClientBehaviour unityClientBehaviour3 = new UnityClientBehaviour(mockServer, testScheduler, reader, writer, new UserId("1"), 0, 1L);
				assertNotNull(unityClientBehaviour3);

				assertThrows(IllegalArgumentException.class, () -> {
					UnityClientBehaviour unityClientBehaviour4 = new UnityClientBehaviour(mockServer, testScheduler, reader, writer, new UserId("1"), 0, -1L);
				});

				async.countDown();
			});
		});

		async.awaitSuccess();
	}
}
