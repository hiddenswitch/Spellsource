/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package io.atomix.vertx;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.impl.ClusterSerializable;

/**
 * Cluster serializable serializer.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ClusterSerializableSerializer<T extends ClusterSerializable> extends Serializer<T> {
  @Override
  public void write(Kryo kryo, Output output, T object) {
    Buffer buffer = Buffer.buffer();
    object.writeToBuffer(buffer);
    byte[] bytes = buffer.getBytes();
    output.writeVarInt(bytes.length, true);
    output.writeBytes(bytes);
  }

  @Override
  public T read(Kryo kryo, Input input, Class<T> type) {
    try {
      byte[] bytes = input.readBytes(input.readVarInt(true));
      Buffer buffer = Buffer.buffer(bytes);
      T object = type.newInstance();
      object.readFromBuffer(0, buffer);
      return object;
    } catch (InstantiationException | IllegalAccessException e) {
      throw new VertxException("failed to instantiate serializable type: " + type);
    }
  }
}
