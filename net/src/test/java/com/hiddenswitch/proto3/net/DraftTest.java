package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.proto3.draft.DraftContext;
import com.hiddenswitch.proto3.net.util.AbstractMatchmakingTest;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.utils.Tuple;
import org.junit.Test;

/**
 * Created by bberman on 12/16/16.
 */
public class DraftTest extends AbstractMatchmakingTest {
	@Test(timeout = 80000L)
	public void testDraftAndJoin(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, this::createTwoPlayersAndMatchmake);
	}

	@Override
	protected Tuple<com.hiddenswitch.proto3.net.client.models.Deck, Deck> createDeckForMatchmaking(int playerId) {
		setLoggingLevel(Level.ERROR);
		DraftContext context = new DraftContext();
		context.accept(done -> {
		});
		return getTuple(context.getPublicState().createDeck());
	}
}
