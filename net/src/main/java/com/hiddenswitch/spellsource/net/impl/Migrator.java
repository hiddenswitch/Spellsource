package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.models.MigrationRequest;
import com.hiddenswitch.spellsource.net.models.MigrationToResponse;
import io.vertx.core.Handler;

import static io.vertx.ext.sync.Sync.awaitFiber;

public interface Migrator {
	Migrator add(MigrationRequest request);

	void migrateTo(final int version, final Handler<MigrationToResponse> response);
}
