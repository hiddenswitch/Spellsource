package io.vertx.spi.cluster.etcd.impl;

import static io.vertx.spi.cluster.etcd.impl.Codec.fromByteArray;
import static io.vertx.spi.cluster.etcd.impl.Codec.toByteArray;

import java.util.Arrays;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.protobuf.ByteString;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class KeyPath {

  private byte[] prefix;

  private static final byte[] BEGIN = new byte[]{ 0x0 };

  private static final byte[] END = new byte[]{ 0x1 };

  private KeyPath(String path) {
    byte[] bytes = path.getBytes(Charsets.UTF_8);
    this.prefix = Bytes.concat(
      Ints.toByteArray(bytes.length), bytes);
  }

  // for sync & async map

  public ByteString getKey(Object key) {
    return ByteString.copyFrom(
      Bytes.concat(
        prefix,
        BEGIN,
        toByteArray(key)
      )
    );
  }

  // for multi map
  public ByteString getKey(Object key, Object value) {
    return ByteString.copyFrom(
      Bytes.concat(
        prefix,
        BEGIN,
        toByteArray(key),
        BEGIN,
        toByteArray(value)
      )
    );
  }

  <K> K getRawKey(ByteString key) {
    byte[] bytes = key.toByteArray();
    return fromByteArray(
      Arrays.copyOfRange(bytes, prefix.length + 1, bytes.length)
    );
  }

  public ByteString rangeBegin() {
    return ByteString.copyFrom(
      Bytes.concat(prefix, BEGIN)
    );
  }

  public ByteString rangeEnd() {
    return ByteString.copyFrom(
      Bytes.concat(prefix, END)
    );
  }

  public ByteString rangeBegin(Object key) {
    return ByteString.copyFrom(
      Bytes.concat(prefix,
        BEGIN,
        toByteArray(key),
        BEGIN
      )
    );
  }

  public ByteString rangeEnd(Object key) {
    return ByteString.copyFrom(
      Bytes.concat(prefix,
        BEGIN,
        toByteArray(key),
        END
      )
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    KeyPath keyPath = (KeyPath) o;
    return Arrays.equals(prefix, keyPath.prefix);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(prefix);
  }

  public static KeyPath path(String path) {
    return new KeyPath(path);
  }

}
