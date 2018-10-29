package com.hiddenswitch.spellsource;

import com.github.fromage.quasi.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.behaviour.PlayRandomBehaviour;
import net.demilich.metastone.game.shared.threat.GameStateValueBehaviour;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MemoryStressTest extends SpellsourceTestBase {
	@Test
	@Ignore("does not consistently reproduce out of memory errors")
	public void testOutOfMemoryError(TestContext context) throws InterruptedException, SuspendExecution {
		// Repeatedly run games until OOM occurs (guessing 240 games)
		// This was resolved by setting the BaseMap base class to HashMap instead of EnumMap, and reducing the depth of
		// game state value behaviour to 2.
		try {
			Bots.BEHAVIOUR.set(GameStateValueBehaviour::new);
			for (int i = 0; i < 240; i++) {
				UnityClient client = new UnityClient(context);
				client.createUserAccount(null);
				client.matchmakeQuickPlay(null);
				client.waitUntilDone();
				assertTrue(client.isGameOver());
			}
		} finally {
			Bots.BEHAVIOUR.set(PlayRandomBehaviour::new);
		}
	}
}
