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

import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

/**
 *
 * @author <a href="mailto:leo.tu.taipei@gmail.com">Leo Tu</a>
 */
public class KeyValueCodec extends BaseCodec {
  private final Encoder valueEncoder;
  private final Decoder<Object> valueDecoder;

  private final Encoder mapKeyEncoder;
  private final Decoder<Object> mapKeyDecoder;

  private final Encoder mapValueEncoder;
  private final Decoder<Object> mapValueDecoder;

  /**
   * copy
   */
  public KeyValueCodec(ClassLoader classLoader, KeyValueCodec codec) {
    this.valueEncoder = codec.valueEncoder;
    this.valueDecoder = codec.valueDecoder;
    this.mapKeyEncoder = codec.mapKeyEncoder;
    this.mapKeyDecoder = codec.mapKeyDecoder;
    this.mapValueEncoder = codec.mapValueEncoder;
    this.mapValueDecoder = codec.mapValueDecoder;
  }

  public KeyValueCodec(Encoder valueEncoder, Decoder<Object> valueDecoder, Encoder mapKeyEncoder,
      Decoder<Object> mapKeyDecoder, Encoder mapValueEncoder, Decoder<Object> mapValueDecoder) {
    this.valueEncoder = valueEncoder;
    this.valueDecoder = valueDecoder;
    this.mapKeyEncoder = mapKeyEncoder;
    this.mapKeyDecoder = mapKeyDecoder;
    this.mapValueEncoder = mapValueEncoder;
    this.mapValueDecoder = mapValueDecoder;
  }

  @Override
  public Decoder<Object> getMapValueDecoder() {
    return mapValueDecoder;
  }

  @Override
  public Encoder getMapValueEncoder() {
    return mapValueEncoder;
  }

  @Override
  public Decoder<Object> getMapKeyDecoder() {
    return mapKeyDecoder;
  }

  @Override
  public Encoder getMapKeyEncoder() {
    return mapKeyEncoder;
  }

  @Override
  public Decoder<Object> getValueDecoder() {
    return valueDecoder;
  }

  @Override
  public Encoder getValueEncoder() {
    return valueEncoder;
  }

  @Override
  public String toString() {
    return super.toString() + "{valueEncoder=" + valueEncoder.getClass().getName() + ", valueDecoder="
        + valueDecoder.getClass().getName() + ", mapKeyEncoder=" + mapKeyEncoder.getClass().getName()
        + ", mapKeyDecoder=" + mapKeyDecoder.getClass().getName() + ", mapValueEncoder="
        + mapValueEncoder.getClass().getName() + ", mapValueDecoder=" + mapValueDecoder.getClass().getName() + "}";
  }

}
