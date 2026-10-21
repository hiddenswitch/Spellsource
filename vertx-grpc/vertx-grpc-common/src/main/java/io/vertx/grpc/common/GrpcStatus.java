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

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.codegen.annotations.VertxGen;

/**
 * gRPC statuses.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public enum GrpcStatus {

  OK(0),

  CANCELLED(1),

  UNKNOWN(2),

  INVALID_ARGUMENT(3),

  DEADLINE_EXCEEDED(4),

  NOT_FOUND(5),

  ALREADY_EXISTS(6),

  PERMISSION_DENIED(7),

  RESOURCE_EXHAUSTED(8),

  FAILED_PRECONDITION(9),

  ABORTED(10),

  OUT_OF_RANGE(11),

  UNIMPLEMENTED(12),

  INTERNAL(13),

  UNAVAILABLE(14),

  DATA_LOSS(15),

  UNAUTHENTICATED(16);

  private static final IntObjectMap<GrpcStatus> codeMap = new IntObjectHashMap<>();

  public static GrpcStatus valueOf(int code) {
    return codeMap.get(code);
  }

  /**
   * Map an HTTP status code to a gRPC status, see <a href="https://github.com/grpc/grpc/blob/master/doc/http-grpc-status-mapping.md">spec</a>.
   * @param sc the HTTP status code
   * @return the mapped status code
   */
  public static GrpcStatus fromHttpStatusCode(int sc) {
    if (sc < 0) {
      throw new IllegalArgumentException("Invalid status code: " + sc);
    }
    switch (sc) {
      case 400:
        return INTERNAL;
      case 401:
        return UNAUTHENTICATED;
      case 403:
        return PERMISSION_DENIED;
      case 404:
        return NOT_FOUND;
      case 429:
      case 502:
      case 503:
      case 504:
        return UNAVAILABLE;
      default:
        return UNKNOWN;
    }
  }

  static {
    for (GrpcStatus status : values()) {
      codeMap.put(status.code, status);
    }
  }

  public final int code;
  private final String string;

  GrpcStatus(int code) {
    this.code = code;
    this.string = Integer.toString(code);
  }


  @Override
  public String toString() {
    return string;
  }
}
