package com.hiddenswitch.spellsource.util;

import com.fasterxml.jackson.databind.ObjectReader;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.impl.ClusterSerializable;

import java.io.IOException;

public interface DefaultClusterSerializable extends ClusterSerializable {
	default void writeToBuffer(Buffer buffer) {
		try {
			Json.mapper.writer().writeValue(new VertxBufferOutputStream(buffer), this);
		} catch (IOException e) {
			throw new VertxException(e);
		}
	}

	default int readFromBuffer(int pos, Buffer buffer) {
		try {
			final ObjectReader objectReader = Json.mapper.readerForUpdating(this);
			final VertxBufferInputStream src = new VertxBufferInputStream(buffer.getBuffer(pos, buffer.length()));
			objectReader.readValue(src);
			final int bytesRead = (int) src.getPosition();
			return pos + bytesRead;
		} catch (IOException e) {
			throw new VertxException(e);
		}
	}
}

