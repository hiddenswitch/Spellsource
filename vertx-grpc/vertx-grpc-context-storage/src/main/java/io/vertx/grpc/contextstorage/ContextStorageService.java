package io.vertx.grpc.contextstorage;

import io.vertx.core.impl.VertxBuilder;
import io.vertx.core.spi.VertxServiceProvider;
import io.vertx.core.spi.context.storage.ContextLocal;

/**
 * Register the local storage.
 */
public class ContextStorageService implements VertxServiceProvider {

  public static final ContextLocal<GrpcStorage> CONTEXT_LOCAL = ContextLocal.registerLocal(GrpcStorage.class);

	@Override
	public void init(VertxBuilder builder) {

	}
}
