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

import io.vertx.core.VertxException;
import io.vertx.core.http.StreamResetException;

/**
 * Thrown when a failure happens before the response, and it could be interpreted to a gRPC failure, e.g.
 * in practice it means an HTTP/2 stream reset mapped to a gRPC code according to the
 * <a href="https://github.com/grpc/grpc/blob/master/doc/PROTOCOL-HTTP2.md#http2-transport-mapping">spec</a>.
 */
public final class GrpcErrorException extends VertxException {

  public static GrpcErrorException create(StreamResetException sre) {
    GrpcError error = GrpcError.mapHttp2ErrorCode(sre.getCode());
    GrpcStatus status = GrpcStatus.UNKNOWN;
    if (error != null) {
      status = error.status;
    }
    return new GrpcErrorException(error, status);
  }

  private final GrpcError error;
  private final GrpcStatus status;

  public GrpcErrorException(GrpcError error, GrpcStatus status) {
    super("gRPC error status: " + status.name());

    this.error = error;
    this.status = status;
  }

  public GrpcError error() {
    return error;
  }

  public GrpcStatus status() {
    return status;
  }
}
