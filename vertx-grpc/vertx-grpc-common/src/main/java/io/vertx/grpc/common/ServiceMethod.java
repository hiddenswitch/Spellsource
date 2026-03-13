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

import io.vertx.codegen.annotations.GenIgnore;

/**
 * Bundle all the bits required to call or bind a grpc service method.
 */
@GenIgnore(GenIgnore.PERMITTED_TYPE)
public interface ServiceMethod<I, O> {

  static <Req, Resp> ServiceMethod<Resp, Req> client(ServiceName serviceName, String methodName, GrpcMessageEncoder<Req> encoder, GrpcMessageDecoder<Resp> decoder) {
    return new ServiceMethod<>() {
      @Override
      public ServiceName serviceName() {
        return serviceName;
      }
      @Override
      public String methodName() {
        return methodName;
      }
      @Override
      public GrpcMessageDecoder<Resp> decoder() {
        return decoder;
      }
      @Override
      public GrpcMessageEncoder<Req> encoder() {
        return encoder;
      }
    };
  }

  static <Req, Resp> ServiceMethod<Req, Resp> server(ServiceName serviceName, String methodName, GrpcMessageEncoder<Resp> encoder, GrpcMessageDecoder<Req> decoder) {
    return new ServiceMethod<>() {
      @Override
      public ServiceName serviceName() {
        return serviceName;
      }
      @Override
      public String methodName() {
        return methodName;
      }
      @Override
      public GrpcMessageDecoder<Req> decoder() {
        return decoder;
      }
      @Override
      public GrpcMessageEncoder<Resp> encoder() {
        return encoder;
      }
    };
  }

  /**
   * @return the service name.
   */
  ServiceName serviceName();

  /**
   * @return the method name
   */
  String methodName();

  default String fullMethodName() {
    return serviceName().fullyQualifiedName() + "/" + methodName();
  }

  /**
   * @return the message decoder
   */
  GrpcMessageDecoder<I> decoder();

  /**
   * @return the message encoder
   */
  GrpcMessageEncoder<O> encoder();

}
