package com.minionate.tests;

import com.hiddenswitch.proto3.net.impl.ServerImpl;
import com.hiddenswitch.proto3.net.impl.ServiceTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;


public class IntegrationTestBase extends ServiceTest<ServerImpl> {
	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<ServerImpl>> done) {
		ServerImpl instance = new ServerImpl();
		vertx.deployVerticle(instance, then -> done.handle(Future.succeededFuture(instance)));
	}
}
