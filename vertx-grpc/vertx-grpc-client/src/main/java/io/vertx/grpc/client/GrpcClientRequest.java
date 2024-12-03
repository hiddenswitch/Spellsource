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
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.streams.ReadStream;
import io.vertx.grpc.common.GrpcWriteStream;
import io.vertx.grpc.common.ServiceName;

/**
 * A request to a gRPC server.
 *
 * <p>You interact with the remote service with gRPC generated messages or protobuf encoded messages.
 *
 * <p>Before sending a request you need to set {@link #serviceName)} and {@link #methodName)} or
 * alternatively the service {@link #fullMethodName}.
 *
 * <p>Writing a request message will send the request to the service:
 *
 * <ul>
 *   <li>To send a unary request, just call {@link #end(Req)}</li>
 *   <li>To send a streaming request, call {@link #write(Req)} any time you need and then {@link #end()}</li>
 * </ul>
 */
@VertxGen
public interface GrpcClientRequest<Req, Resp> extends GrpcWriteStream<Req> {

  @Fluent
  GrpcClientRequest<Req, Resp> encoding(String encoding);

  /**
   * Set the full method name to call, it must follow the format {@code package-name + '.' + service-name + '/' + method-name}
   * or an {@code IllegalArgumentException} is thrown.
   *
   * <p> It must be called before sending the request otherwise an {@code IllegalStateException} is thrown.
   *
   * @param fullMethodName the full method name to call
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GrpcClientRequest<Req, Resp> fullMethodName(String fullMethodName);

  /**
   * Set the service name to call.
   *
   * <p> It must be called before sending the request otherwise an {@code IllegalStateException} is thrown.
   *
   * @param serviceName the service name to call
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GrpcClientRequest<Req, Resp> serviceName(ServiceName serviceName);

  /**
   * Set the method name to call.
   *
   * <p> It must be called before sending the request otherwise an {@code IllegalStateException} is thrown.
   *
   * @param methodName the method name to call
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GrpcClientRequest<Req, Resp> methodName(String methodName);

  /**
   * @return the gRPC response
   */
  @CacheReturn
  Future<GrpcClientResponse<Req, Resp>> response();

  @Override
  GrpcClientRequest<Req, Resp> exceptionHandler(@Nullable Handler<Throwable> handler);

  @Override
  GrpcClientRequest<Req, Resp> setWriteQueueMaxSize(int maxSize);

  @Override
  GrpcClientRequest<Req, Resp> drainHandler(@Nullable Handler<Void> handler);

  /**
   * @return the underlying HTTP connection
   */
  HttpConnection connection();

  default Future<GrpcClientResponse<Req, Resp>> send(Req item) {
    this.end(item);
    return this.response();
  }

  default Future<GrpcClientResponse<Req, Resp>> send(ReadStream<Req> body) {
    body.pipeTo(this);
    return this.response();
  }
}
