package com.hiddenswitch.spellsource.impl.server;

import com.hiddenswitch.spellsource.client.Configuration;
import com.hiddenswitch.spellsource.common.Writer;
import com.hiddenswitch.spellsource.impl.util.ActivityMonitor;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.streams.WriteStream;

import java.util.ArrayList;
import java.util.List;

public class SessionWriter implements WriteStream<Buffer> {
	private final String userId;
	private final int playerId;
	private final EventBus eventBus;
	private final GameSession session;
	private final ActivityMonitor activityMonitor;
	private final List<EventBusWriter> writers = new ArrayList<>();
	private Handler<Throwable> exceptionHandler;

	public SessionWriter(String userId, int playerId, EventBus eventBus, GameSession session, ActivityMonitor activityMonitor) {
		this.userId = userId;
		this.playerId = playerId;
		this.eventBus = eventBus;
		this.session = session;
		this.activityMonitor = activityMonitor;
	}

	private void handleWebSocketMessage(Buffer messageBuffer) {
		com.hiddenswitch.spellsource.client.models.ClientToServerMessage message =
				Configuration.getDefaultApiClient().getJSON().deserialize(messageBuffer.toString(),
						com.hiddenswitch.spellsource.client.models.ClientToServerMessage.class);

		activityMonitor.activity();

		switch (message.getMessageType()) {
			case FIRST_MESSAGE:
				EventBusWriter writer = new EventBusWriter(eventBus, userId, playerId);
				writers.add(writer);

				if (session.isGameReady()) {
					// TODO: Remove references to the old socket
					// Replace the client
					session.onPlayerReconnected(playerId, writer);
				} else {
					session.onPlayerConnected(playerId, writer);
				}
				break;
			case UPDATE_ACTION:
				if (session == null) {
					throw new RuntimeException();
				}
				final String messageId = message.getRepliesTo();
				session.onActionReceived(messageId, message.getActionIndex());
				break;
			case UPDATE_MULLIGAN:
				if (session == null) {
					throw new RuntimeException();
				}
				final String messageId2 = message.getRepliesTo();
				session.onMulliganReceived(messageId2, message.getDiscardedCardIndices());
				break;
			case EMOTE:
				if (session == null) {
					break;
				}
				session.onEmote(message.getEmote().getEntityId(), message.getEmote().getMessage());
				break;
			case TOUCH:
				if (session == null) {
					break;
				}
				if (null != message.getEntityTouch()) {
					session.onTouch(playerId, message.getEntityTouch());
				} else if (null != message.getEntityUntouch()) {
					session.onUntouch(playerId, message.getEntityUntouch());
				}
				break;
			case CONCEDE:
				if (session == null) {
					break;
				}
				session.onConcede(playerId);
				break;
		}
	}

	@Override
	public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
		this.exceptionHandler = handler;
		return this;
	}

	@Override
	public WriteStream<Buffer> write(Buffer data) {
		try {
			handleWebSocketMessage(data);
		} catch (Throwable t) {
			if (exceptionHandler != null) {
				exceptionHandler.handle(t);
			} else {
				throw t;
			}
		}
		return this;
	}

	@Override
	public void end() {
		session.kill();
		writers.forEach(Writer::close);
	}

	@Override
	public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
		return this;
	}

	@Override
	public boolean writeQueueFull() {
		return false;
	}

	@Override
	public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
		return this;
	}
}
