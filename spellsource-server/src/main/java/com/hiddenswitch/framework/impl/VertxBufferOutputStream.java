package com.hiddenswitch.framework.impl;

import io.vertx.core.buffer.Buffer;

import java.io.OutputStream;

/**
 * Converts a vertx buffer into an {@link OutputStream}
 */
public class VertxBufferOutputStream extends OutputStream {
	private final Buffer buffer;

	public VertxBufferOutputStream(Buffer input) {
		buffer = input;
	}

	@Override
	public void write(int b) {
		buffer.appendByte((byte) b);
	}

	@Override
	public void write(byte[] b, int off, int len) {
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

