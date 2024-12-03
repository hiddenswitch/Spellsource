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

/**
 * gRPC error, a subset of {@link GrpcStatus} elements.
 */
@VertxGen
public enum GrpcError {

  INTERNAL(GrpcStatus.INTERNAL, 0x02),

  UNAVAILABLE(GrpcStatus.UNAVAILABLE, 0x07),

  CANCELLED(GrpcStatus.CANCELLED, 0x08),

  RESOURCE_EXHAUSTED(GrpcStatus.RESOURCE_EXHAUSTED, 0x0B),

  PERMISSION_DENIED(GrpcStatus.PERMISSION_DENIED, 0x0C);

  public final GrpcStatus status;
  public final long http2ResetCode;

  GrpcError(GrpcStatus status, long http2ResetCode) {
    this.status = status;
    this.http2ResetCode = http2ResetCode;
  }

  /**
   * Map the HTTP/2 code to the gRPC error.
   * @param code the HTTP/2 code
   * @return the gRPC error or {@code null} when none applies
   */
  public static GrpcError mapHttp2ErrorCode(long code) {
    switch ((int)code) {
      case 0x00:
        // NO_ERROR
      case 0x01:
        // PROTOCOL_ERROR
      case 0x02:
        // INTERNAL_ERROR
      case 0x03:
        // FLOW_CONTROL_ERROR
      case 0x04:
        // FRAME_SIZE_ERROR
      case 0x06:
        // FRAME_SIZE_ERROR
      case 0x09:
        // COMPRESSION_ERROR
        return GrpcError.INTERNAL;
      case 0x07:
        // REFUSED_STREAM
        return GrpcError.UNAVAILABLE;
      case 0x0A:
        // CONNECT_ERROR
      case 0x08:
        // CANCEL
        return GrpcError.CANCELLED;
      case 0x0B:
        // ENHANCE_YOUR_CALM
        return GrpcError.RESOURCE_EXHAUSTED;
      case 0x0C:
        // INADEQUATE_SECURITY
        return GrpcError.PERMISSION_DENIED;
      default:
        // STREAM_CLOSED;
        // HTTP_1_1_REQUIRED
        return null;
    }
  }
}
