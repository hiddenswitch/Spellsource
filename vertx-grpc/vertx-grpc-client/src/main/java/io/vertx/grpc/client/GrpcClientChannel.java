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

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.vertx.core.net.SocketAddress;

/**
 * Bridge a gRPC service with a {@link io.vertx.grpc.client.GrpcClient}.
 */
public class GrpcClientChannel extends io.grpc.Channel {

  private GrpcClient client;
  private SocketAddress server;

  public GrpcClientChannel(GrpcClient client, SocketAddress server) {
    this.client = client;
    this.server = server;
  }

  @Override
  public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(MethodDescriptor<RequestT, ResponseT> methodDescriptor, CallOptions callOptions) {
    return new VertxClientCall<>(client, server, methodDescriptor, callOptions, authority());
  }

  @Override
  public String authority() {
    return null;
  }

}
