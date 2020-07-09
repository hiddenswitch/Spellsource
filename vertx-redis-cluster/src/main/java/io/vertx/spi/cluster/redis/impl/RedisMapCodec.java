/*
 * Copyright (c) 2019 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.spi.cluster.redis.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.shareddata.impl.ClusterSerializable;

/**
 * Support ClusterSerializable Object
 * 
 * @see org.redisson.codec.JsonJacksonCodec
 * @see org.redisson.codec.FstCodec
 * 
 * @author <a href="mailto:leo.tu.taipei@gmail.com">Leo Tu</a>
 */
class RedisMapCodec implements Codec {
  private static final Logger log = LoggerFactory.getLogger(RedisMapCodec.class);

  private final Encoder encoder = new Encoder() {
    @Override
    public ByteBuf encode(Object in) throws IOException {
      ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
      try {
        ByteBufOutputStream os = new ByteBufOutputStream(out);
        try (DataOutputStream dataOutput = new DataOutputStream(os)) {
          if (in instanceof ClusterSerializable) {
            ClusterSerializable clusterSerializable = (ClusterSerializable) in;
            dataOutput.writeBoolean(true);
            dataOutput.writeUTF(in.getClass().getName());
            Buffer buffer = Buffer.buffer();
            clusterSerializable.writeToBuffer(buffer);
            byte[] bytes = buffer.getBytes();
            dataOutput.writeInt(bytes.length);
            dataOutput.write(bytes);
          } else {
            dataOutput.writeBoolean(false);
            ByteArrayOutputStream javaByteOut = new ByteArrayOutputStream();
            ObjectOutput objectOutput = new ObjectOutputStream(javaByteOut);
            objectOutput.writeObject(in);
            dataOutput.write(javaByteOut.toByteArray());
          }
          return os.buffer();
        }
      } catch (IOException e) {
        log.warn("in.class: {}, error: {}", in == null ? "<null>" : in.getClass().getName(), e.toString());
        out.release();
        throw e;
      }
    }
  };

  private final Decoder<Object> decoder = new Decoder<Object>() {
    @Override
    public Object decode(ByteBuf buf, State state) throws IOException {
      ByteBufInputStream byteIn = new ByteBufInputStream(buf);
      try (DataInputStream in = new DataInputStream(byteIn)) {
        boolean isClusterSerializable = in.readBoolean();
        if (isClusterSerializable) {
          String className = in.readUTF();
          Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
          int length = in.readInt();
          byte[] body = new byte[length];
          in.readFully(body);
          try {
            ClusterSerializable clusterSerializable;
            // check clazz if have a public Constructor method.
            if (clazz.getConstructors().length == 0) {
              Constructor<?> constructor = (Constructor<?>) clazz.getDeclaredConstructor();
              constructor.setAccessible(true);
              clusterSerializable = (ClusterSerializable) constructor.newInstance();
            } else {
              clusterSerializable = (ClusterSerializable) clazz.newInstance();
            }
            clusterSerializable.readFromBuffer(0, Buffer.buffer(body));
            return clusterSerializable;
          } catch (Exception e) {
            throw new IllegalStateException("Failed to load class " + e.getMessage(), e);
          }
        } else {
          byte[] body = new byte[in.available()];
          in.readFully(body);
          ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(body));
          return objectIn.readObject();
        }
      } catch (Exception e) {
        log.warn("buf.class: {}, state: {}, error: {}", buf.getClass().getName(), state, e.toString());
        buf.release();
        throw new IOException(e.getMessage(), e);
      }
    }
  };

  @Override
  public Decoder<Object> getMapValueDecoder() {
    return decoder;
  }

  @Override
  public Encoder getMapValueEncoder() {
    return encoder;
  }

  @Override
  public Decoder<Object> getMapKeyDecoder() {
    return decoder;
  }

  @Override
  public Encoder getMapKeyEncoder() {
    return encoder;
  }

  @Override
  public Decoder<Object> getValueDecoder() {
    return decoder;
  }

  @Override
  public Encoder getValueEncoder() {
    return encoder;
  }

  @Override
  public ClassLoader getClassLoader() {
    return this.getClassLoader();
  }
}
