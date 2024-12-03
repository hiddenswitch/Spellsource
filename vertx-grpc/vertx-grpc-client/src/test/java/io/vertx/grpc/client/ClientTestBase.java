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

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.grpc.common.GrpcTestBase;
import org.junit.After;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class ClientTestBase extends GrpcTestBase {

  /* The port on which the server should run */
  protected Server server;

  @After
  public void tearDown(TestContext should) {
    stopServer(false);
    super.tearDown(should);
  }

  void startServer(BindableService service) throws IOException {
    startServer(service, ServerBuilder.forPort(port));
  }

  void stopServer(boolean now) {
    Server s = server;
    if (s != null) {
      server = null;
      s.shutdownNow();
    }
  }

  void startServer(BindableService service, ServerBuilder builder) throws IOException {
    server = builder
        .addService(service)
        .build()
        .start();
  }


  void startServer(ServerServiceDefinition service) throws IOException {
    startServer(service, ServerBuilder.forPort(port));
  }

  void startServer(ServerServiceDefinition service, ServerBuilder builder) throws IOException {
    server = builder
      .addService(service)
      .build()
      .start();
  }
}
