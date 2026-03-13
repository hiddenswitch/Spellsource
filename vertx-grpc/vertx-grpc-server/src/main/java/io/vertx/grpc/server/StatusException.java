/*
 * Copyright (c) 2011-2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.server;

import io.vertx.core.VertxException;
import io.vertx.grpc.common.GrpcStatus;

/**
 * A glorified GOTO forcing a response status.
 */
public final class StatusException extends VertxException {

  private final GrpcStatus status;
  private final String message;

  public StatusException(GrpcStatus status) {
    super("Grpc status " + status.name());
    this.status = status;
    this.message = null;
  }

  public StatusException(GrpcStatus status, String message) {
    super("Grpc status " + status.name());
    this.status = status;
    this.message = message;
  }

  /**
   * @return the status
   */
  public GrpcStatus status() {
    return status;
  }

  /**
   * @return the status message
   */
  public String message() {
    return message;
  }
}
