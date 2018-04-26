package com.hiddenswitch.spellsource.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.shareddata.impl.ClusterSerializable;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Objects;

public abstract class StringEx implements Serializable,
		Comparable<String>,
		CharSequence,
		ClusterSerializable,
		Shareable,
		Cloneable {
	private static final long serialVersionUID = -6849794470754667711L;

	@Deprecated
	public String id;

	@Deprecated
	public StringEx() {
		id = null;
	}

	@SuppressWarnings("deprecation")
	public StringEx(String id) {
		this.id = id;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int length() {
		return id.length();
	}

	@Override
	@SuppressWarnings("deprecation")
	public char charAt(int index) {
		return id.charAt(index);
	}

	@Override
	@SuppressWarnings("deprecation")
	public CharSequence subSequence(int start, int end) {
		return id.subSequence(start, end);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int compareTo(String o) {
		return id.compareTo(o);
	}

	@Override
	@SuppressWarnings("deprecation")
	public String toString() {
		return Objects.toString(id);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean equals(Object obj) {
		if (obj instanceof StringEx) {
			return id.equals(((StringEx) obj).id);
		} else if (obj instanceof CharSequence) {
			return id.equals(obj);
		} else {
			return false;
		}

	}

	@Override
	@SuppressWarnings("deprecation")
	public void writeToBuffer(Buffer buffer) {
		byte[] bytes = id.getBytes();
		buffer.appendInt(bytes.length);
		buffer.appendBytes(bytes);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int readFromBuffer(int pos, Buffer buffer) {
		int length = buffer.getInt(pos);
		final int start = pos + 4;
		id = buffer.getString(start, start + length);
		return start + length;
	}

	@Override
	protected StringEx clone() {
		try {
			return (StringEx) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
/*
	private void writeObject(java.io.ObjectOutputStream out)
			throws IOException {
		if (id == null) {
			out.write(0);
			return;
		}
		final byte[] bytes = id.getBytes(Charset.defaultCharset());
		out.write(bytes.length);
		out.write(bytes);
	}

	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		int size = in.readInt();
		if (size == 0) {
			id = null;
			return;
		}
		byte[] buf = new byte[size];
		int read = in.read(buf, 0, size);
		if (read != size) {
			throw new IOException("invalid size");
		}
		id = new String(buf, Charset.defaultCharset());
	}

	private void readObjectNoData()
			throws ObjectStreamException {
		id = null;
	}
	*/
}
