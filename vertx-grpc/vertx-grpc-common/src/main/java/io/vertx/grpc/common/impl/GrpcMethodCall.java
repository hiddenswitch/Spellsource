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

import io.vertx.grpc.common.ServiceName;

public class GrpcMethodCall {

  private String path;
  private String fullMethodName;
  private ServiceName serviceName;
  private String methodName;

  public GrpcMethodCall(String path) {
    this.path = path;
  }

  public String fullMethodName() {
    if (fullMethodName == null) {
      fullMethodName = path.substring(1);
    }
    return fullMethodName;
  }

  public ServiceName serviceName() {
    if (serviceName == null) {
      int idx1 = path.lastIndexOf('.');
      int idx2 = path.lastIndexOf('/');
      if (idx1 < 1) {
        serviceName = ServiceName.create("", path.substring(1, idx2));
      } else {
        serviceName = ServiceName.create(path.substring(1, idx1), path.substring(idx1 + 1, idx2));
      }
    }
    return serviceName;
  }

  public String methodName() {
    if (methodName == null) {
      int idx = path.lastIndexOf('/');
      methodName = path.substring(idx + 1);
    }
    return methodName;
  }
}
