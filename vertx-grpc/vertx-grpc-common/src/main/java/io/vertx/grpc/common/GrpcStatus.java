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
