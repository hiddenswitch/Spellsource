package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.io.IOException;

public final class JsonMessageCodec<T> implements MessageCodec<T, T> {

	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper(new SmileFactory());
		mapper.registerModule(new AfterburnerModule());
	}

	private final Class<T> target;

	public JsonMessageCodec(Class<T> target) {
		this.target = target;
	}

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
		byte[] bytes = buffer.getBytes(start, start + length);
		try {
			return mapper.readValue(bytes, target);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public T transform(T obj) {
		return obj;
	}

	@Override
	public String name() {
		return "jsoncodec" + target.getName();
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}
}
