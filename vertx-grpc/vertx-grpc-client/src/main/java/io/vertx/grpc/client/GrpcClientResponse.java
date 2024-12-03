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
package io.vertx.grpc.client;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.grpc.common.GrpcError;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.common.GrpcReadStream;
import io.vertx.grpc.common.GrpcStatus;

/**
 * A response from a gRPC server.
 *
 * You can set a {@link #messageHandler(Handler)} to receive {@link GrpcMessage} and a {@link #endHandler(Handler)} to be notified
 * of the end of the response.
 *
 */
@VertxGen
public interface GrpcClientResponse<Req, Resp> extends GrpcReadStream<Resp> {

  /**
   * @return the gRPC status or {@code null} when the status has not yet been received
   */
  @CacheReturn
  GrpcStatus status();

  /**
   * @return the gRPC status message of {@code null} when the status has not yet been received or not transmitted
   */
  @CacheReturn
  String statusMessage();

  /**
   * @return the {@link MultiMap} to write metadata trailers
   */
  MultiMap trailers();

  @Override
  @Fluent
  GrpcClientResponse<Req, Resp> messageHandler(@Nullable Handler<GrpcMessage> handler);

  @Override
  @Fluent
  GrpcClientResponse<Req, Resp> errorHandler(@Nullable Handler<GrpcError> handler);

  @Override
  GrpcClientResponse<Req, Resp> exceptionHandler(@Nullable Handler<Throwable> handler);

  @Override
  GrpcClientResponse<Req, Resp> handler(@Nullable Handler<Resp> handler);

  @Override
  GrpcClientResponse<Req, Resp> endHandler(@Nullable Handler<Void> handler);

  @Override
  GrpcClientResponse<Req, Resp> pause();

  @Override
  GrpcClientResponse<Req, Resp> resume();

  @Override
  GrpcClientResponse<Req, Resp> fetch(long amount);
}
