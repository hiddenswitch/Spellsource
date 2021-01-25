package com.hiddenswitch.framework.impl;

import io.opentracing.propagation.Binary;

import java.nio.ByteBuffer;

public class BinaryCarrier implements Binary {

	private ByteBuffer buffer = null;


	/**
	 * Creates a binary carrier for the purposes of injection.
	 */
	public BinaryCarrier() {
	}

	/**
	 * Creates the binary carrier for extraction
	 *
	 * @param existingBytes
	 */
	public BinaryCarrier(byte[] existingBytes) {
		this.buffer = ByteBuffer.wrap(existingBytes);
	}

	@Override
	public ByteBuffer extractionBuffer() {
		return buffer;
	}

	@Override
	public ByteBuffer injectionBuffer(int length) {
		this.buffer = ByteBuffer.allocate(length);
		return this.buffer;
	}

	public byte[] getBytes() {
		if (buffer == null) {
			return new byte[0];
		}
		return buffer.array();
	}
}
