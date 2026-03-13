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
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.Address;
import io.vertx.grpc.client.impl.GrpcClientBuilderImpl;
import io.vertx.grpc.client.impl.GrpcClientImpl;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.GrpcMessageEncoder;
import io.vertx.grpc.common.ServiceMethod;
import io.vertx.grpc.common.ServiceName;

/**
 * <p>A gRPC client for Vert.x</p>
 *
 * <p>Unlike traditional gRPC clients, this client does not rely on a generated RPC interface to interact with the service.</p>
 *
 * <p>Instead, you can interact with the service with a request/response interfaces and gRPC messages, very much like
 * a traditional client.</p>
 *
 * <p>The client handles only the gRPC protocol and does not encode/decode protobuf messages.</p>
 */
@VertxGen
public interface GrpcClient {

  /**
   * Provide a builder for {@link GrpcClient}, it can be used to configure advanced
   * client settings like a load balancer or an address resolver.
   * <p>
   * Example usage: {@code GrpcClient client = GrpcClient.builder(vertx).with(options)...build()}
   */
  static GrpcClientBuilder<GrpcClient> builder(Vertx vertx) {
    return new GrpcClientBuilderImpl<>(vertx);
  }

  /**
   * Create a client.
   *
   * @param vertx the vertx instance
   * @return the created client
   */
  static GrpcClient client(Vertx vertx) {
    return builder(vertx).build();
  }

  /**
   * Create a client.
   *
   * @param vertx the vertx instance
   * @return the created client
   */
  static GrpcClient client(Vertx vertx, GrpcClientOptions options) {
    return builder(vertx).with(options).build();
  }

  /**
   * Create a client with the specified {@code options}.
   *
   * @param vertx the vertx instance
   * @param grpcOptions the http client options
   * @param httpOptions the http client options
   * @return the created client
   */
  static GrpcClient client(Vertx vertx, GrpcClientOptions grpcOptions, HttpClientOptions httpOptions) {
    return builder(vertx).with(grpcOptions).with(httpOptions).build();
  }

  /**
   * Create a client with the specified {@code options}.
   *
   * @param vertx the vertx instance
   * @param options the http client options
   * @return the created client
   */
  static GrpcClient client(Vertx vertx, HttpClientOptions options) {
    return builder(vertx).with(options).build();
  }

  /**
   * Create a client wrapping an existing {@link HttpClient}.
   *
   * @param vertx the vertx instance
   * @param client the http client instance
   * @return the created client
   */
  static GrpcClient client(Vertx vertx, HttpClient client) {
    return new GrpcClientImpl(vertx, client);
  }

  /**
   * Connect to the remote {@code server} and create a request for any hosted gRPC service.
   *
   * @param server the server hosting the service
   * @return a future request
   */
  Future<GrpcClientRequest<Buffer, Buffer>> request(Address server);

  /**
   * Like {@link #request(Address)} with the default remote server.
   */
  Future<GrpcClientRequest<Buffer, Buffer>> request();

  /**
   * Connect to the remote {@code server} and create a request for any hosted gRPC service.
   *
   * @param server the server hosting the service
   * @param method the grpc method
   * @return a future request
   */
  <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(Address server, ServiceMethod<Resp, Req> method);

  /**
   * Like {@link #request(Address, ServiceMethod)} with the default remote server.
   */
  <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(ServiceMethod<Resp, Req> method);

  /**
   * Backward-compat bridge: connect using a gRPC-Java {@link MethodDescriptor}.
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(Address server, MethodDescriptor<Req, Resp> methodDesc) {
    GrpcMessageDecoder<Resp> decoder = GrpcMessageDecoder.unmarshaller(methodDesc.getResponseMarshaller());
    GrpcMessageEncoder<Req> encoder = GrpcMessageEncoder.marshaller(methodDesc.getRequestMarshaller());
    ServiceMethod<Resp, Req> serviceMethod = ServiceMethod.client(
      ServiceName.create(methodDesc.getServiceName()),
      methodDesc.getBareMethodName(),
      encoder, decoder);
    return request(server, serviceMethod);
  }

  /**
   * Close this client.
   */
  Future<Void> close();

}
