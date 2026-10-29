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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.buffer.BufferInternal;
import io.vertx.grpc.common.*;
import io.vertx.grpc.common.impl.GrpcMessageDeframer;
import io.vertx.grpc.common.impl.GrpcMethodCall;
import io.vertx.grpc.common.impl.Http2GrpcMessageDeframer;
import io.vertx.grpc.server.GrpcProtocol;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

public class WebGrpcServerRequest<Req, Resp> extends GrpcServerRequestImpl<Req, Resp> {

  static class TextMessageDeframer implements GrpcMessageDeframer {

    private final Http2GrpcMessageDeframer inner;
    private Buffer pending;

    TextMessageDeframer(String encoding, WireFormat format) {
      this.inner = new Http2GrpcMessageDeframer(encoding, format);
    }

    @Override
    public void maxMessageSize(long maxMessageSize) {
      this.inner.maxMessageSize(maxMessageSize);
    }

    @Override
    public void update(Buffer chunk) {
      if (chunk == null || chunk.length() == 0) {
        return;
      }
      if (pending == null) {
        pending = chunk;
      } else {
        try {
          pending.appendBuffer(chunk);
        } catch (IndexOutOfBoundsException e) {
          pending = pending.copy();
          pending.appendBuffer(chunk);
        }
      }
      int totalLen = pending.length();
      int usableEnd = (totalLen / 4) * 4;
      if (usableEnd == 0) {
        return;
      }
      int start = 0;
      for (int i = 0; i + 4 <= usableEnd; i += 4) {
        if (pending.getByte(i + 2) == (byte) '=' || pending.getByte(i + 3) == (byte) '=') {
          Buffer slice = pending.slice(start, i + 4);
          BufferInternal decoded = BufferInternal.buffer(Base64.decode(((BufferInternal) slice).getByteBuf()));
          inner.update(decoded);
          start = i + 4;
        }
      }
      if (start < usableEnd) {
        Buffer slice = pending.slice(start, usableEnd);
        BufferInternal decoded = BufferInternal.buffer(Base64.decode(((BufferInternal) slice).getByteBuf()));
        inner.update(decoded);
        start = usableEnd;
      }
      if (start >= totalLen) {
        pending = null;
      } else if (start > 0) {
        pending = pending.getBuffer(start, totalLen);
      }
    }

    @Override
    public void end() {
      if (pending != null && pending.length() > 0) {
        BufferInternal decoded = BufferInternal.buffer(Base64.decode(((BufferInternal) pending).getByteBuf()));
        pending = null;
        if (decoded.length() > 0) {
          inner.update(decoded);
        }
      }
      inner.end();
    }

    @Override
    public Object next() {
      return inner.next();
    }
  }

  public WebGrpcServerRequest(ContextInternal context, GrpcProtocol protocol, WireFormat format, long maxMessageSize, HttpServerRequest httpRequest, GrpcMessageDecoder<Req> messageDecoder, GrpcMethodCall methodCall) {
    super(context, protocol, format, httpRequest, protocol == GrpcProtocol.WEB_TEXT || GrpcMediaType.isGrpcWebText(httpRequest.getHeader(CONTENT_TYPE)) ? new TextMessageDeframer(httpRequest.headers().get(GrpcHeaderNames.GRPC_ENCODING), format) : new Http2GrpcMessageDeframer(httpRequest.headers().get(GrpcHeaderNames.GRPC_ENCODING), format), messageDecoder, methodCall);
  }
}
