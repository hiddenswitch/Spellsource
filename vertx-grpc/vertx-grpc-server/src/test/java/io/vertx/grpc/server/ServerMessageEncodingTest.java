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
package io.vertx.grpc.server;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloRequest;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.StreamResetException;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.grpc.common.GrpcError;
import io.vertx.grpc.common.GrpcMessage;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ServerMessageEncodingTest extends ServerTestBase {

  private HttpClient client;

  @Test
  public void testZipResponseCompress(TestContext should) {
    testEncode(should, "gzip", GrpcMessage.message("identity", Buffer.buffer("Hello World")), true);
  }

  @Test
  public void testZipResponsePassThrough(TestContext should) {
    testEncode(should, "gzip", GrpcMessage.message("gzip", zip(Buffer.buffer("Hello World"))), true);
  }

  @Test
  public void testIdentityResponseUnzip(TestContext should) {
    testEncode(should, "identity", GrpcMessage.message("gzip", zip(Buffer.buffer("Hello World"))), false);
  }

  @Test
  public void testIdentityRequestPassThrough(TestContext should) {
    testEncode(should, "identity", GrpcMessage.message("identity", Buffer.buffer("Hello World")), false);
  }

  private void testEncode(TestContext should, String encoding, GrpcMessage msg, boolean compressed) {

    Buffer expected = Buffer.buffer("Hello World");

    startServer(GrpcServer.server(vertx).callHandler(call -> {
      call.handler(request -> {
        GrpcServerResponse<Buffer, Buffer> response = call.response();
        response
          .encoding(encoding)
          .endMessage(msg);
      });
    }));

    client = vertx.createHttpClient(new HttpClientOptions()
      .setProtocolVersion(HttpVersion.HTTP_2)
      .setHttp2ClearTextUpgrade(true)
    );

    Async done = should.async();

    client
      .request(HttpMethod.POST, 8080, "localhost", "/")
      .onComplete(should.asyncAssertSuccess(request -> {
      request.putHeader("grpc-encoding", "identity");
      request.send(Buffer
        .buffer()
        .appendByte((byte)1)
        .appendInt(expected.length())
        .appendBuffer(expected)).onComplete(should.asyncAssertSuccess(resp -> {
          resp.body().onComplete(should.asyncAssertSuccess(body -> {
            should.assertEquals(compressed ? 1 : 0, (int)body.getByte(0));
            int len = body.getInt(1);
            Buffer received = body.slice(5, 5 + len);
            if (compressed) {
              received = unzip(received);
            }
            should.assertEquals(expected, received);
            done.complete();
          }));
      }));
    }));
  }

  @Test
  public void testEncodeError(TestContext should) {

    startServer(GrpcServer.server(vertx).callHandler(call -> {
      call.handler(request -> {
        GrpcServerResponse<Buffer, Buffer> response = call.response();
        List<GrpcMessage> messages = Arrays.asList(
          GrpcMessage.message("gzip", Buffer.buffer("Hello World")),
          GrpcMessage.message("gzip", Buffer.buffer("not-gzip")),
          GrpcMessage.message("unknown", Buffer.buffer("unknown"))
        );
        response
          .encoding("identity");
        for (GrpcMessage message : messages) {
          Future<Void> fut = response.writeMessage(message);
          fut.onComplete(should.asyncAssertFailure());
        }
        response.cancel();
      });
    }));

    channel = ManagedChannelBuilder.forAddress("localhost", port)
      .usePlaintext()
      .build();

    GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);
    HelloRequest request = HelloRequest.newBuilder().setName("Julien").build();
    try {
      stub.sayHello(request);
    } catch (StatusRuntimeException ignore) {
    }
  }

  @Test
  public void testDecodeMessageHandler(TestContext should) {
    testDecode(should, zip(Buffer.buffer("Hello World")), callResponse -> {
      AtomicInteger count = new AtomicInteger();
      callResponse.messageHandler(msg -> {
        should.assertEquals("gzip", msg.encoding());
        should.assertEquals(Buffer.buffer("Hello World"), unzip(msg.payload()));
        count.incrementAndGet();
      });
      callResponse.endHandler(v -> {
        should.assertEquals(1, count.get());
        callResponse.response().end();
      });
    }, req -> req.response().onComplete(should.asyncAssertSuccess()));
  }

  @Test
  public void testDecodeHandler(TestContext should) {
    testDecode(should, zip(Buffer.buffer("Hello World")), callResponse -> {
      AtomicInteger count = new AtomicInteger();
      callResponse.handler(msg -> {
        should.assertEquals(Buffer.buffer("Hello World"), msg);
        count.incrementAndGet();
      });
      callResponse.endHandler(v -> {
        should.assertEquals(1, count.get());
        callResponse.response().end();
      });
    }, req -> req.response().onComplete(should.asyncAssertSuccess()));
  }

  @Test
  public void testDecodeError(TestContext should) {
    testDecode(should, Buffer.buffer("Hello World"), req -> {
      req.handler(msg -> {
        should.fail();
      });
    }, req -> req.response().onComplete(should.asyncAssertFailure(err -> {
      should.assertEquals(StreamResetException.class, err.getClass());
      StreamResetException reset = (StreamResetException) err;
      should.assertEquals(GrpcError.CANCELLED.http2ResetCode, reset.getCode());
    })));
  }

  private void testDecode(TestContext should, Buffer payload, Consumer<GrpcServerRequest<Buffer, Buffer>> impl, Consumer<HttpClientRequest> checker) {

    startServer(GrpcServer.server(vertx).callHandler(call -> {
      should.assertEquals("gzip", call.encoding());
      impl.accept(call);
    }));

    client = vertx.createHttpClient(new HttpClientOptions()
      .setProtocolVersion(HttpVersion.HTTP_2)
      .setHttp2ClearTextUpgrade(false)
    );

    client.request(HttpMethod.POST, 8080, "localhost", "/").onComplete( should.asyncAssertSuccess(request -> {
      request.putHeader("grpc-encoding", "gzip");
      request.end(Buffer
        .buffer()
        .appendByte((byte)1)
        .appendInt(payload.length())
        .appendBuffer(payload));
      checker.accept(request);
    }));
  }

  // A test to check, gRPC implementation behavior
  @Test
  public void testClientDecodingError(TestContext should) throws Exception {

    Async done = should.async();

    vertx.createHttpServer().requestHandler(req -> {
        req.response()
          .putHeader("content-type", "application/grpc")
          .putHeader("grpc-encoding", "gzip")
          .write(Buffer.buffer()
            .appendByte((byte) 1)
            .appendInt(11)
            .appendString("Hello World"));
        req.response().exceptionHandler(err -> {
          if (err instanceof StreamResetException) {
            StreamResetException reset = (StreamResetException) err;
            should.assertEquals(GrpcError.CANCELLED.http2ResetCode, reset.getCode());
            done.complete();
          }
        });
    }).listen(8080, "localhost")
      .toCompletionStage()
      .toCompletableFuture()
      .get(20, TimeUnit.SECONDS);

    channel = ManagedChannelBuilder.forAddress("localhost", port)
      .usePlaintext()
      .build();

    AtomicReference<String> responseGrpcEncoding = new AtomicReference<>();
    GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(ClientInterceptors.intercept(channel, new ClientInterceptor() {
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
          return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
              super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                @Override
                public void onHeaders(Metadata headers) {
                  responseGrpcEncoding.set(headers.get(Metadata.Key.of("grpc-encoding", io.grpc.Metadata.ASCII_STRING_MARSHALLER)));
                  super.onHeaders(headers);
                }
              }, headers);
            }
          };
        }
      }));
    HelloRequest request = HelloRequest.newBuilder().setName("Julien").build();
    try {
      stub.sayHello(request);
    } catch (StatusRuntimeException ignore) {
    }
  }
}
