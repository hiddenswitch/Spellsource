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

import io.grpc.MethodDescriptor;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.grpc.server.impl.GrpcServerImpl;

/**
 * A gRPC server based on Vert.x HTTP server.
 *
 * <p> The server can be used as a {@link io.vertx.core.http.HttpServer} handler or mounted as a Vert.x Web handler.
 *
 * <p> Unlike traditional gRPC servers, this server does not rely on a generated RPC interface to interact with the service.
 *
 * Instead, you can interact with the service with a request/response interfaces and gRPC messages, very much like
 * a traditional client.
 *
 * The server exposes 2 levels of handlers
 *
 * <ul>
 *   <li>a Protobuf message {@link #callHandler(Handler) handler}: {@link GrpcServerRequest}/{@link GrpcServerResponse} with Protobuf message that handles any method call in a generic way</li>
 *   <li>a gRPC message {@link #callHandler(MethodDescriptor, Handler) handler}: {@link GrpcServerRequest}/{@link GrpcServerRequest} with gRPC messages that handles specific service method calls</li>
 * </ul>
 *
 */
@VertxGen
public interface GrpcServer extends Handler<HttpServerRequest> {

  /**
   * Create a blank gRPC server
   *
   * @param vertx the vertx instance
   * @return the created server
   */
  static GrpcServer server(Vertx vertx) {
    return new GrpcServerImpl(vertx);
  }

  /**
   * Set a call handler that handles any call made to the server.
   *
   * @param handler the service method call handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  GrpcServer callHandler(Handler<GrpcServerRequest<Buffer, Buffer>> handler);

  /**
   * Set a service method call handler that handles any call call made to the server for the {@link MethodDescriptor} service method.
   *
   * @param handler the service method call handler
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  <Req, Resp> GrpcServer callHandler(MethodDescriptor<Req, Resp> methodDesc, Handler<GrpcServerRequest<Req, Resp>> handler);

  Handler<RoutingContext> routeHandler();

  @Fluent
  GrpcServer mount(Router router);
}
