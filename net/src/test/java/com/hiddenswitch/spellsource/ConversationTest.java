package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ConversationTest extends SpellsourceTestBase {

	private static Logger LOGGER = LoggerFactory.getLogger(ConversationTest.class);

	@Test
	public void testConversationRealtime(TestContext context) {
		sync(() -> {

			CreateAccountResponse user1 = createRandomAccount();
			CreateAccountResponse user2 = createRandomAccount();

			final String conversationId = user1.getUserId() + "," + user2.getUserId();
			CountDownLatch latch = new CountDownLatch(1);

			UnityClient client1 = new UnityClient(context, user1.getLoginToken().getToken()) {
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

			UnityClient client2 = new UnityClient(context, user2.getLoginToken().getToken()) {
				AtomicInteger messageCount = new AtomicInteger();

				@Override
				protected void handleMessage(Envelope env) {
					if (messageCount.getAndIncrement() == 0) {
						// Subscribe
						sendMessage(new Envelope().sub(new EnvelopeSub().conversation(new EnvelopeSubConversation().conversationId(conversationId))));
						LOGGER.debug("user2 handler: Subscribed");
					}
					if (env.getAdded() != null && env.getAdded().getChatMessage() != null) {
						context.assertEquals(env.getAdded().getChatMessage().getMessage(), "hello");
						LOGGER.debug("user2 handler: Received message");
						latch.countDown();
					}
				}
			};
			client1.ensureConnected();
			client2.ensureConnected();
			try {
				latch.await();
			} finally {
				context.assertEquals(latch.getCount(), 0L);
			}
		});
	}
}
