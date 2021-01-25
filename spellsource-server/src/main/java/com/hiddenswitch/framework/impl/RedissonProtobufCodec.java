package com.hiddenswitch.framework.impl;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Parser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;

public class RedissonProtobufCodec extends BaseCodec {
	private final Parser<?> parser;

	private final Encoder encoder = in -> {
		if (in instanceof GeneratedMessageV3) {
			var message = (GeneratedMessageV3) in;
			var buffer = ByteBufAllocator.DEFAULT.buffer();
			var buf = new ByteBufOutputStream(buffer);
			message.writeTo(buf);
			return buf.buffer();
		}
		throw new IOException("invalid type");
	};

	private final Decoder<Object> decoder = new Decoder<>() {
		@Override
		public Object decode(ByteBuf buf, State state) throws IOException {
			return parser.parseFrom(buf.nioBuffer());
		}
	};


	/**
	 * Used by {@link BaseCodec#copy(ClassLoader, Codec)}
	 *
	 * @param classLoader
	 * @param codec
	 */
	public RedissonProtobufCodec(ClassLoader classLoader, RedissonProtobufCodec codec) {
		this.parser = codec.parser;
	}

	public RedissonProtobufCodec(Parser<?> parser) {
		this.parser = parser;
	}

	@Override
	public Decoder<Object> getValueDecoder() {
		return decoder;
	}

	@Override
	public Encoder getValueEncoder() {
		return encoder;
	}
}
