package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.Sync.invoke;

public class ReplayTest extends SpellsourceTestBase {

	@Test
	public void testReplayMatchesClientData(TestContext context) {
		sync(() -> {
			List<GameState> receivedStates = new ArrayList<>();

			UnityClient player = new UnityClient(context) {
				@Override
				protected boolean onRequestAction(ServerToClientMessage message) {
					receivedStates.add(message.getGameState());
					return true;
				}
			};

			player.createUserAccount();
			player.matchmakeQuickPlay(null);
			player.waitUntilDone();

			GetGameRecordIdsResponse gameIds = invoke(player.getApi()::getGameRecordIds);
			context.assertEquals(gameIds.getGameIds().size(), 1);
			GetGameRecordResponse gameRecordResponse = invoke(player.getApi()::getGameRecord, gameIds.getGameIds().get(0));

			// Check that every state we received was in this response
			Set<GameState> firsts = gameRecordResponse.getReplay().getGameStates().stream().map(GameStatePair::getFirst).collect(Collectors.toSet());
			Set<GameState> seconds = gameRecordResponse.getReplay().getGameStates().stream().map(GameStatePair::getSecond).collect(Collectors.toSet());
			for (GameState receivedState : receivedStates) {
				context.assertTrue(firsts.contains(receivedState) || seconds.contains(receivedState));
			}
		});
	}
}
