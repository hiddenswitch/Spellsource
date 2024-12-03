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
package io.vertx.grpc.common.impl;

import io.vertx.grpc.common.GrpcWriteStream;
import io.vertx.grpc.common.GrpcMessageEncoder;

/**
 * An adapter between gRPC and Vert.x back-pressure.
 */
public class WriteStreamAdapter<T> {

  private GrpcWriteStream<T> stream;
  private boolean ready;
  private GrpcMessageEncoder<T> encoder;

  /**
   * Override this method to call gRPC {@code onReady}
   */
  protected void handleReady() {
  }

  public final void init(GrpcWriteStream<T> stream, GrpcMessageEncoder<T> encoder) {
    synchronized (this) {
      this.stream = stream;
      this.encoder = encoder;
    }
    stream.drainHandler(v -> {
      checkReady();
    });
    checkReady();
  }

  public final synchronized boolean isReady() {
    return ready;
  }

  public final void write(T msg) {
    stream.writeMessage(encoder.encode(msg));
    synchronized (this) {
      ready = !stream.writeQueueFull();
    }
  }

  private void checkReady() {
    synchronized (this) {
      if (ready || stream.writeQueueFull()) {
        return;
      }
      ready = true;
    }
    handleReady();
  }
}
