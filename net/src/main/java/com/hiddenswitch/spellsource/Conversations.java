package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.concurrent.SuspendableMultimap;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.util.*;
import io.reactivex.disposables.Disposable;
import io.vertx.core.Future;
import org.apache.commons.lang3.RandomStringUtils;

import static com.hiddenswitch.spellsource.util.Sync.defer;
import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

/**
 * Provides the methods to help users message each other.
 */
public interface Conversations {

	/**
	 * Creates the ephemeral messaging state and notifies users when they receive messages.
	 */
	static void handleConnections() {
		Connection.connected((connection, fut) -> {
			defer(v1 -> {
				try {
					SuspendableMultimap<ConversationId, ChatMessage> conversations = SuspendableMultimap.getOrCreate("conversations");
					connection.handler(suspendableHandler(msg -> {
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

							connection.endHandler(v2 -> {
								sub.dispose();
								observer.dispose();
							});
						}
					}));

					fut.handle(Future.succeededFuture());
				} catch (RuntimeException any) {
					fut.handle(Future.failedFuture(any));
				}
			});
		});
	}
}