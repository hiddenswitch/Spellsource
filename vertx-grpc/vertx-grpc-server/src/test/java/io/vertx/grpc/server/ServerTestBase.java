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

import io.grpc.ManagedChannel;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.grpc.common.GrpcTestBase;
import junit.framework.AssertionFailedError;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class ServerTestBase extends GrpcTestBase {

  protected volatile ManagedChannel channel;

  @Override
  public void tearDown(TestContext should) {
    if (channel != null) {
      channel.shutdown();
    }
    super.tearDown(should);
  }

  protected void startServer(GrpcServer server) {
    startServer(new HttpServerOptions().setPort(8080).setHost("localhost"), server);
  }

  protected void startServer(HttpServerOptions options, GrpcServer server) {
    CompletableFuture<Void> res = new CompletableFuture<>();
    vertx.createHttpServer(options).requestHandler(server).listen()
      .onComplete(ar -> {
        if (ar.succeeded()) {
          res.complete(null);
        } else {
          res.completeExceptionally(ar.cause());
        }
      });
    try {
      res.get(20, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    } catch (ExecutionException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e.getCause());
      throw afe;
    } catch (TimeoutException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }
}
