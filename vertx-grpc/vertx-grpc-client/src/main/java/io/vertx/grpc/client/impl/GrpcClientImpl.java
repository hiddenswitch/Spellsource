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

import io.grpc.MethodDescriptor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientRequest;
import io.vertx.grpc.common.GrpcMessageDecoder;
import io.vertx.grpc.common.GrpcMessageEncoder;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcClientImpl implements GrpcClient {

  private final Vertx vertx;
  private HttpClient client;

  public GrpcClientImpl(HttpClientOptions options, Vertx vertx) {
    this.vertx = vertx;
    this.client = vertx.createHttpClient(new HttpClientOptions(options)
      .setProtocolVersion(HttpVersion.HTTP_2));
  }

  public GrpcClientImpl(Vertx vertx) {
    this(new HttpClientOptions().setHttp2ClearTextUpgrade(false), vertx);
  }

  @Override public Future<GrpcClientRequest<Buffer, Buffer>> request(SocketAddress server) {
    RequestOptions options = new RequestOptions()
      .setMethod(HttpMethod.POST)
      .setServer(server);
    return client.request(options)
      .map(request -> new GrpcClientRequestImpl<>(request, GrpcMessageEncoder.IDENTITY, GrpcMessageDecoder.IDENTITY));
  }

  @Override public <Req, Resp> Future<GrpcClientRequest<Req, Resp>> request(SocketAddress server, MethodDescriptor<Req, Resp> service) {
    RequestOptions options = new RequestOptions()
      .setMethod(HttpMethod.POST)
      .setServer(server);
    GrpcMessageDecoder<Resp> messageDecoder = GrpcMessageDecoder.unmarshaller(service.getResponseMarshaller());
    GrpcMessageEncoder<Req> messageEncoder = GrpcMessageEncoder.marshaller(service.getRequestMarshaller());
    return client.request(options)
      .map(request -> {
        GrpcClientRequestImpl<Req, Resp> call = new GrpcClientRequestImpl<>(request, messageEncoder, messageDecoder);
        call.fullMethodName(service.getFullMethodName());
        return call;
      });
  }

  @Override
  public Future<Void> close() {
    return client.close();
  }
}
