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

    private long maxMessageSize;
    private boolean processed;
    private Buffer buffer;
    private Object result;

    @Override
    public void maxMessageSize(long maxMessageSize) {
      this.maxMessageSize = maxMessageSize;
    }

    @Override
    public void update(Buffer chunk) {
      if (processed) {
        return;
      }
      if (buffer == null) {
        buffer = chunk;
      } else {
        try {
          buffer.appendBuffer(chunk);
        } catch (IndexOutOfBoundsException e) {
          // Work around because we cannot happend to slices
          //          java.lang.IndexOutOfBoundsException: writerIndex(270) + minWritableBytes(120) exceeds maxCapacity(270): UnpooledSlicedByteBuf(ridx: 0, widx: 270, cap: 270/270, unwrapped: VertxUnsafeHeapByteBuf(ridx: 0, widx: 0, cap: 270))
          //          at io.netty.buffer@4.2.0.RC3/io.netty.buffer.AbstractByteBuf.ensureWritable0(AbstractByteBuf.java:294)
          //          at io.netty.buffer@4.2.0.RC3/io.netty.buffer.AbstractByteBuf.ensureWritable(AbstractByteBuf.java:280)
          //          at io.netty.buffer@4.2.0.RC3/io.netty.buffer.AbstractByteBuf.writeBytes(AbstractByteBuf.java:1103)
          //          at io.vertx.core@5.0.0-SNAPSHOT/io.vertx.core.buffer.impl.BufferImpl.appendBuffer(BufferImpl.java:256)
          //          at io.vertx.core@5.0.0-SNAPSHOT/io.vertx.core.buffer.impl.BufferImpl.appendBuffer(BufferImpl.java:41)
          buffer = buffer.copy();
          buffer.appendBuffer(chunk);
        }
      }
      if (result == null && buffer.length() > maxMessageSize) {
        result = new MessageSizeOverflowException(buffer.length());
        buffer = null;
        processed = true;
      }
    }

    @Override
    public void end() {
      if (!processed) {
        BufferInternal decoded = BufferInternal.buffer(Base64.decode(((BufferInternal)buffer).getByteBuf()));
        buffer = null;
        result = GrpcMessage.message("identity", decoded.slice(5, decoded.length()));
      }
    }

    @Override
    public Object next() {
      if (result != null) {
        Object ret = result;
        result = null;
        return ret;
      } else {
        return null;
      }
    }
  }

  public WebGrpcServerRequest(ContextInternal context, GrpcProtocol protocol, WireFormat format, long maxMessageSize, HttpServerRequest httpRequest, GrpcMessageDecoder<Req> messageDecoder, GrpcMethodCall methodCall) {
    super(context, protocol, format, httpRequest, httpRequest.version() != HttpVersion.HTTP_2 && GrpcMediaType.isGrpcWebText(httpRequest.getHeader(CONTENT_TYPE)) ? new TextMessageDeframer() : new Http2GrpcMessageDeframer(httpRequest.headers().get(GrpcHeaderNames.GRPC_ENCODING), format), messageDecoder, methodCall);
  }
}
