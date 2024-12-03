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

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.grpc.common.impl.ServiceNameImpl;

/**
 * A gRPC service name.
 */
@VertxGen
public interface ServiceName {

  /**
   * Create a service name from its fully qualified name, e.g {@code com.examples.MyService}
   *
   * @param fqn the fully qualified service name
   * @return the service name
   */
  static ServiceName create(String fqn) {
    return new ServiceNameImpl(fqn);
  }

  /**
   * Create a service name from its package name and name
   *
   * @param packageName the package name
   * @param name the name
   * @return the service name
   */
  static ServiceName create(String packageName, String name) {
    return new ServiceNameImpl(packageName, name);
  }

  /**
   * @return the name
   */
  @CacheReturn
  String name();

  /**
   * @return the package name
   */
  @CacheReturn
  String packageName();

  /**
   * @return the fully qualified name
   */
  @CacheReturn
  String fullyQualifiedName();

  /**
   * Create the path of a given {@code method} to call.
   * @param method the method
   * @return the path, e.g {@code /com.examples.MyService/MyMethod}
   */
  String pathOf(String method);

}
