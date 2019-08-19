package com.hiddenswitch.spellsource.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

import java.io.IOException;
import java.io.InputStream;

public abstract class JsonMessageCodec<T> implements MessageCodec<T, T> {

	private static ObjectMapper mapper = new ObjectMapper(new SmileFactory());

	protected abstract Class<? extends T> getMessageClass();

	@Override
	public abstract String name();

	@Override
	public void encodeToWire(Buffer buffer, T obj) {
		byte[] bytes;
		try {
			bytes = mapper.writeValueAsBytes(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		buffer.appendInt(bytes.length);
		buffer.appendBytes(bytes);
	}

	@Override
	public T decodeFromWire(int pos, Buffer buffer) {
		int length = buffer.getInt(pos);
		int start = pos + 4;
//		ByteBufInputStream stream = new ByteBufInputStream(buffer.getByteBuf());
		byte[] bytes = buffer.getBytes(start, start + length);
		try {
			return mapper.readValue(bytes, getMessageClass());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
