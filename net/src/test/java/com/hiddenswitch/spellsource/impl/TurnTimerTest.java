package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.Spellsource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

public class TurnTimerTest extends ServiceTest<GatewayImpl> {
	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<GatewayImpl>> done) {
		Spellsource.spellsource().deployAll(vertx, then -> {
			if (then.failed()) {
				done.handle(Future.failedFuture(then.cause()));
				return;
			}


		});
	}
}
