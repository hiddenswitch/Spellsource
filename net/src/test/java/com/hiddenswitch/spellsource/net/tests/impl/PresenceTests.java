package com.hiddenswitch.spellsource.net.tests.impl;

import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.client.models.PresenceEnum;
import com.hiddenswitch.spellsource.net.Presence;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PresenceTests extends SpellsourceTestBase {

	@Test
	public void testOnlineOfflinePresence(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			try (var user = new UnityClient(context)) {
				invoke0(user::createUserAccount);
				user.ensureConnected();
				assertEquals(PresenceEnum.ONLINE, Presence.presence(user.getUserId()));
				user.disconnect();
				assertEquals(PresenceEnum.ONLINE, Presence.presence(user.getUserId()));
				Strand.sleep(Presence.TIMEOUT_MILLIS + 1);
				assertEquals(PresenceEnum.OFFLINE, Presence.presence(user.getUserId()));
			}
		}, context, vertx);
	}
}
