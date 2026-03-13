package io.vertx.grpc.common;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.MethodDescriptor;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public interface GrpcMessageEncoder<T> {

  /**
   * Create an encoder for arbitrary message extending {@link MessageLite}.
   * @return the message encoder
   */
  @GenIgnore
  static <T extends MessageLite> GrpcMessageEncoder<T> encoder() {
    return new GrpcMessageEncoder<T>() {
      @Override
      public GrpcMessage encode(T msg, WireFormat format) throws CodecException {
        switch (format) {
          case PROTOBUF:
            byte[] bytes = msg.toByteArray();
            return GrpcMessage.message("identity", Buffer.buffer(bytes));
          case JSON:
            if (msg instanceof MessageOrBuilder) {
              MessageOrBuilder mob = (MessageOrBuilder) msg;
              try {
                String res = JsonFormat.printer().print(mob);
                return GrpcMessage.message("identity", WireFormat.JSON, Buffer.buffer(res));
              } catch (InvalidProtocolBufferException e) {
                throw new CodecException(e);
              }
            }
            return GrpcMessage.message(
              "identity",
              WireFormat.JSON,
              Json.encodeToBuffer(msg));
          default:
            throw new IllegalArgumentException("Invalid wire format: " + format);
        }
      }
      @Override
      public boolean accepts(WireFormat format) {
        return true;
      }
    };
  }

  GrpcMessageEncoder<Buffer> IDENTITY = new GrpcMessageEncoder<>() {
    @Override
    public GrpcMessage encode(Buffer msg, WireFormat format) throws CodecException {
      return GrpcMessage.message("identity", format, msg);
    }
    @Override
    public boolean accepts(WireFormat format) {
      return true;
    }
  };

  /**
   * Create and reutrn an encoder in JSON format encoding instances of {@link MessageOrBuilder} using the protobuf-java-util library
   * otherwise using {@link Json#encodeToBuffer(Object)} (Jackson Databind is required).
   *
   * @return an encoder in JSON format encoding instances of {@code <T>}.
   */
  static <T> GrpcMessageEncoder<T> json() {
    return new GrpcMessageEncoder<>() {
      @Override
      public GrpcMessage encode(T msg, WireFormat format) throws CodecException {
        if (msg instanceof MessageOrBuilder) {
          MessageOrBuilder mob = (MessageOrBuilder) msg;
          try {
            String res = JsonFormat.printer().print(mob);
            return GrpcMessage.message("identity", WireFormat.JSON, Buffer.buffer(res));
          } catch (InvalidProtocolBufferException e) {
            throw new CodecException(e);
          }
        }
        return GrpcMessage.message(
          "identity",
          WireFormat.JSON,
          Json.encodeToBuffer(msg));
      }
      @Override
      public boolean accepts(WireFormat format) {
        return format == WireFormat.JSON;
      }
    };
  }

  /**
   * An encoder in JSON format encoding {@link JsonObject} instances.
   */
  GrpcMessageEncoder<JsonObject> JSON_OBJECT = new GrpcMessageEncoder<>() {
    @Override
    public GrpcMessage encode(JsonObject msg, WireFormat format) throws CodecException {
      return GrpcMessage.message("identity", WireFormat.JSON, msg == null ? Buffer.buffer("null") : msg.toBuffer());
    }
    @Override
    public boolean accepts(WireFormat format) {
      return format == WireFormat.JSON;
    }
  };

  GrpcMessage encode(T msg, WireFormat format) throws CodecException;

  boolean accepts(WireFormat format);

  /**
   * Bridge from a gRPC-Java {@link MethodDescriptor.Marshaller} to a {@link GrpcMessageEncoder}.
   * Backward compatibility for Vert.x gRPC v4 generated stubs.
   */
  static <T> GrpcMessageEncoder<T> marshaller(MethodDescriptor.Marshaller<T> marshaller) {
    return new GrpcMessageEncoder<>() {
      @Override
      public GrpcMessage encode(T msg, WireFormat format) throws CodecException {
        try {
          InputStream stream = marshaller.stream(msg);
          byte[] bytes = stream.readAllBytes();
          return GrpcMessage.message("identity", Buffer.buffer(bytes));
        } catch (Exception e) {
          throw new CodecException(e);
        }
      }
      @Override
      public boolean accepts(WireFormat format) {
        return true;
      }
    };
  }

}
