package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.common.SuspendablePump;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.sync.HandlerReceiverAdaptor;
import io.vertx.ext.web.RoutingContext;
import io.vertx.reactivex.ObservableHelper;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.vertx.ext.sync.Sync.awaitResult;


public class Bridge {
	public static Handler<RoutingContext> create() {
		return Sync.suspendableHandler((routingContext) -> {
			String userId = Accounts.userId(routingContext);
			if (userId == null) {
				throw new SecurityException("Not authorized");
			}

			final Vertx vertx = Vertx.currentContext().owner();

			try {
				final Lock lock = awaitResult(h -> vertx.sharedData().getLockWithTimeout("bridge-lock-" + userId, 400L, h));
				final EventBus bus = vertx.eventBus();
				final ServerWebSocket socket = routingContext.request().upgrade();
				final MessageConsumer<Buffer> consumer = bus.<Buffer>consumer("bridge-" + userId);
				final Pump toUser = new SuspendablePump<>(consumer.bodyStream(), socket, Integer.MAX_VALUE).start();
				final Deque<Pump> subs = new ConcurrentLinkedDeque<>();
				final Set<String> subscriptions = new ConcurrentHashSet<>();

				socket.closeHandler(ignored -> {
					toUser.stop();
					consumer.unregister();
					for (Pump pump : subs) {
						pump.stop();
					}

					lock.release();
				});

				socket.handler(Sync.suspendableHandler(buffer -> {
					Envelope envelope = Json.decodeValue(buffer, Envelope.class);

					if (envelope.getSub() != null) {
						// Subscription to data
						if (envelope.getSub().getConversation() != null) {
							// Subscribe to conversation
							final String conversationId = envelope.getSub().getConversation().getConversationId();
							final String key = "conversations-" + conversationId;
							if (!subscriptions.contains(key)) {
								subscriptions.add(key);
								final ConversationId key1 = new ConversationId(conversationId);
								// Send all existing messages
								for (ChatMessage message : SharedData.<ConversationId, ChatMessage>getClusterWideMultimap("conversations").get(key1)) {
									socket.write(Json.encodeToBuffer(new EnvelopeAdded().chatMessage(message)));
								}

								// Subscribe to ongoing messages
								AddedChangedRemoved<ConversationId, ChatMessage> observer =
										SharedData.subscribeToKeyInMultimap("conversations",
												key1);
								ReadStream<Buffer> addedStream = ObservableHelper.toReadStream(observer.added()
										.map(cm -> new Envelope().added(new EnvelopeAdded().chatMessage(cm.getValue())))
										.map(Json::encodeToBuffer));
								Pump addedPump = new SuspendablePump<>(addedStream, socket, Integer.MAX_VALUE);
								addedPump.start();
								subs.add(addedPump);
								// TODO: Deal with removed?
							}

						}
					}

					if (envelope.getMethod() != null) {
						// Method call
						if (envelope.getMethod().getSendMessage() != null) {
							final EnvelopeMethodSendMessage sendMessage = envelope.getMethod().getSendMessage();
							// Sending a chat message
							SuspendableMultimap<ConversationId, ChatMessage> conversations = SharedData.getClusterWideMultimap("conversations");
							final UserRecord sender = Accounts.findOne(userId);
							final String conversationId = sendMessage.getConversationId();
							// TODO: Check that you have permission to actually message this conversation ID

							final ChatMessage message = new ChatMessage()
									.messageId("c" + Integer.toString(conversations.size()) + ":" + RandomStringUtils.randomAlphanumeric(6))
									.conversationId(conversationId)
									.message(sendMessage.getMessage())
									.senderUserId(userId)
									.senderName(sender.getUsername());

							conversations.put(new ConversationId(conversationId), message);
							// Notify updated/result obtained
							socket.write(Json.encodeToBuffer(new Envelope().result(new EnvelopeResult().sendMessage(new EnvelopeResultSendMessage().messageId(message.getMessageId())))));
						}
					}
				}));
			} catch (VertxException ex) {
				throw new RuntimeException("Already connected or timed out.");
			}
		});
	}
}