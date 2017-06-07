package com.minionate.tests;

import com.hiddenswitch.proto3.net.impl.ServerImpl;
import com.hiddenswitch.proto3.net.impl.ServiceTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;


@RunWith(VertxUnitRunner.class)
public class IntegrationTestBase extends ServiceTest<ServerImpl> {
	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<ServerImpl>> done) {
		ServerImpl instance = new ServerImpl().withEmbeddedConfiguration();
		vertx.deployVerticle(instance, then -> done.handle(Future.succeededFuture(instance)));
	}
}
