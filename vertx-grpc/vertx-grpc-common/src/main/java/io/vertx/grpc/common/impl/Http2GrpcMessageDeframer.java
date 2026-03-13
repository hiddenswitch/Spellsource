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
package io.vertx.grpc.common.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.common.MessageSizeOverflowException;
import io.vertx.grpc.common.WireFormat;

/**
 * State machine that handles slicing the input to a message
 */
public class Http2GrpcMessageDeframer implements GrpcMessageDeframer {

  private final String encoding;
  private final WireFormat format;
  private long maxMessageSize;

  private Buffer buffer;
  private long bytesToSkip;

  public Http2GrpcMessageDeframer(String encoding, WireFormat format) {
    this.encoding = encoding;
    this.format = format;
  }

  @Override
  public void maxMessageSize(long maxMessageSize) {
    this.maxMessageSize = maxMessageSize;
  }

  public void update(Buffer chunk) {
    if (bytesToSkip > 0L) {
      int len = chunk.length();
      if (len <= bytesToSkip) {
        bytesToSkip -= len;
        return;
      }
      chunk = chunk.slice((int) bytesToSkip, len);
      bytesToSkip = 0L;
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
  }

  @Override
  public void end() {
  }

  public Object next() {
    if (buffer == null) {
      return null;
    }
    int idx = 0;
    if (idx + 5 > buffer.length()) {
      return null;
    }
    long len = ((long) buffer.getInt(idx + 1)) & 0xFFFFFFFFL;
    if (len > maxMessageSize) {
      MessageSizeOverflowException msoe = new MessageSizeOverflowException(len);
      if (buffer.length() < (len + 5)) {
        bytesToSkip = (len + 5) - buffer.length();
        buffer = null;
      } else {
        buffer = buffer.slice((int) (len + 5), buffer.length());
      }
      return msoe;
    }
    if (len > buffer.length() - (idx + 5)) {
      return null;
    }
    boolean compressed = buffer.getByte(idx) == 1;
    if (compressed && encoding == null) {
      throw new UnsupportedOperationException("Handle me");
    }
    Buffer payload = buffer.slice(idx + 5, (int) (idx + 5 + len));
    GrpcMessage message = GrpcMessage.message(compressed ? encoding : "identity", format, payload);
    idx += 5 + (int) len;
    if (idx < buffer.length()) {
      if (idx > 0) {
        buffer = buffer.getBuffer(idx, buffer.length());
      }
    } else {
      buffer = null;
    }
    return message;
  }
}
