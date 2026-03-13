package io.vertx.grpc.contextstorage;

import io.grpc.Context;
import io.vertx.core.internal.ContextInternal;

/**
 * gRPC context storage.
 */
public class GrpcStorage {

  public final io.grpc.Context currentGrpcContext;
  public final io.vertx.core.internal.ContextInternal prevVertxContext;

  public GrpcStorage(Context currentGrpcContext, ContextInternal prevVertxContext) {
    this.currentGrpcContext = currentGrpcContext;
    this.prevVertxContext = prevVertxContext;
  }
}
