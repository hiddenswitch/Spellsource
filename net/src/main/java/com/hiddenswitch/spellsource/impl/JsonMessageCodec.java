package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.client.models.Envelope;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

import static io.vertx.core.json.JsonObject.mapFrom;

public abstract class JsonMessageCodec<T> implements MessageCodec<T, T> {

	protected abstract Class<? extends T> getMessageClass();

	@Override
	public abstract String name();

	@Override
	public void encodeToWire(Buffer buffer, T obj) {
		mapFrom(obj).writeToBuffer(buffer);
	}

	@Override
	public T decodeFromWire(int pos, Buffer buffer) {
		JsonObject obj = new JsonObject();
		obj.readFromBuffer(pos, buffer);
		return obj.mapTo(getMessageClass());
	}

	@Override
	public T transform(T envelope) {
		return envelope;
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}
}
