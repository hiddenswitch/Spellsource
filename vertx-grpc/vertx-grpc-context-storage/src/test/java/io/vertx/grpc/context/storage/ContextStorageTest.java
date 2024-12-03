package io.vertx.grpc.context.storage;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.Context;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServiceBridge;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static io.grpc.Metadata.*;

@RunWith(VertxUnitRunner.class)
public class ContextStorageTest {

  @Rule
  public RepeatRule repeatRule = new RepeatRule();

  private Vertx vertx;
  private volatile HttpServer httpServer;
  private volatile ManagedChannel channel;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext should) {
    if (channel != null) {
      channel.shutdown();
    }
    if (httpServer != null) {
      httpServer.close().onComplete(should.asyncAssertSuccess());
    }
    if (vertx != null) {
      vertx.close().onComplete(should.asyncAssertSuccess());
    }
  }

  @Test
  @Repeat(10)
  public void testGrpcContextPropagatedAcrossVertxAsyncCalls(TestContext should) {
    CallOptions.Key<String> traceOptionsKey = CallOptions.Key.create("trace");
    Key<String> traceMetadataKey = Key.of("trace", ASCII_STRING_MARSHALLER);
    Context.Key<String> traceContextKey = Context.key("trace");

    GreeterGrpc.GreeterImplBase impl = new GreeterGrpc.GreeterImplBase() {
      @Override
      public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        vertx.executeBlocking(() -> "Hello " + request.getName() + ", trace: " + traceContextKey.get()).onSuccess(greeting -> {
          responseObserver.onNext(HelloReply.newBuilder().setMessage(greeting).build());
          responseObserver.onCompleted();
        }).onFailure(should::fail);
      }
    };

    ServerServiceDefinition def = ServerInterceptors.intercept(impl, new ServerInterceptor() {
      @Override
      public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String traceId = headers.get(traceMetadataKey);
        should.assertNotNull(traceId);
        Context context = Context.current().withValue(traceContextKey, traceId);
        Context previous = context.attach();
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {
          @Override
          public void onComplete() {
            context.detach(previous);
            super.onComplete();
          }
        };
      }
    });

    GrpcServer server = GrpcServer.server(vertx);
    GrpcServiceBridge serverStub = GrpcServiceBridge.bridge(def);
    serverStub.bind(server);

    Async servertStart = should.async();
    vertx.createHttpServer()
      .requestHandler(server)
      .listen(0).onComplete(should.asyncAssertSuccess(httpServer -> {
        this.httpServer = httpServer;
        servertStart.complete();
      }));
    servertStart.await();

    channel = ManagedChannelBuilder.forAddress("localhost", httpServer.actualPort())
      .usePlaintext()
      .build();

    GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(ClientInterceptors.intercept(channel, new ClientInterceptor() {
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
          return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
              Metadata traceHeaders = new Metadata();
              traceHeaders.put(traceMetadataKey, callOptions.getOption(traceOptionsKey));
              headers.merge(traceHeaders);
              super.start(responseListener, headers);
            }
          };
        }
      }))
      .withCompression("identity");

    String trace = UUID.randomUUID().toString();
    HelloRequest request = HelloRequest.newBuilder().setName("Julien").build();
    HelloReply res = stub.withOption(traceOptionsKey, trace).sayHello(request);

    should.assertEquals("Hello Julien, trace: " + trace, res.getMessage());
  }
}
