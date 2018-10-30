package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.client.models.Envelope;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

public class EnvelopeMessageCodec implements MessageCodec<Envelope, Envelope> {

	@Override
	public void encodeToWire(Buffer buffer, Envelope envelope) {
		JsonObject.mapFrom(envelope).writeToBuffer(buffer);
	}

	@Override
	public Envelope decodeFromWire(int pos, Buffer buffer) {
		JsonObject obj = new JsonObject();
		obj.readFromBuffer(pos, buffer);
		return obj.mapTo(Envelope.class);
	}

	@Override
	public Envelope transform(Envelope envelope) {
		return envelope;
	}

	@Override
	public String name() {
		return "envelope";
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}
}
