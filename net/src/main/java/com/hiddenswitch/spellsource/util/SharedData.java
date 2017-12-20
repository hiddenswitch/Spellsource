package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import static io.vertx.ext.sync.Sync.awaitResult;

public class SharedData {
	private static SharedData instance;
	private io.vertx.core.shareddata.SharedData client;
	private Vertx vertx;

	private SharedData() {
	}

	public static SharedData sharedData() {
		if (instance == null) {
			instance = new SharedData();
		}

		return instance;
	}

	public SharedData connect(Vertx vertx) {
		if ((this.vertx != null
				&& !this.vertx.equals(vertx))
				|| client == null) {
			client = vertx.sharedData();
		}

		return this;
	}

	@Suspendable
	public <K, V> SuspendableMap<K, V> getClusterWideMap(String name) {
		return awaitResult(done -> client.<K, V>getClusterWideMap(name, then -> {
			if (then.failed()) {
				done.handle(Future.failedFuture(then.cause()));
				return;
			}

			done.handle(Future.succeededFuture(new SuspendableMap<K, V>(then.result())));
		}));
	}

	@Suspendable
	public static <K, V> SuspendableMap<K, V> getClusterWideMap(String name, final io.vertx.core.shareddata.SharedData client) {
		return awaitResult(done -> client.<K, V>getClusterWideMap(name, then -> {
			if (then.failed()) {
				done.handle(Future.failedFuture(then.cause()));
				return;
			}

			done.handle(Future.succeededFuture(new SuspendableMap<K, V>(then.result())));
		}));
	}
}
