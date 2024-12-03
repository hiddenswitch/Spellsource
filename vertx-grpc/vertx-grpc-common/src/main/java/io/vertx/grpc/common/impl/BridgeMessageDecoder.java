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

import io.grpc.Decompressor;
import io.grpc.MethodDescriptor;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.common.GrpcMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BridgeMessageDecoder<T> implements GrpcMessageDecoder<T> {

  private MethodDescriptor.Marshaller<T> marshaller;
  private Decompressor decompressor;

  public BridgeMessageDecoder(MethodDescriptor.Marshaller<T> marshaller, Decompressor decompressor) {
    this.marshaller = marshaller;
    this.decompressor = decompressor;
  }

  @Override
  public T decode(GrpcMessage msg) {
    if (msg.encoding().equals("identity")) {
      return marshaller.parse(new ByteArrayInputStream(msg.payload().getBytes()));
    } else {
      try {
        return marshaller.parse(decompressor.decompress(new ByteArrayInputStream(msg.payload().getBytes())));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
