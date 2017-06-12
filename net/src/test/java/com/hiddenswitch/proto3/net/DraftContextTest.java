package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.proto3.draft.DraftContext;
import com.hiddenswitch.proto3.draft.DraftStatus;
import com.hiddenswitch.proto3.net.client.models.MatchmakingDeck;
import com.hiddenswitch.proto3.net.impl.util.DraftRecord;
import com.hiddenswitch.proto3.net.models.DraftActionRequest;
import com.hiddenswitch.proto3.net.util.AbstractMatchmakingTest;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.utils.Tuple;
import org.junit.Test;

/**
 * Created by bberman on 12/16/16.
 */
public class DraftContextTest extends AbstractMatchmakingTest {
	@Test(timeout = 80000L)
	public void testDraftAndJoin(TestContext context) {
		setLoggingLevel(Level.DEBUG);
		wrapSync(context, this::createTwoPlayersAndMatchmake);
	}

	@Override
	protected Tuple<MatchmakingDeck, Deck> createDeckForMatchmaking(int playerId) {
		setLoggingLevel(Level.ERROR);
		DraftContext context = new DraftContext();
		context.accept(done -> {
		});
		return getTuple(context.getPublicState().createDeck());
	}
}
