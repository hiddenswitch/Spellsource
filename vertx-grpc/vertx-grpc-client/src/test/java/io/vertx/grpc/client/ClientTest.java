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
package io.vertx.grpc.client;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.streaming.Empty;
import io.grpc.examples.streaming.Item;
import io.grpc.examples.streaming.StreamingGrpc;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class ClientTest extends ClientTestBase {

  static final int NUM_ITEMS = 128;
  static final int NUM_BATCHES = 5;

  protected GrpcClient client;


  @Test
  public void testUnary(TestContext should) throws IOException {
    testUnary(should, "identity", "identity");
  }

  @Test
  public void testUnaryDecompression(TestContext should) throws IOException {
    testUnary(should, "identity", "gzip");
  }

  @Test
  public void testUnaryCompression(TestContext should) throws IOException {
    testUnary(should, "gzip", "identity");
  }

  protected void testUnary(TestContext should, String requestEncoding, String responseEncoding) throws IOException {
    GreeterGrpc.GreeterImplBase called = new GreeterGrpc.GreeterImplBase() {
      @Override
      public void sayHello(HelloRequest request, StreamObserver<HelloReply> plainResponseObserver) {
        ServerCallStreamObserver<HelloReply> responseObserver =
          (ServerCallStreamObserver<HelloReply>) plainResponseObserver;
        responseObserver.setCompression(responseEncoding);
        responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello " + request.getName()).build());
        responseObserver.onCompleted();
      }
    };
    startServer(called, ServerBuilder.forPort(port).intercept(new ServerInterceptor() {
      @Override
      public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String encodingHeader = headers.get(Metadata.Key.of("grpc-encoding", Metadata.ASCII_STRING_MARSHALLER));
        should.assertEquals(requestEncoding, encodingHeader);
        return next.startCall(call, headers);
      }
    }));
  }

  @Test
  public void testServerStreaming(TestContext should) throws IOException {
    startServer(new StreamingGrpc.StreamingImplBase() {
      @Override
      public void source(Empty request, StreamObserver<Item> responseObserver) {
        for (int i = 0;i < NUM_ITEMS;i++) {
          responseObserver.onNext(Item.newBuilder().setValue("the-value-" + i).build());
        }
        responseObserver.onCompleted();
      }
    });
  }

  protected final ConcurrentLinkedDeque<Integer> batchQueue = new ConcurrentLinkedDeque<>();

  @Test
  public void testServerStreamingBackPressure(TestContext should) throws IOException {
    batchQueue.clear();
    startServer(new StreamingGrpc.StreamingImplBase() {
      @Override
      public void source(Empty request, StreamObserver<Item> responseObserver) {
        ServerCallStreamObserver obs = (ServerCallStreamObserver) responseObserver;
        AtomicInteger numRounds = new AtomicInteger(20);
        Runnable readyHandler = () -> {
          if (numRounds.decrementAndGet() > 0) {
            int num = 0;
            while (obs.isReady()) {
              num++;
              Item item = Item.newBuilder().setValue("the-value-" + num).build();
              responseObserver.onNext(item);
            }
            batchQueue.add(num);
          } else {
            batchQueue.add(-1);
            responseObserver.onCompleted();
          }
        };
        obs.setOnReadyHandler(readyHandler);
      }
    });
  }

  @Test
  public void testClientStreaming(TestContext should) throws Exception {
    startServer(new StreamingGrpc.StreamingImplBase() {
      @Override
      public StreamObserver<Item> sink(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<Item>() {
          final List<String> items = new ArrayList<>();
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
            List<String> expected = IntStream.rangeClosed(0, NUM_ITEMS - 1).mapToObj(val -> "the-value-" + val).collect(Collectors.toList());
            should.assertEquals(expected, items);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
          }
        };
      }
    });
  }

  @Test
  public void testClientStreamingBackPressure(TestContext should) throws Exception {
    startServer(new StreamingGrpc.StreamingImplBase() {
      @Override
      public StreamObserver<Item> sink(StreamObserver<Empty> responseObserver) {
        return sink((ServerCallStreamObserver<Empty>) responseObserver);
      }
      private AtomicBoolean completed = new AtomicBoolean();
      private AtomicInteger toRead = new AtomicInteger();
      final AtomicInteger batchCount = new AtomicInteger();
      private void waitForBatch(ServerCallStreamObserver<Empty> responseObserver) {
        if (batchCount.get() < NUM_BATCHES) {
          batchCount.incrementAndGet();
          new Thread(() -> {
            while (batchQueue.isEmpty()) {
              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            Integer num = batchQueue.poll();
            toRead.addAndGet(num);
            responseObserver.request(num);
          }).start();
        } else if (completed.get()) {
          responseObserver.onNext(Empty.getDefaultInstance());
          responseObserver.onCompleted();
        }
      }
      private StreamObserver<Item> sink(ServerCallStreamObserver<Empty> responseObserver) {
        responseObserver.disableAutoRequest();
        waitForBatch(responseObserver);
        return new StreamObserver<Item>() {
          @Override
          public void onNext(Item item) {
            should.assertEquals("the-value-" + (batchCount.get() - 1), item.getValue());
            if (toRead.decrementAndGet() == 0) {
              waitForBatch(responseObserver);
            }
          }
          @Override
          public void onError(Throwable t) {
            should.fail(t);
          }
          @Override
          public void onCompleted() {
            completed.set(true);
            if (batchCount.get() == NUM_BATCHES) {
              responseObserver.onNext(Empty.getDefaultInstance());
              responseObserver.onCompleted();
            }
          }
        };
      }
    });
  }

  @Test
  public void testClientStreamingCompletedBeforeHalfClose(TestContext should) throws Exception {
    Async latch = should.async();
    startServer(new StreamingGrpc.StreamingImplBase() {
      @Override
      public StreamObserver<Item> sink(StreamObserver<Empty> responseObserver) {
        return new StreamObserver<Item>() {
          @Override
          public void onNext(Item item) {
            responseObserver.onCompleted();
          }
          @Override
          public void onError(Throwable t) {
            latch.complete();
          }
          @Override
          public void onCompleted() {
            should.fail();
          }
        };
      }
    });
  }

  @Test
  public void testBidiStreaming(TestContext should) throws Exception {
    startServer(new StreamingGrpc.StreamingImplBase() {
      @Override
      public StreamObserver<Item> pipe(StreamObserver<Item> responseObserver) {
        return responseObserver;
      }
    });
  }

  @Test
  public void testBidiStreamingCompletedBeforeHalfClose(TestContext should) throws Exception {
    startServer(new StreamingGrpc.StreamingImplBase() {
      @Override
      public StreamObserver<Item> pipe(StreamObserver<Item> responseObserver) {
        return new StreamObserver<Item>() {
          @Override
          public void onNext(Item value) {
            responseObserver.onCompleted();
          }
          @Override
          public void onError(Throwable t) {
            should.fail(t);
          }
          @Override
          public void onCompleted() {
            should.fail();
          }
        };
      }
    });
  }

  @Test
  public void testStatus(TestContext should) throws IOException {

    GreeterGrpc.GreeterImplBase called = new GreeterGrpc.GreeterImplBase() {
      @Override
      public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        responseObserver.onError(Status.UNAVAILABLE
          .withDescription("~Greeter temporarily unavailable...~").asRuntimeException());
      }
    };
    startServer(called);
  }

  @Test
  public void testFail(TestContext should) throws Exception {

    Async done = should.async();
    startServer(new StreamingGrpc.StreamingImplBase() {
      @Override
      public StreamObserver<Item> pipe(StreamObserver<Item> responseObserver) {
        return new StreamObserver<Item>() {
          @Override
          public void onNext(Item item) {
            responseObserver.onNext(item);
          }
          @Override
          public void onError(Throwable t) {
            should.assertEquals(StatusRuntimeException.class, t.getClass());
            StatusRuntimeException ex = (StatusRuntimeException) t;
            should.assertEquals(Status.CANCELLED.getCode(), ex.getStatus().getCode());
            done.complete();
          }
          @Override
          public void onCompleted() {
          }
        };
      }
    });
  }

  protected AtomicInteger testMetadataStep;

  @Test
  public void testMetadata(TestContext should) throws Exception {

    testMetadataStep = new AtomicInteger();
    ServerInterceptor interceptor = new ServerInterceptor() {
      @Override
      public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        should.assertEquals("custom_request_header_value", headers.get(Metadata.Key.of("custom_request_header", Metadata.ASCII_STRING_MARSHALLER)));
        assertEquals(should, new byte[] { 0,1,2 }, headers.get(Metadata.Key.of("custom_request_header-bin", Metadata.BINARY_BYTE_MARSHALLER)));
        should.assertEquals("grpc-custom_request_header_value", headers.get(Metadata.Key.of("grpc-custom_request_header", Metadata.ASCII_STRING_MARSHALLER)));
        assertEquals(should, new byte[] { 2,1,0 }, headers.get(Metadata.Key.of("grpc-custom_request_header-bin", Metadata.BINARY_BYTE_MARSHALLER)));
        should.assertEquals(0, testMetadataStep.getAndIncrement());
        return next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
          @Override
          public void sendHeaders(Metadata headers) {
            headers.put(Metadata.Key.of("custom_response_header", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "custom_response_header_value");
            headers.put(Metadata.Key.of("custom_response_header-bin", Metadata.BINARY_BYTE_MARSHALLER), new byte[] { 0,1,2 });
            headers.put(Metadata.Key.of("grpc-custom_response_header", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "grpc-custom_response_header_value");
            headers.put(Metadata.Key.of("grpc-custom_response_header-bin", Metadata.BINARY_BYTE_MARSHALLER), new byte[] { 2,1,0 });
            should.assertEquals(1, testMetadataStep.getAndIncrement());
            super.sendHeaders(headers);
          }
          @Override
          public void close(Status status, Metadata trailers) {
            trailers.put(Metadata.Key.of("custom_response_trailer", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "custom_response_trailer_value");
            trailers.put(Metadata.Key.of("custom_response_trailer-bin", Metadata.BINARY_BYTE_MARSHALLER), new byte[] { 0,1,2 });
            trailers.put(Metadata.Key.of("grpc-custom_response_trailer", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "grpc-custom_response_trailer_value");
            trailers.put(Metadata.Key.of("grpc-custom_response_trailer-bin", Metadata.BINARY_BYTE_MARSHALLER), new byte[] { 2,1,0 });
            should.assertEquals(2, testMetadataStep.getAndIncrement());
            super.close(status, trailers);
          }
        },headers);
      }
    };

    GreeterGrpc.GreeterImplBase called = new GreeterGrpc.GreeterImplBase() {

      @Override
      public void sayHello(HelloRequest request, StreamObserver<HelloReply> plainResponseObserver) {
        ServerCallStreamObserver<HelloReply> responseObserver =
          (ServerCallStreamObserver<HelloReply>) plainResponseObserver;
        responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello " + request.getName()).build());
        responseObserver.onCompleted();
      }
    };
    startServer(ServerInterceptors.intercept(called, interceptor));

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
