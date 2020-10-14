package com.hiddenswitch.framework;

import io.grpc.BindableService;
import io.vertx.core.Future;

/**
 * The legacy services for Spellsource, to rapidly transition the game into a new backend.
 */
public class Legacy {

	public Future<BindableService[]> services() {



		return Future.succeededFuture();
	}
}
