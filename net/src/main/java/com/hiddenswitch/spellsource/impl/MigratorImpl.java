package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.Migrations;
import com.hiddenswitch.spellsource.models.MigrateToRequest;
import com.hiddenswitch.spellsource.models.MigrationRequest;
import com.hiddenswitch.spellsource.models.MigrationToResponse;
import com.hiddenswitch.spellsource.util.Migrator;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.RpcClient;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
			vertx.runOnContext(Sync.suspendableHandler(v2 -> {
				response.handle(impl.migrateTo(new MigrateToRequest().withVersion(version)));
			}));
		});
	}
}
