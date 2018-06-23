package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import static io.vertx.core.json.Json.decodeValue;

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
				handler.write(Buffer.buffer(Json.encode(
						new Envelope().sub(new EnvelopeSub().conversation(new EnvelopeSubConversation().conversationId(conversationId))))));

				// Send message
				handler.write(Buffer.buffer(Json.encode(
						new Envelope().method(new EnvelopeMethod().sendMessage(new EnvelopeMethodSendMessage().conversationId(conversationId).message("hello"))))));
			});

			// User 2 client
			vertx.createHttpClient(options).websocket("/realtime?X-Auth-Token=" + user2.getLoginToken().getToken(), handler -> {
				handler.handler(incoming -> {
					Envelope envelope = decodeValue(incoming, Envelope.class);
					context.assertEquals(envelope.getAdded().getChatMessage().getMessage(), "hello");
					async.complete();
				});
				// Subscribe
				handler.write(Buffer.buffer(Json.encode(new Envelope().sub(new EnvelopeSub().conversation(new EnvelopeSubConversation().conversationId(conversationId))))));
			});
		});
	}
}
