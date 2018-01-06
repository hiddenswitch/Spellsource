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
	protected volatile long id;

	public NumberEx(long id) {
		this.id = id;
	}

	@Override
	public void writeToBuffer(Buffer buffer) {
		buffer.appendLong(id);
	}

	@Override
	public int readFromBuffer(int pos, Buffer buffer) {
		id = buffer.getLong(pos);
		return pos + 8;
	}

	@Override
	public int compareTo(Long o) {
		return Long.compare(id, o);
	}

	@Override
	public int intValue() {
		return ((Long) id).intValue();
	}

	@Override
	public long longValue() {
		return id;
	}

	@Override
	public float floatValue() {
		return ((Long) id).floatValue();
	}

	@Override
	public double doubleValue() {
		return ((Long) id).doubleValue();
	}

	@Override
	public String toString() {
		return Long.toString(id);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(id);
	}

	@Override
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

