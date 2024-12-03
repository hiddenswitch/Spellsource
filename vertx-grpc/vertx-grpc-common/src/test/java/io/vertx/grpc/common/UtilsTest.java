/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.common;

import io.grpc.Metadata;
import io.vertx.core.MultiMap;
import io.vertx.grpc.common.impl.Utils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

  /**
   * Reproduce <a href="https://github.com/eclipse-vertx/vertx-grpc/issues/35">#35</a>
   */
  @Test
  public void testReadMetadata() {
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();

    headers.add("key0", "value0");
    headers.add("key1", "value1");
    headers.add("key0", "value2");

    Metadata metadata = Utils.readMetadata(headers);
    assertEquals(metadata.keys().size(), 2);

    List<String> l = StreamSupport.stream(metadata.getAll(Metadata.Key.of("key0", Metadata.ASCII_STRING_MARSHALLER)).spliterator(), false)
      .collect(Collectors.toList());
    assertEquals(l.size(), 2);
    assertTrue(l.contains("value0"));
    assertTrue(l.contains("value2"));

    l = StreamSupport.stream(metadata.getAll(Metadata.Key.of("key1", Metadata.ASCII_STRING_MARSHALLER)).spliterator(), false)
      .collect(Collectors.toList());
    assertEquals(l.size(), 1);
    assertTrue(l.contains("value1"));
  }
}
