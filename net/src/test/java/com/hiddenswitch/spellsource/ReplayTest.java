package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.annotation.JsonInclude;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.util.GameRecord;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.GameContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static org.junit.Assert.assertTrue;

public class ReplayTest extends SpellsourceTestBase {

	@Test
	public void testSmallerDocument(TestContext testContext) {
		GameContext context = GameContext.fromTwoRandomDecks();
		context.play();
		GameRecord record = new GameRecord("local").setReplay(Games.replayFromGameContext(context));
		JsonObject json = JsonObject.mapFrom(record);
		String encoded = json.encode();
		testContext.assertTrue(encoded.length() < 16777216);
	}

	@Test
	public void testReplayMatchesClientData(TestContext context) {
		sync(() -> {
			List<GameState> receivedStates = new ArrayList<>();

			UnityClient player = new UnityClient(context) {
				@Override
				@Suspendable
				protected boolean onRequestAction(ServerToClientMessage message) {
					receivedStates.add(message.getGameState());
					return true;
				}
			};

			player.createUserAccount();
			player.matchmakeQuickPlay(null);
			player.waitUntilDone();
			context.assertTrue(player.getTurnsPlayed() > 0);

			// Sleep to let the replay actually get saved
			Strand.sleep(4000);

			GetGameRecordIdsResponse gameIds = invoke(player.getApi()::getGameRecordIds);
			context.assertEquals(gameIds.getGameIds().size(), 1);
			GetGameRecordResponse gameRecordResponse = invoke(player.getApi()::getGameRecord, gameIds.getGameIds().get(0));
			context.assertNotNull(gameRecordResponse.getReplay());

			/*
			// Check that every state we received was in this response
			Set<GameState> firsts = gameRecordResponse.getReplay().getGameStates().stream().map(ReplayGameStates::getFirst).collect(Collectors.toSet());
			Set<GameState> seconds = gameRecordResponse.getReplay().getGameStates().stream().map(ReplayGameStates::getSecond).collect(Collectors.toSet());

			// TODO: Use the stricter criteria when ready.
			for (GameState receivedState : receivedStates) {
				context.assertTrue(firsts.contains(receivedState) || seconds.contains(receivedState));
			}
			*/
		});
	}
}
