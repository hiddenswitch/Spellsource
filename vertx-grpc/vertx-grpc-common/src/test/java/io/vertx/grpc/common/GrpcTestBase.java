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

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class GrpcTestBase {

  /* The port on which the server should run */
  protected Vertx vertx;
  protected int port;

  @Before
  public void setUp() {
    port = 8080;
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext should) {
    vertx.close().onComplete(should.asyncAssertSuccess());
  }

  public static Buffer unzip(Buffer buffer) {
    Buffer ret = Buffer.buffer();
    try {
      GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(buffer.getBytes()));
      byte[] tmp = new byte[256];
      for (int l = 0;l != -1;l = in.read(tmp)) {
        ret.appendBytes(tmp, 0, l);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return ret;
  }

  public static Buffer zip(Buffer buffer) {
    ByteArrayOutputStream ret = new ByteArrayOutputStream();
    try {
      GZIPOutputStream in = new GZIPOutputStream(ret);
      in.write(buffer.getBytes());
      in.flush();
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Buffer.buffer(ret.toByteArray());
  }
}
