package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.models.GameState;
import com.hiddenswitch.spellsource.client.models.GetGameRecordIdsResponse;
import com.hiddenswitch.spellsource.client.models.GetGameRecordResponse;
import com.hiddenswitch.spellsource.client.models.ServerToClientMessage;
import com.hiddenswitch.spellsource.net.Games;
import com.hiddenswitch.spellsource.net.impl.util.GameRecord;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.GameContext;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.hiddenswitch.spellsource.net.impl.Sync.invoke;
import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;

public class ReplayTest extends SpellsourceTestBase {

	@Test
	public void testSmallerDocument(TestContext testContext) {
		GameContext context = GameContext.fromTwoRandomDecks();
		context.play();
		GameRecord record = new GameRecord("local")
				.setTrace(context.getTrace().clone())
				.setReplay(Games.replayFromGameContext(context));
		JsonObject json = JsonObject.mapFrom(record);
		String encoded = json.encode();
		testContext.assertTrue(encoded.length() < 16777216);
	}

	@Test
	@Ignore("still pretty flakey")
	public void testReplayMatchesClientData(TestContext context) {
		sync(() -> {
			List<GameState> receivedStates = new ArrayList<>();

			try (UnityClient player = new UnityClient(context) {
				@Override
				@Suspendable
				protected boolean onRequestAction(ServerToClientMessage message) {
					receivedStates.add(message.getGameState());
					return true;
				}
			}) {
				invoke0(player::createUserAccount);
				player.matchmakeQuickPlay(null);
				invoke0(player::waitUntilDone);
				context.assertTrue(player.getTurnsPlayed() > 0);
				Strand.sleep(100);
				GetGameRecordIdsResponse gameIds = invoke(player.getApi()::getGameRecordIds);
				context.assertEquals(1, gameIds.getGameIds().size(), "should have saved game");
				GetGameRecordResponse gameRecordResponse = invoke(player.getApi()::getGameRecord, gameIds.getGameIds().get(0));
				context.assertNotNull(gameRecordResponse.getReplay());
			}
		}, context);
	}
}
