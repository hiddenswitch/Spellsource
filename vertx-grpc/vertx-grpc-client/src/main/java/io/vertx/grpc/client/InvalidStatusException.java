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

import io.vertx.core.VertxException;
import io.vertx.grpc.common.GrpcStatus;

/**
 * Denotes a failure due to an invalid status.
 */
public final class InvalidStatusException extends VertxException {

  private final GrpcStatus expected;
  private final GrpcStatus actual;

  public InvalidStatusException(GrpcStatus expected, GrpcStatus actual) {
    super("Invalid status: actual:" + actual.name() + ", expected:" + expected.name());
    this.expected = expected;
    this.actual = actual;
  }

  /**
   * @return the expected status
   */
  public GrpcStatus expectedStatus() {
    return expected;
  }

  /**
   * @return the actual status
   */
  public GrpcStatus actualStatus() {
    return actual;
  }
}
