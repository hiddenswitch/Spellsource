package com.hiddenswitch.minionate;

import com.hiddenswitch.proto3.net.models.EventLogicRequest;
import com.hiddenswitch.proto3.net.models.LogicResponse;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;

import java.util.function.Function;

/**
 * Created by bberman on 6/6/17.
 */
class LegacyPersistenceHandlerImpl<T extends GameEvent> implements LegacyPersistenceHandler<T> {
	private GameEventType gameEventType;
	private String id;
	private Function<EventLogicRequest<T>, LogicResponse> onLogicRequest;
	private Function<T, EventLogicRequest<T>> onGameEvent;

	public LegacyPersistenceHandlerImpl(GameEventType gameEventType, String id, Function<EventLogicRequest<T>,
			LogicResponse> onLogicRequest, Function<T, EventLogicRequest<T>> onGameEvent) {
		this.gameEventType = gameEventType;
		this.id = id;
		this.onLogicRequest = onLogicRequest;
		this.onGameEvent = onGameEvent;
	}

	@Override
	public GameEventType getGameEvent() {
		return gameEventType;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public LogicResponse onLogicRequest(EventLogicRequest<T> request) {
		return this.onLogicRequest.apply(request);
	}

	@Override
	public EventLogicRequest<T> onGameEvent(T event) {
		return this.onGameEvent.apply(event);
	}
}
