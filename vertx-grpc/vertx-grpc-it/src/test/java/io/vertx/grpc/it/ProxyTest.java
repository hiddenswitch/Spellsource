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
package io.vertx.grpc.it;

import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerResponse;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ProxyTest extends ProxyTestBase {

  @Test
  public void testUnary(TestContext should) {

    GrpcClient client = GrpcClient.client(vertx);

    Future<HttpServer> server = vertx.createHttpServer().requestHandler(GrpcServer.server(vertx).callHandler(GreeterGrpc.getSayHelloMethod(), call -> {
      call.handler(helloRequest -> {
        HelloReply helloReply = HelloReply.newBuilder().setMessage("Hello " + helloRequest.getName()).build();
        call.response().end(helloReply);
      });
    })).listen(8080, "localhost");

    Future<HttpServer> proxy = vertx.createHttpServer().requestHandler(GrpcServer.server(vertx).callHandler(clientReq -> {
      clientReq.pause();
      client.request(SocketAddress.inetSocketAddress(8080, "localhost")).onComplete(should.asyncAssertSuccess(proxyReq -> {
        proxyReq.response().onSuccess(resp -> {
          GrpcServerResponse<Buffer, Buffer> bc = clientReq.response();
          resp.messageHandler(bc::writeMessage);
          resp.endHandler(v -> bc.end());
        });
        proxyReq.fullMethodName(clientReq.fullMethodName());
        clientReq.messageHandler(proxyReq::writeMessage);
        clientReq.endHandler(v -> proxyReq.end());
        clientReq.resume();
      }));
    })).listen(8081, "localhost");

    Async test = should.async();
    server.flatMap(v -> proxy).onComplete(should.asyncAssertSuccess(v -> {
      client.request(SocketAddress.inetSocketAddress(8081, "localhost"), GreeterGrpc.getSayHelloMethod())
        .onComplete(should.asyncAssertSuccess(callRequest -> {
          callRequest.response().onComplete(should.asyncAssertSuccess(callResponse -> {
            AtomicInteger count = new AtomicInteger();
            callResponse.handler(reply -> {
              should.assertEquals(1, count.incrementAndGet());
              should.assertEquals("Hello Julien", reply.getMessage());
            });
            callResponse.endHandler(v2 -> {
              should.assertEquals(1, count.get());
              test.complete();
            });
          }));
          callRequest.end(HelloRequest.newBuilder().setName("Julien").build());
        }));
    }));
  }
}
