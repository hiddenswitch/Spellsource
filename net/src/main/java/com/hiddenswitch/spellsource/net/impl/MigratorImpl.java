package com.hiddenswitch.spellsource.net.impl;

import com.hiddenswitch.spellsource.net.models.MigrateToRequest;
import com.hiddenswitch.spellsource.net.models.MigrationRequest;
import com.hiddenswitch.spellsource.net.models.MigrationToResponse;
import io.vertx.core.*;

import java.util.ArrayList;
import java.util.List;

import static io.vertx.ext.sync.Sync.fiber;

public class MigratorImpl implements Migrator {
	Vertx vertx;
	List<MigrationRequest> requests = new ArrayList<>();

	public MigratorImpl(Vertx vertx) {
		this.vertx = vertx;
	}

	@Override
	public Migrator add(MigrationRequest request) {
		requests.add(request);
		return this;
	}

	@Override
	public void migrateTo(final int version, final Handler<MigrationToResponse> response) {
		MigrationsImpl impl = new MigrationsImpl();
		for (MigrationRequest req : requests) {
			impl.add(req);
		}
		vertx.runOnContext(v1 -> {
			vertx.runOnContext(io.vertx.ext.sync.Sync.<Void>fiber(v2 -> {
				response.handle(impl.migrateTo(new MigrateToRequest().withVersion(version)));
			}));
		});
	}
}
