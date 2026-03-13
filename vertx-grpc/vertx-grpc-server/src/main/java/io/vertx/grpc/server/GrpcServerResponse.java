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

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.streams.ReadStream;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.common.GrpcWriteStream;
import io.vertx.grpc.common.WireFormat;

import java.util.Set;

@VertxGen
public interface GrpcServerResponse<Req, Resp> extends GrpcWriteStream<Resp> {

  /**
   * Set the grpc status response
   *
   * @param status the status
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GrpcServerResponse<Req, Resp> status(GrpcStatus status);

  /**
   * Set the grpc status response message
   *
   * @param msg the message
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GrpcServerResponse<Req, Resp> statusMessage(String msg);

  @Fluent
  GrpcServerResponse<Req, Resp> encoding(String encoding);

  @Fluent
  GrpcServerResponse<Req, Resp> format(WireFormat format);

  /**
   * @return the set of accepted encodings sent by the client, note that {@code identity} should not be part of this set.
   *         This can be used to set the response {@link #encoding(String) encoding} to ensure the client will accept
   *         the encoding. This is a glorified wrapper for the {@code grpc-accept-encoding} header.
   */
  Set<String> acceptedEncodings();

  /**
   * @return the {@link MultiMap} to write metadata trailers
   */
  MultiMap trailers();

  @Override
  GrpcServerResponse<Req, Resp> exceptionHandler(@Nullable Handler<Throwable> handler);

  @Override
  GrpcServerResponse<Req, Resp> setWriteQueueMaxSize(int maxSize);

  @Override
  GrpcServerResponse<Req, Resp> drainHandler(@Nullable Handler<Void> handler);

  /**
   * Send the response headers.
   *
   * @return a future notified by the success or failure of the write
   */
  Future<Void> writeHead();

  default Future<Void> send(Resp item) {
    return end(item);
  }

  default Future<Void> send(ReadStream<Resp> body) {
    return body.pipeTo(this);
  }

  /**
   * End the stream with an appropriate status message, when {@code failure} is
   *
   * <ul>
   *   <li>{@link StatusException}, set status to {@link StatusException#status()} and status message to {@link StatusException#message()}</li>
   *   <li>{@link UnsupportedOperationException} returns {@link GrpcStatus#UNIMPLEMENTED}</li>
   *   <li>otherwise returns {@link GrpcStatus#UNKNOWN}</li>
   * </ul>
   *
   * @param failure the failure
   */
  void fail(Throwable failure);

}
