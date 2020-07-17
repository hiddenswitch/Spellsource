package com.hiddenswitch.spellsource.net.impl;

import com.hiddenswitch.spellsource.net.models.MigrationRequest;
import com.hiddenswitch.spellsource.net.models.MigrationToResponse;
import io.vertx.core.Handler;

public interface Migrator {
	Migrator add(MigrationRequest request);

	void migrateTo(final int version, final Handler<MigrationToResponse> response);
}
