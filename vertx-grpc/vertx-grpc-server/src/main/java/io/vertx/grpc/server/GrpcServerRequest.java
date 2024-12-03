/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.server;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpConnection;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.grpc.common.GrpcError;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.common.GrpcReadStream;
import io.vertx.grpc.common.ServiceName;

@VertxGen
public interface GrpcServerRequest<Req, Resp> extends GrpcReadStream<Req> {

  /**
   * @return the service name
   */
  @CacheReturn
  ServiceName serviceName();

  /**
   * @return the method name
   */
  @CacheReturn
  String methodName();

  /**
   * @return the full method name sent by the client
   */
  @CacheReturn
  String fullMethodName();

  /**
   * @return the response
   */
  @CacheReturn
  GrpcServerResponse<Req, Resp> response();

  @Override
  @Fluent
  GrpcServerRequest<Req, Resp> messageHandler(@Nullable Handler<GrpcMessage> handler);

  @Override
  @Fluent
  GrpcServerRequest<Req, Resp> errorHandler(@Nullable Handler<GrpcError> handler);

  @Override
  GrpcServerRequest<Req, Resp> exceptionHandler(@Nullable Handler<Throwable> handler);

  @Override
  GrpcServerRequest<Req, Resp> handler(@Nullable Handler<Req> handler);

  @Override
  GrpcServerRequest<Req, Resp> pause();

  @Override
  GrpcServerRequest<Req, Resp> resume();

  @Override
  GrpcServerRequest<Req, Resp> fetch(long amount);

  @Override
  GrpcServerRequest<Req, Resp> endHandler(@Nullable Handler<Void> endHandler);

  /**
   * @return the underlying HTTP connection
   */
  HttpConnection connection();

  RoutingContext routingContext();
}
