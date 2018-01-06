package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Map;
import java.util.Set;

import static io.vertx.ext.sync.Sync.awaitResult;

public class SharedData {
	@Suspendable
	public static <K, V> Map<K, V> getClusterWideMap(String name, final Vertx vertx) {
		io.vertx.core.shareddata.SharedData client = vertx.sharedData();
		if (vertx.isClustered()) {
			return awaitResult(done -> client.<K, V>getClusterWideMap(name, then -> {
				if (then.failed()) {
					done.handle(Future.failedFuture(then.cause()));
					return;
				}

				done.handle(Future.succeededFuture(new SuspendableMap<K, V>(then.result())));
			}));
		} else {
			return client.getLocalMap(name);
		}
	}
}
