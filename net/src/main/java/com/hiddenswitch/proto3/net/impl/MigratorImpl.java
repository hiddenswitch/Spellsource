package com.hiddenswitch.proto3.net.impl;

import com.hiddenswitch.proto3.net.Migrations;
import com.hiddenswitch.proto3.net.models.MigrateToRequest;
import com.hiddenswitch.proto3.net.models.MigrationRequest;
import com.hiddenswitch.proto3.net.util.Migrator;
import com.hiddenswitch.proto3.net.util.RPC;
import com.hiddenswitch.proto3.net.util.RpcClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MigratorImpl implements com.hiddenswitch.proto3.net.util.Migrator {
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
	public void migrateTo(final int version, final Handler<AsyncResult<CompositeFuture>> response) {
		vertx.deployVerticle(new MigrationsImpl(), then -> {
			RpcClient<Migrations> migrations = RPC.connect(Migrations.class, vertx.eventBus());
			CompositeFuture.join(
					Stream.concat(
							requests.stream().map(x -> migrations.promise(service -> service.add(x))),
							Stream.of(migrations.promise(service -> service.migrateTo(new MigrateToRequest().withVersion(version)))))
							.collect(Collectors.toList())).setHandler(cf -> {
				vertx.undeploy(then.result(), finallyThen -> response.handle(cf));
			});
		});

	}
}
