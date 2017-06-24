package com.hiddenswitch.proto3.net.models;

import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.proto3.net.util.SuspendableSerializableAction;
import io.vertx.core.Vertx;

import java.io.Serializable;

public class MigrationRequest implements Serializable {
	private SuspendableSerializableAction<Vertx> up;
	private SuspendableSerializableAction<Vertx> down;
	private int version;

	public SuspendableAction1<Vertx> getUp() {
		return up;
	}

	public void setUp(SuspendableSerializableAction<Vertx> up) {
		this.up = up;
	}

	public SuspendableAction1<Vertx> getDown() {
		return down;
	}

	public void setDown(SuspendableSerializableAction<Vertx> down) {
		this.down = down;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public MigrationRequest withUp(final SuspendableSerializableAction<Vertx> up) {
		this.up = up;
		return this;
	}

	public MigrationRequest withDown(final SuspendableSerializableAction<Vertx> down) {
		this.down = down;
		return this;
	}

	public MigrationRequest withVersion(final int version) {
		this.version = version;
		return this;
	}
}
