package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.Friend;
import com.hiddenswitch.spellsource.client.models.FriendPutRequest;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static io.vertx.ext.sync.Sync.awaitEvent;

public class PresenceTest extends SpellsourceTestBase {

	@Test
	public void testDoesNotifyPresence(TestContext context) {
		Async async = context.async();
		vertx.exceptionHandler(context.exceptionHandler());
		vertx.runOnContext(v1 -> {
			vertx.runOnContext(Sync.suspendableHandler(v2 -> {
				CreateAccountResponse account1 = createRandomAccount();
				CreateAccountResponse account2 = createRandomAccount();
				WebSocket ws1 = awaitEvent(h -> vertx.createHttpClient().websocket(8080, "localhost", "/realtime?X-Auth-Token=" + account1.getLoginToken().getToken(), h));
				WebSocket ws2 = awaitEvent(h -> vertx.createHttpClient().websocket(8080, "localhost", "/realtime?X-Auth-Token=" + account2.getLoginToken().getToken(), h));
				AtomicBoolean didGetOnline = new AtomicBoolean();
				AtomicBoolean didGetOffline = new AtomicBoolean();
				ws2.handler(buf -> {
					Envelope msg = Json.decodeValue(buf, Envelope.class);

					if (msg.getChanged() != null && msg.getChanged().getFriend() != null) {
						Friend friend = msg.getChanged().getFriend();
						switch (friend.getPresence()) {
							case ONLINE:
								context.assertTrue(didGetOffline.compareAndSet(false, false));
								context.assertTrue(didGetOnline.compareAndSet(false, true));
								break;
							case OFFLINE:
								context.assertTrue(didGetOnline.compareAndSet(true, false));
								context.assertTrue(didGetOffline.compareAndSet(false, true));
								async.complete();
								break;
						}
					}
				});

				Friends.putFriend(Accounts.findOne(account1.getUserId()), new FriendPutRequest().friendId(account2.getUserId()));
				Long tick = awaitEvent(t -> vertx.setTimer(5001L, t));
				ws1.close();
			}));
		});

	}
}
