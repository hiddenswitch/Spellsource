/*
 * Copyright (c) 2011-2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.common;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.spi.context.storage.ContextLocal;
import io.vertx.grpc.common.impl.GrpcRequestLocalRegistration;

import java.time.Instant;

/**
 * gRPC request local propagation.
 */
public class GrpcLocal {

  /**
   * Context local key.
   */
  public static final ContextLocal<GrpcLocal> CONTEXT_LOCAL_KEY = GrpcRequestLocalRegistration.CONTEXT_LOCAL;

  private final long deadlineMillis;
  private Instant deadline;

  public GrpcLocal(long deadlineMillis) {
    this.deadlineMillis = deadlineMillis;
  }

  /**
   * @return the local associated with the given {@code context}
   */
  public static GrpcLocal of(Context context) {
    return ((ContextInternal)context).getLocal(CONTEXT_LOCAL_KEY);
  }

  /**
   * @return the current request local or {@code null}
   */
  public static GrpcLocal current() {
    Context ctx = Vertx.currentContext();
    return ctx != null ? of(ctx) : null;
  }

  /**
   * @return the deadline
   */
  public Instant deadline() {
    if (deadline == null) {
      deadline = Instant.ofEpochMilli(deadlineMillis);
    }
    return deadline;
  }
}
