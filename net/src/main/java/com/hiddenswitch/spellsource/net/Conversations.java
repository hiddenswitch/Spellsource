package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMultimap;
import com.hiddenswitch.spellsource.net.impl.AddedChangedRemoved;
import com.hiddenswitch.spellsource.net.impl.ConversationId;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import io.vertx.core.Future;
import org.apache.commons.lang3.RandomStringUtils;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;

/**
 * Provides the methods to help users message each other.
 */
public interface Conversations {

	/**
	 * Creates the ephemeral messaging state and notifies users when they receive messages.
	 */
	@Suspendable
	static void handleConnections() {
		SuspendableMultimap<ConversationId, ChatMessage> conversations = SuspendableMultimap.getOrCreate("conversations");

		Connection.connected((connection, fut) -> {
			connection.handler(fiber(msg -> {
				// Send a message
				if (msg.getMethod() != null && msg.getMethod().getSendMessage() != null) {
					var sendMessage = msg.getMethod().getSendMessage();
					// Sending a chat message
					var sender = mongo().findOne(Accounts.USERS, json("_id", connection.userId()), UserRecord.class);
					var conversationId = sendMessage.getConversationId();
					if (!conversationId.contains(connection.userId())) {
						throw new SecurityException(String.format("User %s attempted to subscribe to unauthorized conversationId %s",
								connection.userId(),
								conversationId));
					}
					// Conversation IDs should be of the form userId1,userId2
					// TODO: Assert that it's two valid user IDs.
					var message = new ChatMessage()
							.messageId("c:" + Integer.toString(conversations.size()) + ":" + RandomStringUtils.randomAlphanumeric(6))
							.conversationId(conversationId)
							.message(sendMessage.getMessage())
							.senderUserId(sender.getId())
							.senderName(sender.getUsername());

					conversations.put(new ConversationId(conversationId), message);
					connection.write(new Envelope().result(new EnvelopeResult().sendMessage(new EnvelopeResultSendMessage().messageId(message.getMessageId()))));
				}

				if (msg.getSub() != null && msg.getSub().getConversation() != null) {
					// Subscribe to conversation
					var request = msg.getSub().getConversation();
					var conversationId = request.getConversationId();
					var key = new ConversationId(conversationId);

					AddedChangedRemoved<ConversationId, ChatMessage> observer =
							SuspendableMultimap.subscribeToKeyInMultimap("conversations", key);

					for (var message : conversations.get(key)) {
						connection.write(new Envelope().added(new EnvelopeAdded().chatMessage(message)));
					}

					var sub = observer.added().subscribe(next -> {
						connection.write(new Envelope().added(new EnvelopeAdded().chatMessage(next.getValue())));
					});

					connection.addCloseHandler(v2 -> {
						sub.dispose();
						observer.dispose();
						v2.complete();
					});
				}
			}));

			fut.handle(Future.succeededFuture());
		});
	}
}