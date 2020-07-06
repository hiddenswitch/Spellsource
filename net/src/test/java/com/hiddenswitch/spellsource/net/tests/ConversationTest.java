package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static com.hiddenswitch.spellsource.net.impl.Sync.invoke0;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConversationTest extends SpellsourceTestBase {

	private static Logger LOGGER = LoggerFactory.getLogger(ConversationTest.class);

	@Test
	public void testConversationRealtime(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var user1 = createRandomAccount();
			var user2 = createRandomAccount();

			var conversationId = user1.getUserId() + "," + user2.getUserId();
			var latch = new CountDownLatch(1);

			var client1 = new UnityClient(context, user1.getLoginToken().getToken()) {
				AtomicInteger messageCount = new AtomicInteger();

				@Override
				protected void handleMessage(Envelope env) {
					if (messageCount.getAndIncrement() == 0) {
						// Subscribe
						sendMessage(
								new Envelope().sub(new EnvelopeSub().conversation(new EnvelopeSubConversation().conversationId(conversationId))));
						LOGGER.debug("user1 handler: Subscribed");

						// Send message
						sendMessage(
								new Envelope().method(new EnvelopeMethod().sendMessage(new EnvelopeMethodSendMessage().conversationId(conversationId).message("hello"))));
						LOGGER.debug("user1 handler: Sent message");
					}
				}
			};

			var client2 = new UnityClient(context, user2.getLoginToken().getToken()) {
				AtomicInteger messageCount = new AtomicInteger();

				@Override
				protected void handleMessage(Envelope env) {
					if (messageCount.getAndIncrement() == 0) {
						// Subscribe
						sendMessage(new Envelope().sub(new EnvelopeSub().conversation(new EnvelopeSubConversation().conversationId(conversationId))));
						LOGGER.debug("user2 handler: Subscribed");
					}
					if (env.getAdded() != null && env.getAdded().getChatMessage() != null) {
						context.verify(() -> {
							assertEquals("hello", env.getAdded().getChatMessage().getMessage());
							LOGGER.debug("user2 handler: Received message");
							latch.countDown();
						});

					}
				}
			};
			invoke0(client1::ensureConnected);
			invoke0(client2::ensureConnected);
			try {
				latch.await();
			} finally {
				assertEquals(0L, latch.getCount());
			}
		}, context, vertx);
	}
}
