package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.SpellsourceAuthHandler;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static io.vertx.core.json.Json.decodeValue;
import static io.vertx.core.json.Json.encodeToBuffer;

public class ConversationTest extends SpellsourceTestBase {

	@Test
	public void testConversationRealtime(TestContext context) {
		Async async = context.async();
		sync(() -> {

			CreateAccountResponse user1 = createRandomAccount();
			CreateAccountResponse user2 = createRandomAccount();

			// Deployed. Subscribe to conversation
			// User 1 client
			final HttpClientOptions options = new HttpClientOptions().setDefaultPort(8080).setDefaultHost("localhost");
			final String conversationId = user1.getUserId() + "," + user2.getUserId();
			vertx.createHttpClient(options).websocket("/realtime?X-Auth-Token=" + user1.getLoginToken().getToken(), handler -> {
				// Subscribe
				handler.write(encodeToBuffer(
						new Envelope().sub(new EnvelopeSub().conversation(new EnvelopeSubConversation().conversationId(conversationId)))));

				// Send message
				handler.write(encodeToBuffer(
						new Envelope().method(new EnvelopeMethod().sendMessage(new EnvelopeMethodSendMessage().conversationId(conversationId).message("hello")))));
			});

			// User 2 client
			vertx.createHttpClient(options).websocket("/realtime?X-Auth-Token=" + user2.getLoginToken().getToken(), handler -> {
				handler.handler(incoming -> {
					Envelope envelope = decodeValue(incoming, Envelope.class);
					context.assertEquals(envelope.getAdded().getChatMessage().getMessage(), "hello");
					async.complete();
				});
				// Subscribe
				handler.write(encodeToBuffer(new Envelope().sub(new EnvelopeSub().conversation(new EnvelopeSubConversation().conversationId(conversationId)))));
			});
		});
	}
}
