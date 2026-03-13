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

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.Expectation;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClientResponse;

import io.vertx.core.internal.ContextInternal;
import io.vertx.grpc.client.GrpcClientResponse;
import io.vertx.grpc.client.InvalidStatusException;
import io.vertx.grpc.common.*;
import io.vertx.grpc.common.impl.GrpcReadStreamBase;
import io.vertx.grpc.common.impl.Http2GrpcMessageDeframer;

import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GrpcClientResponseImpl<Req, Resp> extends GrpcReadStreamBase<GrpcClientResponseImpl<Req, Resp>, Resp> implements GrpcClientResponse<Req, Resp> {

  private final GrpcClientRequestImpl<Req, Resp> request;
  private final HttpClientResponse httpResponse;
  private GrpcStatus status;
  private String statusMessage;

  public GrpcClientResponseImpl(ContextInternal context,
                                GrpcClientRequestImpl<Req, Resp> request,
                                WireFormat format,
                                GrpcStatus status,
                                HttpClientResponse httpResponse, GrpcMessageDecoder<Resp> messageDecoder) {
    super(
      context,
      httpResponse,
      httpResponse.headers().get(GrpcHeaderNames.GRPC_ENCODING),
      format,
      new Http2GrpcMessageDeframer(httpResponse.headers().get(GrpcHeaderNames.GRPC_ENCODING), format),
      messageDecoder);
    this.request = request;
    this.httpResponse = httpResponse;
    this.status = status;
  }

  @Override
  public MultiMap headers() {
    return httpResponse.headers();
  }

  @Override
  public MultiMap trailers() {
    return httpResponse.trailers();
  }

  protected void handleEnd() {
    request.cancelTimeout();
    if (status == null) {
      String responseStatus = httpResponse.getTrailer("grpc-status");
      if (responseStatus != null) {
        status = GrpcStatus.valueOf(Integer.parseInt(responseStatus));
      } else {
        status = GrpcStatus.UNKNOWN;
      }
    }
    super.handleEnd();
    if (!request.isTrailersSent()) {
      request.cancel();
    }
  }

  @Override
  public GrpcStatus status() {
    return status;
  }

  @Override
  public String statusMessage() {
    if (status != null && status != GrpcStatus.OK) {
      String msg = httpResponse.getHeader(GrpcHeaderNames.GRPC_MESSAGE);
      if (msg != null) {
        statusMessage = QueryStringDecoder.decodeComponent(msg, StandardCharsets.UTF_8);
      }
    }
    return statusMessage;
  }

  @Override
  public Future<Void> end() {
    return super.end()
      .expecting(new Expectation<>() {
        @Override
        public boolean test(Void value) {
          return status() == GrpcStatus.OK;
        }
        @Override
        public Throwable describe(Void value) {
          return new InvalidStatusException(GrpcStatus.OK, status());
        }
      });
  }

  @Override
  public GrpcClientResponseImpl<Req, Resp> handler(Handler<Resp> handler) {
    if (handler != null) {
      return messageHandler(msg -> {
        Resp decoded;
        try {
          decoded = decodeMessage(msg);
        } catch (CodecException e) {
          request.cancel();
          return;
        }
        handler.handle(decoded);
      });
    } else {
      return messageHandler(null);
    }
  }
}
