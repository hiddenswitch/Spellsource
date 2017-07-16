package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.Migrations;
import com.hiddenswitch.spellsource.models.MigrateToRequest;
import com.hiddenswitch.spellsource.models.MigrationRequest;
import com.hiddenswitch.spellsource.models.MigrationToResponse;
import com.hiddenswitch.spellsource.util.Migrator;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

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
	public void migrateTo(final int version, final Handler<AsyncResult<MigrationToResponse>> response) {
		vertx.deployVerticle(new MigrationsImpl(), then -> {
			RpcClient<Migrations> migrations = Rpc.connect(Migrations.class, vertx.eventBus());
			CompositeFuture.join(requests.stream().map(x -> migrations.promise(service -> service.add(x))).collect(Collectors.toList()))
					.setHandler(cf -> {
						migrations.promise(service -> service.migrateTo(new MigrateToRequest().withVersion(version))).setHandler(migrationResult -> {
							vertx.undeploy(then.result(), finallyThen -> response.handle(migrationResult));
						});

			});
		});

	}
}
