package com.hiddenswitch.framework.impl;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class ProtobufCodec<T extends Message> implements MessageCodec<T, T> {

	private final T target;

	public ProtobufCodec(T defaultInstance) {
		this.target = defaultInstance;
	}

	@Override
	public void encodeToWire(Buffer buffer, T t) {
		buffer.appendBytes(t.toByteArray());
	}

	@Override
	public T decodeFromWire(int pos, Buffer buffer) {
		try {
			@SuppressWarnings("unchecked")
			var parserForType = (Parser<T>) target.getParserForType();
			var bytes = buffer.getBytes(pos, buffer.length());
			return parserForType.parseFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public T transform(T t) {
		return t;
	}

	@Override
	public String name() {
		return "protobufcodec" + target.getClass().getName();
	}

	@Override
	public byte systemCodecID() {
		return -1;
	}

	public Class<T> getTargetClass() {
		@SuppressWarnings("unchecked")
		var targetClass = (Class<T>) target.getClass();
		return targetClass;
	}
}
