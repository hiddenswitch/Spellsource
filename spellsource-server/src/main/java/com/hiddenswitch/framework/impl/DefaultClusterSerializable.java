package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectReader;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.shareddata.impl.ClusterSerializable;

import java.io.IOException;

public interface DefaultClusterSerializable extends ClusterSerializable, Shareable {
	default void writeToBuffer(Buffer buffer) {
		try {
			DatabindCodec.mapper().writer().writeValue(new VertxBufferOutputStream(buffer), this);
		} catch (IOException e) {
			throw new VertxException(e);
		}
	}

	default int readFromBuffer(int pos, Buffer buffer) {
		try {
			final ObjectReader objectReader = DatabindCodec.mapper()
					.readerForUpdating(this)
					.without(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
			final VertxBufferInputStream src = new VertxBufferInputStream(buffer.getBuffer(pos, buffer.length()));
			objectReader.readValue(src);
			final int bytesRead = (int) src.getPosition();
			return pos + bytesRead;
		} catch (IOException e) {
			throw new VertxException(e);
		}
	}
}

