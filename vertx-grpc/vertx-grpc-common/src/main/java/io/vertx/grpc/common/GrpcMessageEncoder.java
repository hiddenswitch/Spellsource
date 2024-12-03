package io.vertx.grpc.common;

import io.grpc.MethodDescriptor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.compression.GzipOptions;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibEncoder;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.VertxByteBufAllocator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;

@VertxGen
public interface GrpcMessageEncoder<T> {

  GrpcMessageEncoder<Buffer> IDENTITY = new GrpcMessageEncoder<Buffer>() {
    @Override
    public GrpcMessage encode(Buffer payload) {
      return GrpcMessage.message("identity", payload);
    }
  };

  GrpcMessageEncoder<Buffer> GZIP = new GrpcMessageEncoder<Buffer>() {
    @Override
    public GrpcMessage encode(Buffer payload) {
      CompositeByteBuf composite = Unpooled.compositeBuffer();
      GzipOptions options = StandardCompressionOptions.gzip();
      ZlibEncoder encoder = ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP, options.compressionLevel(), options.windowBits(), options.memLevel());
      EmbeddedChannel channel = new EmbeddedChannel(encoder);
      channel.config().setAllocator(VertxByteBufAllocator.UNPOOLED_ALLOCATOR);
      channel.writeOutbound(((Buffer) payload).getByteBuf());
      channel.finish();
      Queue<Object> messages = channel.outboundMessages();
      ByteBuf a;
      while ((a = (ByteBuf) messages.poll()) != null) {
        composite.addComponent(true, a);
      }
      channel.close();
      return GrpcMessage.message("gzip", Buffer.buffer(composite));
    }
  };

  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static <T> GrpcMessageEncoder<T> marshaller(MethodDescriptor.Marshaller<T> desc) {
    return new GrpcMessageEncoder<T>() {
      @Override
      public GrpcMessage encode(T msg) {
        Buffer encoded = Buffer.buffer();
        InputStream stream = desc.stream(msg);
        byte[] tmp = new byte[256];
        int i;
        try {
          while ((i = stream.read(tmp)) != -1) {
            encoded.appendBytes(tmp, 0, i);
          }
        } catch (IOException e) {
          throw new VertxException(e);
        }
        return GrpcMessage.message("identity", encoded);
      }
    };
  }

  GrpcMessage encode(T msg);

}
