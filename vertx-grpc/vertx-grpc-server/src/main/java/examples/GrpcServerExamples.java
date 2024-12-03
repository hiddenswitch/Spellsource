package examples;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.docgen.Source;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.common.ServiceName;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerRequest;
import io.vertx.grpc.server.GrpcServerResponse;
import io.vertx.grpc.server.GrpcServiceBridge;

@Source
public class GrpcServerExamples {

  public void createServer(Vertx vertx, HttpServerOptions options) {

    GrpcServer grpcServer = GrpcServer.server(vertx);

    HttpServer server = vertx.createHttpServer(options);

    server
      .requestHandler(grpcServer)
      .listen();
  }

  public void requestResponse(GrpcServer server) {

    server.callHandler(GreeterGrpc.getSayHelloMethod(), request -> {

      request.handler(hello -> {

        GrpcServerResponse<HelloRequest, HelloReply> response = request.response();

        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + hello.getName()).build();

        response.end(reply);
      });
    });
  }

  public void streamingRequest(GrpcServer server) {

    server.callHandler(StreamingGrpc.getSinkMethod(), request -> {
      request.handler(item -> {
        // Process item
      });
      request.endHandler(v ->{
        // No more items
        // Send the response
        request.response().end(Empty.getDefaultInstance());
      });
      request.exceptionHandler(err -> {
        // Something wrong happened
      });
    });
  }

  public void streamingResponse(GrpcServer server) {

    server.callHandler(StreamingGrpc.getSourceMethod(), request -> {
      GrpcServerResponse<Empty, Item> response = request.response();
      request.handler(empty -> {
        for (int i = 0;i < 10;i++) {
          response.write(Item.newBuilder().setValue("1").build());
        }
        response.end();
      });
    });
  }

  public void bidi(GrpcServer server) {

    server.callHandler(StreamingGrpc.getPipeMethod(), request -> {

      request.handler(item -> request.response().write(item));
      request.endHandler(v -> request.response().end());
    });
  }

  public void requestFlowControl(Vertx vertx, GrpcServerRequest<Item, Empty> request, Item item) {
    // Pause the response
    request.pause();

    performAsyncOperation().onComplete(ar -> {
      // And then resume
      request.resume();
    });
  }

  private Future<Buffer> performAsyncOperation() {
    return Future.succeededFuture();
  }

  private Future<Buffer> performAsyncOperation(Object o) {
    return Future.succeededFuture();
  }

  private Future<GrpcMessage> handleGrpcMessage(GrpcMessage o) {
    return Future.succeededFuture();
  }

  public void responseFlowControl(GrpcServerResponse<Empty, Item> response, Item item) {
    if (response.writeQueueFull()) {
      response.drainHandler(v -> {
        // Writable again
      });
    } else {
      response.write(item);
    }
  }

  public void responseCompression(GrpcServerResponse<Empty, Item> response) {
    response.encoding("gzip");

    // Write items after encoding has been defined
    response.write(Item.newBuilder().setValue("item-1").build());
    response.write(Item.newBuilder().setValue("item-2").build());
    response.write(Item.newBuilder().setValue("item-3").build());
  }

  public void stubExample(Vertx vertx, HttpServerOptions options) {

    GrpcServer grpcServer = GrpcServer.server(vertx);

    GreeterGrpc.GreeterImplBase service = new GreeterGrpc.GreeterImplBase() {
      @Override
      public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello " + request.getName()).build());
        responseObserver.onCompleted();
      }
    };

    // Bind the service bridge in the gRPC server
    GrpcServiceBridge serverStub = GrpcServiceBridge.bridge(service);
    serverStub.bind(grpcServer);

    // Start the HTTP/2 server
    vertx.createHttpServer(options)
      .requestHandler(grpcServer)
      .listen();
  }

  public void protobufLevelAPI(GrpcServer server) {

    ServiceName greeterServiceName = ServiceName.create("helloworld", "Greeter");

    server.callHandler(request -> {

      if (request.serviceName().equals(greeterServiceName) && request.methodName().equals("SayHello")) {

        request.handler(protoHello -> {
          // Handle protobuf encoded hello
          performAsyncOperation(protoHello)
            .onSuccess(protoReply -> {
              // Reply with protobuf encoded reply
              request.response().end(protoReply);
            }).onFailure(err -> {
              request.response()
                .status(GrpcStatus.ABORTED)
                .end();
            });
        });
      } else {
        request.response()
          .status(GrpcStatus.NOT_FOUND)
          .end();
      }
    });
  }

  public void messageLevelAPI(GrpcServer server) {

    ServiceName greeterServiceName = ServiceName.create("helloworld", "Greeter");

    server.callHandler(request -> {

      if (request.serviceName().equals(greeterServiceName) && request.methodName().equals("SayHello")) {

        request.messageHandler(helloMessage -> {

          // Can be identity or gzip
          String helloEncoding = helloMessage.encoding();

          // Handle hello message
          handleGrpcMessage(helloMessage)
            .onSuccess(replyMessage -> {
              // Reply with reply message

              // Can be identity or gzip
              String replyEncoding = replyMessage.encoding();

              // Send the reply
              request.response().endMessage(replyMessage);
            }).onFailure(err -> {
              request.response()
                .status(GrpcStatus.ABORTED)
                .end();
            });
        });
      } else {
        request.response()
          .status(GrpcStatus.NOT_FOUND)
          .end();
      }
    });
  }
}
