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

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.HttpServerRequestInternal;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.grpc.common.CodecException;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.GrpcMessageEncoder;
import io.vertx.grpc.common.ServiceName;
import io.vertx.grpc.common.impl.GrpcReadStreamBase;
import io.vertx.grpc.common.impl.GrpcMethodCall;
import io.vertx.grpc.server.GrpcServerRequest;
import io.vertx.grpc.server.GrpcServerResponse;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcServerRequestImpl<Req, Resp> extends GrpcReadStreamBase<GrpcServerRequestImpl<Req, Resp>, Req> implements GrpcServerRequest<Req, Resp> {

  final HttpServerRequest httpRequest;
  final RoutingContext routingContext;
  final GrpcServerResponse<Req, Resp> response;
  private GrpcMethodCall methodCall;

  public GrpcServerRequestImpl(HttpServerRequest httpRequest, RoutingContext routingContext, GrpcMessageDecoder<Req> messageDecoder, GrpcMessageEncoder<Resp> messageEncoder, GrpcMethodCall methodCall) {
    super(((HttpServerRequestInternal) httpRequest).context(), httpRequest, httpRequest.headers().get("grpc-encoding"), messageDecoder);
    this.httpRequest = httpRequest;
    this.routingContext = routingContext;
    this.response = new GrpcServerResponseImpl<>(this, httpRequest.response(), messageEncoder);
    this.methodCall = methodCall;
  }

  public String fullMethodName() {
    return methodCall.fullMethodName();
  }

  @Override
  public MultiMap headers() {
    return httpRequest.headers();
  }

  @Override
  public String encoding() {
    return httpRequest.getHeader("grpc-encoding");
  }

  @Override
  public ServiceName serviceName() {
    return methodCall.serviceName();
  }

  @Override
  public String methodName() {
    return methodCall.methodName();
  }

  @Override
  public RoutingContext routingContext() {
    return this.routingContext;
  }

  @Override
  public GrpcServerRequest<Req, Resp> handler(Handler<Req> handler) {
    if (handler != null) {
      return messageHandler(msg -> {
        Req decoded;
        try {
          decoded = decodeMessage(msg);
        } catch (CodecException e) {
          response.cancel();
          return;
        }
        handler.handle(decoded);
      });
    } else {
      return messageHandler(null);
    }
  }

  public GrpcServerResponse<Req, Resp> response() {
    return response;
  }

  @Override
  public HttpConnection connection() {
    return httpRequest.connection();
  }
}
