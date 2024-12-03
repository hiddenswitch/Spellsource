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
package io.vertx.grpc.common;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.grpc.common.impl.GrpcMessageImpl;

/**
 * A generic gRPC message
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface GrpcMessage {

  /**
   * @return a new message
   */
  static GrpcMessage message(String encoding, Buffer payload) {
    return new GrpcMessageImpl(encoding, payload);
  }

  /**
   * @return the message encoding
   */
  String encoding();

  /**
   * @return the message payload, usually in Protobuf format encoded in the {@link #encoding()} format
   */
  Buffer payload();

}
