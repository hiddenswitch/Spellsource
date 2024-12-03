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
package io.vertx.grpc.server.impl;

import io.grpc.MethodDescriptor;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.GrpcMessageEncoder;
import io.vertx.grpc.common.impl.GrpcMethodCall;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcServerImpl implements GrpcServer {

  private final Vertx vertx;
  private Handler<GrpcServerRequest<Buffer, Buffer>> requestHandler;
  private Map<String, MethodCallHandler<?, ?>> methodCallHandlers = new HashMap<>();
  private List<Router> routers = new ArrayList<>();

  public GrpcServerImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void handle(HttpServerRequest httpRequest) {
    handle(httpRequest, null);
  }

  public void handle(HttpServerRequest httpRequest, RoutingContext routingContext) {
    GrpcMethodCall methodCall = new GrpcMethodCall(httpRequest.path());
    String fmn = methodCall.fullMethodName();
    MethodCallHandler<?, ?> method = methodCallHandlers.get(fmn);
    if (method != null) {
      handle(method, httpRequest, routingContext, methodCall);
    } else {
      Handler<GrpcServerRequest<Buffer, Buffer>> handler = requestHandler;
      if (handler != null) {
        GrpcServerRequestImpl<Buffer, Buffer> grpcRequest = new GrpcServerRequestImpl<>(httpRequest, routingContext, GrpcMessageDecoder.IDENTITY, GrpcMessageEncoder.IDENTITY, methodCall);
        grpcRequest.init();
        handler.handle(grpcRequest);
      } else {
        httpRequest.response().setStatusCode(500).end();
      }
    }
  }

  private <Req, Resp> void handle(MethodCallHandler<Req, Resp> method, HttpServerRequest httpRequest, RoutingContext routingContext, GrpcMethodCall methodCall) {
    GrpcServerRequestImpl<Req, Resp> grpcRequest = new GrpcServerRequestImpl<>(httpRequest, routingContext, method.messageDecoder, method.messageEncoder, methodCall);
    grpcRequest.init();
    method.handle(grpcRequest);
  }

  public GrpcServer callHandler(Handler<GrpcServerRequest<Buffer, Buffer>> handler) {
    this.requestHandler = handler;
    return this;
  }

  public <Req, Resp> GrpcServer callHandler(MethodDescriptor<Req, Resp> methodDesc, Handler<GrpcServerRequest<Req, Resp>> handler) {
    if (handler != null) {
      MethodCallHandler<Req, Resp> methodCallHandler = new MethodCallHandler<>(methodDesc, GrpcMessageDecoder.unmarshaller(methodDesc.getRequestMarshaller()), GrpcMessageEncoder.marshaller(methodDesc.getResponseMarshaller()), handler);
      methodCallHandlers.put(methodDesc.getFullMethodName(), methodCallHandler);
      for (Router router : routers) {
        addToRouter(router, methodCallHandler);
      }
    } else {
      MethodCallHandler<?, ?> methodCallHandler = methodCallHandlers.remove(methodDesc.getFullMethodName());
      if (methodCallHandler != null) {
        removeFromRouter(methodCallHandler);
      }
    }
    return this;
  }

  private void removeFromRouter(MethodCallHandler<?, ?> methodCallHandler) {
    for (Route route : methodCallHandler.routes) {
      route.remove();
    }
    methodCallHandler.routes.clear();
  }

  @Override
  public Handler<RoutingContext> routeHandler() {
    return ctx -> handle(ctx.request(), ctx);
  }

  @Override
  public GrpcServer mount(Router router) {
    routers.add(router);
    for (MethodCallHandler<?, ?> methodCallHandler : methodCallHandlers.values()) {
      addToRouter(router, methodCallHandler);
    }
    return this;
  }

  private void addToRouter(Router router, MethodCallHandler<?, ?> methodCallHandler) {
    Route route = router.route(HttpMethod.POST, '/' + methodCallHandler.def.getFullMethodName())
      .consumes("application/grpc")
      .handler(this.routeHandler());
    methodCallHandler.routes.add(route);
  }

  private static class MethodCallHandler<Req, Resp> implements Handler<GrpcServerRequest<Req, Resp>> {

    final MethodDescriptor<Req, Resp> def;
    final GrpcMessageDecoder<Req> messageDecoder;
    final GrpcMessageEncoder<Resp> messageEncoder;
    final Handler<GrpcServerRequest<Req, Resp>> handler;
    final List<Route> routes = new ArrayList<>();

    MethodCallHandler(MethodDescriptor<Req, Resp> def, GrpcMessageDecoder<Req> messageDecoder, GrpcMessageEncoder<Resp> messageEncoder, Handler<GrpcServerRequest<Req, Resp>> handler) {
      this.def = def;
      this.messageDecoder = messageDecoder;
      this.messageEncoder = messageEncoder;
      this.handler = handler;
    }

    @Override
    public void handle(GrpcServerRequest<Req, Resp> grpcRequest) {
      handler.handle(grpcRequest);
    }
  }
}
