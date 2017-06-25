package com.hiddenswitch.proto3.net.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.models.MigrationRequest;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Handler;

import static io.vertx.ext.sync.Sync.awaitFiber;

public interface Migrator {
	Migrator add(MigrationRequest request);

	void migrateTo(int version, Handler<AsyncResult<CompositeFuture>> response);

	@Suspendable
	default CompositeFuture migrateTo(int version) throws SuspendExecution, InterruptedException {
		return awaitFiber(h -> migrateTo(version, h));
	}
}
