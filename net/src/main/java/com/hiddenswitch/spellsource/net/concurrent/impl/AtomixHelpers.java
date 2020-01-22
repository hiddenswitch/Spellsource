package com.hiddenswitch.spellsource.net.concurrent.impl;

import io.atomix.primitive.Consistency;
import io.atomix.primitive.Replication;
import io.atomix.primitive.protocol.PrimitiveProtocolConfig;
import io.atomix.protocols.backup.MultiPrimaryProtocolConfig;
import io.atomix.vertx.AtomixClusterManager;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;

public class AtomixHelpers {
	public static AtomixClusterManager getClusterManager() {
		return getClusterManager(Vertx.currentContext().owner());
	}
	public static AtomixClusterManager getClusterManager(Vertx vertx) {
		return (AtomixClusterManager) ((VertxInternal) vertx).getClusterManager();
	}

	public static PrimitiveProtocolConfig<MultiPrimaryProtocolConfig> getProtocol() {
		return new MultiPrimaryProtocolConfig()
				.setReplication(Replication.SYNCHRONOUS)
				.setBackups(1)
				.setConsistency(Consistency.LINEARIZABLE);
	}
}
