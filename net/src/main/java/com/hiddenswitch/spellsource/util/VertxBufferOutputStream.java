package com.hiddenswitch.spellsource.util;

import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by bberman on 11/26/16.
 */
public class VertxBufferOutputStream extends OutputStream {
	private final Buffer buffer;

	public VertxBufferOutputStream() {
		this(512);
	}

	public VertxBufferOutputStream(int initialSize) {
		buffer = Buffer.buffer(initialSize);
	}

	public VertxBufferOutputStream(Buffer input) {
		buffer = input;
	}

	@Override
	public void write(int b) throws IOException {
		buffer.appendByte((byte) b);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) ||
				((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}

		buffer.appendBytes(b, off, len);
	}

	public Buffer getBuffer() {
		return buffer;
	}

	public int size() {
		return getBuffer().length();
	}
}

