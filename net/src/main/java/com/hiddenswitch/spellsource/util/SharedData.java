package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import static io.vertx.ext.sync.Sync.awaitResult;

public class SharedData {
	@Suspendable
	public static <K, V> SuspendableMap<K, V> getClusterWideMap(String name, final Vertx vertx) throws SuspendExecution {
		io.vertx.core.shareddata.SharedData client = vertx.sharedData();
		if (vertx.isClustered()) {
			AsyncMap<K, V> map = awaitResult(done -> client.<K, V>getClusterWideMap(name, done));
			return new SuspendableAsyncMap<>(map);
		} else {
			return new SuspendableWrappedMap<>(client.<K, V>getLocalMap(name));
		}
	}
}
