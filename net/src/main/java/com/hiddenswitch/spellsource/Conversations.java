package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.ConversationRecord;
import com.hiddenswitch.spellsource.impl.util.MessageRecord;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

import static io.vertx.ext.sync.Sync.awaitResult;

public class Conversations {
	/*
	private static final String CONVERSATIONS = "conversations";
	private final static char ID_SEPARATOR = '$';

	public static String getId(String id1, String id2) {
		return id1.compareTo(id2) > 0 ? id1 + ID_SEPARATOR + id2 : id2 + ID_SEPARATOR + id1;
	}

	public static MessageRecord insertMessage(MongoClient mongo, String originId, String authorDisplayName,
	                                          String destId, String text)
			throws SuspendExecution, InterruptedException {

		//get conversation
		ConversationRecord conversation = getCreateConversation(mongo, originId, destId);

		//create message record
		MessageRecord messageRecord = new MessageRecord(originId, authorDisplayName, text, System.currentTimeMillis());

		//insert message record
		MongoClientUpdateResult result = awaitResult(h -> mongo.updateCollection(CONVERSATIONS,
				QuickJson.json("_id", conversation.getId()),
				QuickJson.json("$push", QuickJson.json("messages", QuickJson.json(messageRecord))), h));

		return messageRecord;
	}

	public static ConversationRecord getCreateConversation(MongoClient mongo, String player1, String player2)
			throws SuspendExecution, InterruptedException {
		String conversationId = getId(player1, player2);
		JsonObject result = awaitResult(h -> mongo.findOne(CONVERSATIONS, QuickJson.json("_id", conversationId), QuickJson.json(), h));

		ConversationRecord conversation;
		if (result != null) {
			conversation = QuickJson.fromJson(result, ConversationRecord.class);
		} else {
			final ConversationRecord newConversation = new ConversationRecord(conversationId);
			String ignored = awaitResult(h -> mongo.insert(CONVERSATIONS, QuickJson.toJson(newConversation), h));
			conversation = newConversation;
		}
		return conversation;
	}
	*/

	public static void realtime() {
		// Only requires accounts
		Realtime.method(EnvelopeMethod::getSendMessage, context -> {
			final EnvelopeMethodSendMessage sendMessage = context.request();
			// Sending a chat message
			SuspendableMultimap<ConversationId, ChatMessage> conversations = SharedData.getClusterWideMultimap("conversations");
			final UserRecord sender = Accounts.findOne(context.user());
			final String conversationId = sendMessage.getConversationId();
			// TODO: Check that you have permission to actually message this conversation ID

			final ChatMessage message = new ChatMessage()
					.messageId("c" + Integer.toString(conversations.size()) + ":" + RandomStringUtils.randomAlphanumeric(6))
					.conversationId(conversationId)
					.message(sendMessage.getMessage())
					.senderUserId(sender.getId())
					.senderName(sender.getUsername());

			conversations.put(new ConversationId(conversationId), message);
			// Notify updated/result obtained
			context.result().sendMessage(new EnvelopeResultSendMessage().messageId(message.getMessageId()));
		});

		Realtime.publish(EnvelopeSubConversation.class, EnvelopeSub::getConversation, (ChatMessage obj) -> new EnvelopeAdded().chatMessage(obj), context -> {
			// Subscribe to conversation
			final String conversationId = context.request().getConversationId();
			final ConversationId key = new ConversationId(conversationId);

			AddedChangedRemoved<ConversationId, ChatMessage> observer =
					SharedData.subscribeToKeyInMultimap("conversations", key);

			for (ChatMessage message : SharedData.<ConversationId, ChatMessage>getClusterWideMultimap("conversations").get(key)) {
				context.client().added(message.getConversationId(), message);
			}

			context.addDisposable(observer.added().subscribe(next -> {
				context.client().added(next.getKey(), next.getValue());
			}));

			context.addDisposable(observer);
		});
	}
}