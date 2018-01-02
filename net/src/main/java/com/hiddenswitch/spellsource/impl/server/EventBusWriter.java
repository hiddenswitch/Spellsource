package com.hiddenswitch.spellsource.impl.server;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.streams.Pump;

public class EventBusWriter extends WebSocketWriter {
	public static final String WRITER_ADDRESS_PREFIX = "EventBusClient/users/";
	private Pump pump;


	public EventBusWriter(EventBus bus, String userId, int playerId) {
		super(bus.publisher(WRITER_ADDRESS_PREFIX + userId), userId, playerId);
	}

	public String getAddress() {
		return WRITER_ADDRESS_PREFIX + getUserId();
	}

	@Override
	protected void onSocketClosed(Void ignored) {
		super.onSocketClosed(ignored);
		try {
			getPrivateSocket().end();
		} catch (Throwable ignore) {
		}
		pump.stop();
		pump = null;
	}
}

