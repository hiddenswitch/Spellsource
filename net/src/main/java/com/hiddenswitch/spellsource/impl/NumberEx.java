package com.hiddenswitch.spellsource.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.Shareable;
import io.vertx.core.shareddata.impl.ClusterSerializable;

import java.io.Serializable;
import java.util.Objects;

public abstract class NumberEx extends Number implements Serializable,
		Comparable<Long>,
		ClusterSerializable,
		Shareable,
		Cloneable {

	@Deprecated
	public long id;

	@SuppressWarnings("deprecation")
	@Deprecated
	public NumberEx() {
		this.id = 0;
	}

	@SuppressWarnings("deprecation")
	public NumberEx(long id) {
		this.id = id;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void writeToBuffer(Buffer buffer) {
		buffer.appendLong(id);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int readFromBuffer(int pos, Buffer buffer) {
		id = buffer.getLong(pos);
		return pos + 8;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int compareTo(Long o) {
		return Long.compare(id, o);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int intValue() {
		return ((Long) id).intValue();
	}

	@Override
	@SuppressWarnings("deprecation")
	public long longValue() {
		return id;
	}

	@Override
	@SuppressWarnings("deprecation")
	public float floatValue() {
		return ((Long) id).floatValue();
	}

	@Override
	@SuppressWarnings("deprecation")
	public double doubleValue() {
		return ((Long) id).doubleValue();
	}

	@Override
	@SuppressWarnings("deprecation")
	public String toString() {
		return Long.toString(id);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int hashCode() {
		return Long.hashCode(id);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		final Long rhs;
		if (obj instanceof NumberEx) {
			rhs = ((NumberEx) obj).id;
		} else if (obj instanceof Long) {
			rhs = (Long) obj;
		} else if (obj instanceof Number) {
			rhs = ((Number) obj).longValue();
		} else {
			return false;
		}
		return Long.compare(id, rhs) == 0;
	}
}

