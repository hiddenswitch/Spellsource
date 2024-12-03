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
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.streaming.Empty;
import io.grpc.examples.streaming.Item;
import io.grpc.examples.streaming.StreamingGrpc;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class ServerTest extends ServerTestBase {

  static final int NUM_ITEMS = 128;

  @Test
  public void testUnary(TestContext should) {
    testUnary(should, "identity", "identity");
  }

  @Test
  public void testUnaryDecompression(TestContext should) {
    testUnary(should, "gzip", "identity");
  }

  @Test
  public void testUnaryCompression(TestContext should) {
    testUnary(should, "identity", "gzip");
  }

  protected void testUnary(TestContext should, String requestEncoding, String responseEncoding) {
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
      }))
      .withCompression(requestEncoding);
    HelloRequest request = HelloRequest.newBuilder().setName("Julien").build();
    HelloReply res = stub.sayHello(request);
    should.assertEquals("Hello Julien", res.getMessage());
    if (!responseEncoding.equals("identity")) {
      should.assertEquals(responseEncoding, responseGrpcEncoding.get());
    }
  }

  @Test
  public void testStatus(TestContext should) {
    HelloRequest request = HelloRequest.newBuilder().setName("Julien").build();
    channel = ManagedChannelBuilder.forAddress( "localhost", port)
      .usePlaintext()
      .build();
    GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);
    try {
      stub.sayHello(request);
    } catch (StatusRuntimeException e) {
      should.assertEquals(Status.UNAVAILABLE, e.getStatus());
    }
  }

  @Test
  public void testServerStreaming(TestContext should) {
    channel = ManagedChannelBuilder.forAddress("localhost", port)
      .usePlaintext()
      .build();
    StreamingGrpc.StreamingBlockingStub stub = StreamingGrpc.newBlockingStub(channel);

    List<String> items = new ArrayList<>();
    stub.source(Empty.newBuilder().build()).forEachRemaining(item -> items.add(item.getValue()));
    List<String> expected = IntStream.rangeClosed(0, NUM_ITEMS - 1).mapToObj(val -> "the-value-" + val).collect(Collectors.toList());
    should.assertEquals(expected, items);
  }

  @Test
  public void testClientStreaming(TestContext should) throws Exception {
    channel = ManagedChannelBuilder.forAddress("localhost", port)
      .usePlaintext()
      .build();
    StreamingGrpc.StreamingStub stub = StreamingGrpc.newStub(channel);

    Async test = should.async();
    StreamObserver<Item> items = stub.sink(new StreamObserver<Empty>() {
      @Override
      public void onNext(Empty value) {
      }
      @Override
      public void onError(Throwable t) {
        should.fail(t);
      }
      @Override
      public void onCompleted() {
        test.complete();
      }
    });
    for (int i = 0; i < NUM_ITEMS; i++) {
      items.onNext(Item.newBuilder().setValue("the-value-" + i).build());
      Thread.sleep(10);
    }
    items.onCompleted();
  }

  @Test
  public void testClientStreamingCompletedBeforeHalfClose(TestContext should) {
    channel = ManagedChannelBuilder.forAddress("localhost", port)
      .usePlaintext()
      .build();
    StreamingGrpc.StreamingStub stub = StreamingGrpc.newStub(channel);

    Async test = should.async();
    StreamObserver<Item> items = stub.sink(new StreamObserver<Empty>() {
      @Override
      public void onNext(Empty value) {
        should.fail();
      }
      @Override
      public void onError(Throwable t) {
        test.complete();
      }
      @Override
      public void onCompleted() {
        should.fail();
      }
    });
    items.onNext(Item.newBuilder().setValue("the-value").build());
  }

  @Test
  public void testBidiStreaming(TestContext should) throws Exception {
    channel = ManagedChannelBuilder.forAddress("localhost", port)
      .usePlaintext()
      .build();
    StreamingGrpc.StreamingStub stub = StreamingGrpc.newStub(channel);

    Async test = should.async();
    List<String> items = new ArrayList<>();
    StreamObserver<Item> writer = stub.pipe(new StreamObserver<Item>() {
      @Override
      public void onNext(Item item) {
        items.add(item.getValue());
      }
      @Override
      public void onError(Throwable t) {
        should.fail(t);
      }
      @Override
      public void onCompleted() {
        test.complete();
      }
    });
    for (int i = 0; i < NUM_ITEMS; i++) {
      writer.onNext(Item.newBuilder().setValue("the-value-" + i).build());
      Thread.sleep(10);
    }
    writer.onCompleted();
    test.awaitSuccess(20_000);
    List<String> expected = IntStream.rangeClosed(0, NUM_ITEMS - 1).mapToObj(val -> "the-value-" + val).collect(Collectors.toList());
    should.assertEquals(expected, items);
  }

  @Test
  public void testBidiStreamingCompletedBeforeHalfClose(TestContext should) throws Exception {
    channel = ManagedChannelBuilder.forAddress("localhost", port)
      .usePlaintext()
      .build();
    StreamingGrpc.StreamingStub stub = StreamingGrpc.newStub(channel);

    Async test = should.async();
    StreamObserver<Item> writer = stub.pipe(new StreamObserver<Item>() {
      @Override
      public void onNext(Item item) {
        should.fail();
      }
      @Override
      public void onError(Throwable t) {
        should.fail(t);
      }
      @Override
      public void onCompleted() {
        test.complete();
      }
    });
    writer.onNext(Item.newBuilder().setValue("the-value").build());
  }

  protected AtomicInteger testMetadataStep;

  @Test
  public void testMetadata(TestContext should) {

    testMetadataStep = new AtomicInteger();

    channel = ManagedChannelBuilder.forAddress("localhost", port)
      .usePlaintext()
      .build();

    ClientInterceptor interceptor = new ClientInterceptor() {
      @Override
      public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
          @Override
          public void start(Listener<RespT> responseListener, Metadata headers) {
            headers.put(Metadata.Key.of("custom_request_header", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "custom_request_header_value");
            headers.put(Metadata.Key.of("custom_request_header-bin", Metadata.BINARY_BYTE_MARSHALLER), new byte[]{0,1,2});
            headers.put(Metadata.Key.of("grpc-custom_request_header", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "grpc-custom_request_header_value");
            headers.put(Metadata.Key.of("grpc-custom_request_header-bin", io.grpc.Metadata.BINARY_BYTE_MARSHALLER), new byte[]{2,1,0});
            super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
              @Override
              public void onHeaders(Metadata headers) {
                should.assertEquals("custom_response_header_value", headers.get(Metadata.Key.of("custom_response_header", Metadata.ASCII_STRING_MARSHALLER)));
                assertEquals(should, new byte[] { 0,1,2 }, headers.get(Metadata.Key.of("custom_response_header-bin", Metadata.BINARY_BYTE_MARSHALLER)));
                should.assertEquals("grpc-custom_response_header_value", headers.get(Metadata.Key.of("grpc-custom_response_header", Metadata.ASCII_STRING_MARSHALLER)));
                assertEquals(should, new byte[] { 2,1,0 }, headers.get(Metadata.Key.of("grpc-custom_response_header-bin", Metadata.BINARY_BYTE_MARSHALLER)));
                int step = testMetadataStep.getAndIncrement();
                should.assertTrue(step == 2 || step == 3, "Was expected " + step + " 3 or " + step + " == 4");
                super.onHeaders(headers);
              }
              @Override
              public void onClose(Status status, Metadata trailers) {
                should.assertEquals("custom_response_trailer_value", trailers.get(Metadata.Key.of("custom_response_trailer", Metadata.ASCII_STRING_MARSHALLER)));
                assertEquals(should, new byte[] { 0,1,2 }, trailers.get(Metadata.Key.of("custom_response_trailer-bin", Metadata.BINARY_BYTE_MARSHALLER)));
                should.assertEquals("grpc-custom_response_trailer_value", trailers.get(Metadata.Key.of("grpc-custom_response_trailer", Metadata.ASCII_STRING_MARSHALLER)));
                assertEquals(should, new byte[] { 2,1,0 }, trailers.get(Metadata.Key.of("grpc-custom_response_trailer-bin", Metadata.BINARY_BYTE_MARSHALLER)));
                should.assertEquals(4, testMetadataStep.getAndIncrement());
                super.onClose(status, trailers);
              }
            }, headers);
          }
        };
      }
    };

    GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(ClientInterceptors.intercept(channel, interceptor));
    HelloRequest request = HelloRequest.newBuilder().setName("Julien").build();
    HelloReply res = stub.sayHello(request);
    should.assertEquals("Hello Julien", res.getMessage());

    should.assertEquals(5, testMetadataStep.get());
  }

  @Test
  public void testHandleCancel(TestContext should) {
    channel = ManagedChannelBuilder.forAddress("localhost", port)
      .usePlaintext()
      .build();
    StreamingGrpc.StreamingStub stub = StreamingGrpc.newStub(channel);

    Async latch = should.async();
    ClientCallStreamObserver<Item> items = (ClientCallStreamObserver<Item>) stub.pipe(new StreamObserver<Item>() {
      AtomicInteger count = new AtomicInteger();
      @Override
      public void onNext(Item value) {
        if (count.getAndIncrement() == 0) {
          latch.complete();
        }
      }
      @Override
      public void onError(Throwable t) {
      }
      @Override
      public void onCompleted() {
      }
    });
    items.onNext(Item.newBuilder().setValue("the-value").build());
    latch.awaitSuccess(10_000);
    items.cancel("cancelled", new Exception());
  }

  @Test
  public void testTrailersOnly(TestContext should) {
    HelloRequest request = HelloRequest.newBuilder().setName("Julien").build();
    channel = ManagedChannelBuilder.forAddress( "localhost", port)
      .usePlaintext()
      .build();
    GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);
    try {
      stub.sayHello(request);
    } catch (StatusRuntimeException e) {
      Metadata trailers = e.getTrailers();
      should.assertEquals("custom_response_trailer_value", trailers.get(Metadata.Key.of("custom_response_trailer", Metadata.ASCII_STRING_MARSHALLER)));
      assertEquals(should, new byte[] { 0,1,2 }, trailers.get(Metadata.Key.of("custom_response_trailer-bin", Metadata.BINARY_BYTE_MARSHALLER)));
      should.assertEquals("grpc-custom_response_trailer_value", trailers.get(Metadata.Key.of("grpc-custom_response_trailer", Metadata.ASCII_STRING_MARSHALLER)));
      assertEquals(should, new byte[] { 2,1,0 }, trailers.get(Metadata.Key.of("grpc-custom_response_trailer-bin", Metadata.BINARY_BYTE_MARSHALLER)));
      should.assertEquals(Status.INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
      should.assertEquals("grpc-status-message-value +*~", e.getStatus().getDescription());
    }
  }

  protected static void assertEquals(TestContext should, byte[] expected, byte[] actual) {
    should.assertNotNull(actual);
    should.assertTrue(Arrays.equals(expected, actual));
  }

  protected static void assertEquals(TestContext should, byte[] expected, String actual) {
    should.assertNotNull(actual);
    should.assertTrue(Arrays.equals(expected, Base64.getDecoder().decode(actual)));
  }
}
