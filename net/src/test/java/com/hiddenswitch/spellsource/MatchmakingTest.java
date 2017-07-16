package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.models.MatchExpireRequest;
import com.hiddenswitch.spellsource.util.AbstractMatchmakingTest;
import com.hiddenswitch.spellsource.impl.ServiceTest;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(VertxUnitRunner.class)
public class MatchmakingTest extends AbstractMatchmakingTest {
	@Test()
	public void testMatchmakeAndJoin(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::createTwoPlayersAndMatchmake);
	}

	@Test()
	public void testMatchmakeSamePlayersTwice(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			// Creates the same two players
			String gameId = createTwoPlayersAndMatchmake();
			Strand.sleep(1000L);
			ServiceTest.getContext().assertNull(gameSessions.getGameSession(gameId));
			final MatchExpireRequest request = new MatchExpireRequest(gameId);
			ServiceTest.getContext().assertFalse(service.expireOrEndMatch(request).expired, "We should fail to expire an already expired match.");
			createTwoPlayersAndMatchmake();
		});

	}

}
