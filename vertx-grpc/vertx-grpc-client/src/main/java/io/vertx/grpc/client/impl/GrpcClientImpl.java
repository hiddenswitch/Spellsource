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
package io.vertx.grpc.client.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.net.Address;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientOptions;
import io.vertx.grpc.client.GrpcClientRequest;
import io.vertx.grpc.common.ServiceMethod;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.GrpcMessageEncoder;
import io.vertx.grpc.common.GrpcLocal;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcClientImpl implements GrpcClient {

  private final Vertx vertx;
  private HttpClient client;
  private boolean closeClient;
  private final boolean scheduleDeadlineAutomatically;
  private final long maxMessageSize;
  private final int timeout;
  private final TimeUnit timeoutUnit;

  public GrpcClientImpl(Vertx vertx, HttpClient client) {
    this(vertx, new GrpcClientOptions(), client, false);
  }

  protected GrpcClientImpl(Vertx vertx, GrpcClientOptions grpcOptions, HttpClient client, boolean close) {
    this.vertx = vertx;
    this.client = client;
    this.scheduleDeadlineAutomatically = grpcOptions.getScheduleDeadlineAutomatically();
    this.maxMessageSize = grpcOptions.getMaxMessageSize();;
    this.timeout = grpcOptions.getTimeout();
    this.timeoutUnit = grpcOptions.getTimeoutUnit();
    this.closeClient = close;
  }

  public Vertx vertx() {
    return vertx;
  }

  public Future<GrpcClientRequest<Buffer, Buffer>> request(RequestOptions options) {
    return client.request(options)
      .map(httpRequest -> {
        GrpcClientRequestImpl<Buffer, Buffer> grpcRequest = new GrpcClientRequestImpl<>(
          httpRequest,
          maxMessageSize,
          scheduleDeadlineAutomatically,
          GrpcMessageEncoder.IDENTITY,
          GrpcMessageDecoder.IDENTITY);
        grpcRequest.init();
        configureTimeout(grpcRequest);
        return grpcRequest;
      });
  }

  @Override
  public Future<GrpcClientRequest<Buffer, Buffer>> request() {
    return request(new RequestOptions().setMethod(HttpMethod.POST));
  }

  @Override
  public Future<GrpcClientRequest<Buffer, Buffer>> request(Address server) {
    return request(new RequestOptions().setMethod(HttpMethod.POST).setServer(server));
  }

  private void configureTimeout(GrpcClientRequest<?, ?> request) {
    ContextInternal current = (ContextInternal) vertx.getOrCreateContext();
    GrpcLocal local = current.getLocal(GrpcLocal.CONTEXT_LOCAL_KEY);
    long timeout = this.timeout;
    TimeUnit timeoutUnit = this.timeoutUnit;
    if (local != null) {
      timeout = local.deadline().toEpochMilli() - System.currentTimeMillis();
      timeoutUnit = TimeUnit.MILLISECONDS;
      if (timeout < 0L) {
        throw new UnsupportedOperationException("Handle this case");
      }
    }
    request.timeout(timeout, timeoutUnit);
  }

  @Override
  public <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(ServiceMethod<Resp, Req> method) {
    return request(new RequestOptions()
      .setMethod(HttpMethod.POST), method);
  }

  @Override
  public <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(Address server, ServiceMethod<Resp, Req> method) {
    return request(new RequestOptions()
      .setMethod(HttpMethod.POST)
      .setServer(server), method);
  }

  private <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(RequestOptions options, ServiceMethod<Resp, Req> method) {
    return client.request(options)
      .map(request -> {
        GrpcClientRequestImpl<Req, Resp> call = new GrpcClientRequestImpl<>(
          request,
          maxMessageSize,
          scheduleDeadlineAutomatically,
          method.encoder(),
          method.decoder());
        call.init();
        call.serviceName(method.serviceName());
        call.methodName(method.methodName());
        configureTimeout(call);
        return call;
      });
  }

  @Override
  public Future<Void> close() {
    if (closeClient) {
      return client.close();
    } else {
      return ((VertxInternal)vertx).getOrCreateContext().succeededFuture();
    }
  }
}
