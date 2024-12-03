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

import io.grpc.MethodDescriptor;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.ReadStream;
import io.vertx.grpc.client.impl.GrpcClientImpl;

import java.util.function.Function;

/**
 * A gRPC client for Vert.x
 *
 * Unlike traditional gRPC clients, this client does not rely on a generated RPC interface to interact with the service.
 *
 * Instead, you can interact with the service with a request/response interfaces and gRPC messages, very much like
 * a traditional client.
 *
 * The client exposes 2 levels of API
 *
 * <ul>
 *   <li>a Protobuf message {@link #request(SocketAddress) API}: {@link GrpcClientRequest}/{@link GrpcClientResponse} with Protobuf messages to call any gRPC service in a generic way</li>
 *   <li>a gRPC message {@link #request(SocketAddress, MethodDescriptor)}: {@link GrpcClientRequest}/{@link GrpcClientRequest} with gRPC messages to call a given method of a gRPC service</li>
 * </ul>
 */
@VertxGen
public interface GrpcClient {

  /**
   * Create a new client
   *
   * @param vertx the vertx instance
   * @return the created client
   */
  static GrpcClient client(Vertx vertx) {
    return new GrpcClientImpl(vertx);
  }

  /**
   * Create a new client
   *
   * @param vertx the vertx instance
   * @param options the client options
   * @return the created client
   */
  static GrpcClient client(Vertx vertx, HttpClientOptions options) {
    return new GrpcClientImpl(options, vertx);
  }

  /**
   * Connect to the remote {@code server} and create a request for any hosted gRPC service.
   *
   * @param server the server hosting the service
   * @return a future request
   */
  Future<GrpcClientRequest<Buffer, Buffer>> request(SocketAddress server);

  /**
   * Connect to the remote {@code server} and create a request for given {@code method} of a hosted gRPC service.
   *
   * @param server the server hosting the service
   * @param service the service to be called
   * @return a future request
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(SocketAddress server, MethodDescriptor<Req, Resp> service);

  /**
   * Call the {@code service} gRPC service hosted by {@code server}.
   * <p>
   *   The {@code requestHandler} is called to send the request, e.g. {@code req -> req.send(item)}
   * <p>
   *   The {@code responseFunction} extracts the result, e.g. {@code resp -> resp.last()}
   * <p>
   *   It can be used in various ways:
   * <ul>
   *   <li>{@code Future<Resp> fut = client.call(..., req -> req.send(item), resp -> resp.last());}</li>
   *   <li>{@code Future<Void> fut = client.call(..., req -> req.send(stream), resp -> resp.pipeTo(anotherService));}</li>
   *   <li>{@code Future<List<Resp>> fut = client.call(..., req -> req.send(stream), resp -> resp.collecting(Collectors.toList()));}</li>
   * </ul>
   * <pre>
   *
   * @param server the server hosting the service
   * @param service the service to call
   * @param requestHandler the handler called to send the request
   * @param resultFn the function applied to extract the result.
   * @return a future of the result
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default <Req, Resp, T> Future<T> call(SocketAddress server, MethodDescriptor<Req, Resp> service, Handler<GrpcClientRequest<Req, Resp>> requestHandler, Function<GrpcClientResponse<Req, Resp>, Future<T>> resultFn) {
    return request(server, service).compose(req -> {
      requestHandler.handle(req);
      return req
        .response()
        .compose(resultFn);
    });
  }

  /**
   * Close this client.
   */
  Future<Void> close();

}
