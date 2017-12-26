package com.hiddenswitch.spellsource.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.shareddata.impl.ClusterSerializable;

import java.io.Serializable;
import java.util.Objects;

public abstract class StringEx implements Serializable,
		Comparable<String>,
		CharSequence,
		ClusterSerializable,
		Shareable,
		Cloneable {
	private volatile String id;

	public StringEx(String id) {
		this.id = id;
	}

	@Override
	public int length() {
		return id.length();
	}

	@Override
	public char charAt(int index) {
		return id.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return id.subSequence(start, end);
	}

	@Override
	public int compareTo(String o) {
		return id.compareTo(o);
	}

	@Override
	public String toString() {
		return Objects.toString(id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public void writeToBuffer(Buffer buffer) {
		byte[] bytes = id.getBytes();
		buffer.appendInt(bytes.length);
		buffer.appendBytes(bytes);
	}

	@Override
	public int readFromBuffer(int pos, Buffer buffer) {
		int length = buffer.getInt(pos);
		final int start = pos + 4;
		id = buffer.getString(start, start + length);
		return start + length;
	}

	@Override
	protected StringEx clone() throws CloneNotSupportedException {
		return (StringEx) super.clone();
	}
}
