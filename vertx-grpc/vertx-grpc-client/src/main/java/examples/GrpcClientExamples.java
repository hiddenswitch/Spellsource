package examples;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.net.SocketAddress;
import io.vertx.docgen.Source;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientChannel;
import io.vertx.grpc.client.GrpcClientRequest;
import io.vertx.grpc.client.GrpcClientResponse;
import io.vertx.grpc.common.GrpcException;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.common.ServiceName;

@Source
public class GrpcClientExamples {

  public void createClient(Vertx vertx) {
    GrpcClient client = GrpcClient.client(vertx);
  }

  public void sendRequest(GrpcClient client) {

    SocketAddress server = SocketAddress.inetSocketAddress(443, "example.com");
    MethodDescriptor<HelloRequest, HelloReply> sayHelloMethod = GreeterGrpc.getSayHelloMethod();
    Future<GrpcClientRequest<HelloRequest, HelloReply>> fut = client.request(server, sayHelloMethod);
    fut.onSuccess(request -> {
      // The end method calls the service
      request.end(HelloRequest.newBuilder().setName("Bob").build());
    });
  }

  public void receiveResponse(GrpcClientRequest<HelloRequest, HelloReply> request) {
    request.response().onSuccess(response -> {
      Future<HelloReply> fut = response.last();
      fut.onSuccess(reply -> {
        System.out.println("Received " + reply.getMessage());
      });
    });
  }

  public void requestResponse(GrpcClient client, SocketAddress server) {
    client
      .request(server, GreeterGrpc.getSayHelloMethod()).compose(request -> {
        request.end(HelloRequest
          .newBuilder()
          .setName("Bob")
          .build());
        return request.response().compose(response -> response.last());
      }).onSuccess(reply -> {
        System.out.println("Received " + reply.getMessage());
      });
  }

  public void streamingRequest(GrpcClient client, SocketAddress server) {
    client
      .request(server, StreamingGrpc.getSinkMethod())
      .onSuccess(request -> {
      for (int i = 0;i < 10;i++) {
        request.write(Item.newBuilder().setValue("1").build());
      }
      request.end();
    });
  }

  public void streamingResponse(GrpcClient client, SocketAddress server) {
    client
      .request(server, StreamingGrpc.getSourceMethod())
      .compose(request -> {
        request.end(Empty.getDefaultInstance());
        return request.response();
      })
      .onSuccess(response -> {
        response.handler(item -> {
          // Process item
        });
        response.endHandler(v -> {
          // Done
        });
        response.exceptionHandler(err -> {
          // Something went bad
        });
      });
  }

  public void requestFlowControl(GrpcClientRequest<Item, Empty> request, Item item) {
    if (request.writeQueueFull()) {
      request.drainHandler(v -> {
        // Writable again
      });
    } else {
      request.write(item);
    }
  }

  public void responseFlowControl(Vertx vertx, GrpcClientResponse<Empty, Item> response, Item item) {
    // Pause the response
    response.pause();

    performAsyncOperation().onComplete(ar -> {
      // And then resume
      response.resume();
    });
  }

  private Future<Void> performAsyncOperation() {
    return null;
  }

  public void requestCancellation(GrpcClientRequest<Item, Empty> request) {
    request.cancel();
  }

  public void requestCompression(GrpcClientRequest<Item, Empty> request) {
    request.encoding("gzip");

    // Write items after encoding has been defined
    request.write(Item.newBuilder().setValue("item-1").build());
    request.write(Item.newBuilder().setValue("item-2").build());
    request.write(Item.newBuilder().setValue("item-3").build());
  }

  public void stubExample(GrpcClient client) {

    GrpcClientChannel channel = new GrpcClientChannel(client, SocketAddress.inetSocketAddress(443, "example.com"));

    GreeterGrpc.GreeterStub greeter = GreeterGrpc.newStub(channel);

    greeter.sayHello(HelloRequest.newBuilder().setName("Bob").build(), new StreamObserver<HelloReply>() {
      @Override
      public void onNext(HelloReply value) {
        // Process response
      }
      @Override
      public void onCompleted() {
        // Done
      }
      @Override
      public void onError(Throwable t) {
        // Something went bad
      }
    });
  }

  public void protobufLevelAPI(GrpcClient client, Buffer protoHello, SocketAddress server) {

    Future<GrpcClientRequest<Buffer, Buffer>> requestFut = client.request(server);

    requestFut.onSuccess(request -> {

      // Set the service name and the method to call
      request.serviceName(ServiceName.create("helloworld", "Greeter"));
      request.methodName("SayHello");

      // Send the protobuf request
      request.end(protoHello);

      // Handle the response
      Future<GrpcClientResponse<Buffer, Buffer>> responseFut = request.response();
      responseFut.onSuccess(response -> {
        response.handler(protoReply -> {
          // Handle the protobuf reply
        });
      });
    });
  }

  public void messageLevelAPI(GrpcClient client, Buffer protoHello, SocketAddress server) {

    Future<GrpcClientRequest<Buffer, Buffer>> requestFut = client.request(server);

    requestFut.onSuccess(request -> {

      // Set the service name and the method to call
      request.serviceName(ServiceName.create("helloworld", "Greeter"));
      request.methodName("SayHello");

      // Send the protobuf request
      request.endMessage(GrpcMessage.message("identity", protoHello));

      // Handle the response
      Future<GrpcClientResponse<Buffer, Buffer>> responseFut = request.response();
      responseFut.onSuccess(response -> {
        response.messageHandler(replyMessage -> {
          System.out.println("Got reply message encoded as " + replyMessage.encoding());
        });
      });
    });
  }

  public void errorHandling(GrpcClientRequest<HelloRequest, HelloReply> request) {
    request.response().onSuccess(response -> {
      Future<HelloReply> fut = response.last();
      fut.onFailure(error -> {
        if (error instanceof GrpcException) {
          GrpcException grpcError = (GrpcException) error;
          GrpcStatus status = grpcError.status();
          HttpClientResponse httpResponse = grpcError.response();
        }
      });
    });
  }

}
