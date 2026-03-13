/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.common.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.compression.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.buffer.BufferInternal;
import io.vertx.grpc.common.CodecException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.function.Function;

public class Utils {

  public static final Function<Buffer, Buffer> GZIP_DECODER = data -> {
    EmbeddedChannel channel = new EmbeddedChannel(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
    channel.config().setAllocator(BufferInternal.buffer().getByteBuf().alloc());
    try {
      ChannelFuture fut = channel.writeOneInbound(((BufferInternal)data).getByteBuf());
      if (fut.isSuccess()) {
        Buffer decoded = null;
        while (true) {
          ByteBuf buf = channel.readInbound();
          if (buf == null) {
            break;
          }
          if (decoded == null) {
            decoded = BufferInternal.buffer(buf);
          } else {
            decoded.appendBuffer(BufferInternal.buffer(buf));
          }
        }
        if (decoded == null) {
          throw new CodecException("Invalid GZIP input");
        }
        return decoded;
      } else {
        throw new CodecException(fut.cause());
      }
    } finally {
      channel.close();
    }
  };

  public static final Function<Buffer, Buffer> GZIP_ENCODER = data -> {
    CompositeByteBuf composite = Unpooled.compositeBuffer();
    GzipOptions options = StandardCompressionOptions.gzip();
    ZlibEncoder encoder = ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP, options.compressionLevel(), options.windowBits(), options.memLevel());
    EmbeddedChannel channel = new EmbeddedChannel(encoder);
    channel.config().setAllocator(BufferInternal.buffer().getByteBuf().alloc());
    channel.writeOutbound(((BufferInternal) data).getByteBuf());
    channel.finish();
    Queue<Object> messages = channel.outboundMessages();
    ByteBuf a;
    while ((a = (ByteBuf) messages.poll()) != null) {
      composite.addComponent(true, a);
    }
    channel.close();
    return BufferInternal.buffer(composite);
  };

  public static String utf8PercentEncode(String s) {
    try {
      return URLEncoder.encode(s, StandardCharsets.UTF_8.name())
        .replace("+", "%20")
        .replace("*", "%2A")
        .replace("~", "%7E");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
