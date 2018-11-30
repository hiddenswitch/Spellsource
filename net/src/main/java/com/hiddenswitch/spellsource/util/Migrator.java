package com.hiddenswitch.spellsource.util;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.models.MigrationRequest;
import com.hiddenswitch.spellsource.models.MigrationToResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import static io.vertx.ext.sync.Sync.awaitFiber;

public interface Migrator {
	Migrator add(MigrationRequest request);

	void migrateTo(final int version, final Handler<MigrationToResponse> response);

	@Suspendable
	default Void migrateTo(int version) throws SuspendExecution, InterruptedException {
		return awaitFiber(h -> migrateTo(version, h::handle));
	}
}
