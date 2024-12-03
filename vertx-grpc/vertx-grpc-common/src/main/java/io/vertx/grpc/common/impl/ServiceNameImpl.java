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

import java.util.Objects;

public class ServiceNameImpl implements ServiceName {

  private String name;
  private String packageName;
  private String fullyQualifiedName;

  public ServiceNameImpl(String packageName, String name) {
    this.name = name;
    this.packageName = packageName;
  }

  public ServiceNameImpl(String fullyQualifiedName) {
    this.fullyQualifiedName = fullyQualifiedName;
  }

  @Override
  public String name() {
    if (name == null) {
      int idx = fullyQualifiedName.lastIndexOf('.');
      name = fullyQualifiedName.substring(idx + 1);
    }
    return name;
  }

  @Override
  public String packageName() {
    if (packageName == null) {
      if (fullyQualifiedName == null) {
        return "";
      }
      int idx = fullyQualifiedName.lastIndexOf('.');
      if (idx < 0) {
        packageName = "";
      } else {
        packageName = fullyQualifiedName.substring(0, idx);
      }
    }
    return packageName;
  }

  @Override
  public String fullyQualifiedName() {
    if (fullyQualifiedName == null) {
      if (packageName == null || packageName.isEmpty()) {
        fullyQualifiedName = name;
      } else {
        fullyQualifiedName = packageName + '.' + name;
      }
    }
    return fullyQualifiedName;
  }

  @Override
  public String pathOf(String method) {
    if (fullyQualifiedName != null) {
      return '/' + fullyQualifiedName + '/' + method;
    } else {
      if (packageName == null || packageName.isEmpty()) {
        return '/' + name + '/' + method;
      } else {
        return '/' + packageName + '.' + name + '/' + method;
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceNameImpl that = (ServiceNameImpl) o;
    return Objects.equals(fullyQualifiedName(), that.fullyQualifiedName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(fullyQualifiedName());
  }
}
