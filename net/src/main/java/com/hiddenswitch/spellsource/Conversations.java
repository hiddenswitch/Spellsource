package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.util.*;
import io.reactivex.disposables.Disposable;
import org.apache.commons.lang3.RandomStringUtils;

import static io.vertx.core.json.JsonObject.mapFrom;

public class Conversations {

	public static void handleConnections() throws SuspendExecution {
		SuspendableMultimap<ConversationId, ChatMessage> conversations = SuspendableMultimap.getOrCreate("conversations");

		Connection.connected(connection -> {
			connection.handler(Sync.suspendableHandler(msg -> {
				// Send a message
				if (msg.getMethod() != null && msg.getMethod().getSendMessage() != null) {
					EnvelopeMethodSendMessage sendMessage = msg.getMethod().getSendMessage();
					// Sending a chat message
					UserRecord sender = Accounts.findOne(connection.userId());
					String conversationId = sendMessage.getConversationId();
					if (!conversationId.contains(connection.userId())) {
						throw new SecurityException(String.format("User %s attempted to subscribe to unauthorized conversationId %s",
										connection.userId(),
										conversationId));
					}
					// Conversation IDs should be of the form userId1,userId2
					// TODO: Assert that it's two valid user IDs.
					ChatMessage message = new ChatMessage()
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
					EnvelopeSubConversation request = msg.getSub().getConversation();
					String conversationId = request.getConversationId();
					ConversationId key = new ConversationId(conversationId);

					AddedChangedRemoved<ConversationId, ChatMessage> observer =
									SuspendableMultimap.subscribeToKeyInMultimap("conversations", key);

					for (ChatMessage message : conversations.get(key)) {
						connection.write(new Envelope().added(new EnvelopeAdded().chatMessage(message)));
					}

					Disposable sub = observer.added().subscribe(next -> {
						connection.write(new Envelope().added(new EnvelopeAdded().chatMessage(next.getValue())));
					});

					connection.endHandler(v -> {
						sub.dispose();
						observer.dispose();
					});
				}
			}));
		});
	}
}