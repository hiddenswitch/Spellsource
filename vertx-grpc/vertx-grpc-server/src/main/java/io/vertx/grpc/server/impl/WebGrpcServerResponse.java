/*
 * Copyright (c) 2011-2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.server.impl;

import io.netty.handler.codec.base64.Base64;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.buffer.BufferInternal;
import io.vertx.grpc.common.GrpcMessageEncoder;
import io.vertx.grpc.server.GrpcProtocol;

import java.util.Map;

import static io.vertx.grpc.server.GrpcProtocol.WEB_TEXT;

public class WebGrpcServerResponse<Req, Resp> extends GrpcServerResponseImpl<Req,Resp> {

  public static Buffer grpcWebEncode(Buffer message) {
    return BufferInternal.buffer(Base64.encode(((BufferInternal)message).getByteBuf(), false));
  }

  private final GrpcProtocol protocol;
  private final HttpServerResponse httpResponse;
  private Buffer trailers;

  public WebGrpcServerResponse(ContextInternal context, GrpcServerRequestImpl<Req, Resp> request, GrpcProtocol protocol, HttpServerResponse httpResponse, GrpcMessageEncoder<Resp> encoder) {
    super(context, request, protocol, httpResponse, encoder);

    this.protocol = protocol;
    this.httpResponse = httpResponse;
  }

  private void appendToTrailers(MultiMap entries) {
    if (trailers == null) {
      trailers = Buffer.buffer();
    }
    for (Map.Entry<String, String> trailer : entries) {
      trailers.appendString(trailer.getKey())
        .appendByte((byte) ':')
        .appendString(trailer.getValue())
        .appendString("\r\n");
    }
  }

  @Override
  protected Buffer encodeMessage(Buffer message, boolean compressed, boolean trailer) {
    message = super.encodeMessage(message, compressed, trailer);
    if (protocol == WEB_TEXT) {
      message = grpcWebEncode(message);
    }
    return message;
  }

  @Override
  protected void setHeaders(String contentType, MultiMap grpcHeaders) {
    httpResponse.setChunked(!isTrailersOnly());
    super.setHeaders(contentType, grpcHeaders);
  }

  @Override
  protected void setTrailers(MultiMap grpcTrailers) {
    if (isTrailersOnly()) {
      if (grpcTrailers != null) {
        encodeGrpcTrailers(grpcTrailers, httpResponse.headers());
      }
    } else {
      MultiMap buffer = HttpHeaders.headers();
      super.encodeGrpcStatus(buffer);
      appendToTrailers(buffer);
      if (grpcTrailers != null) {
        appendToTrailers(grpcTrailers);
      }
    }
  }

  @Override
  protected Future<Void> sendEnd() {
    if (trailers != null) {
      Future<Void> ret = httpResponse.end(encodeMessage(trailers, false, true));
      trailers = null;
      return ret;
    } else {
      return httpResponse.end();
    }
  }
}
