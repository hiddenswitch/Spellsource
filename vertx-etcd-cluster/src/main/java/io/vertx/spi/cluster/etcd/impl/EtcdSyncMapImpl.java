package io.vertx.spi.cluster.etcd.impl;

import static io.vertx.spi.cluster.etcd.impl.Codec.fromByteString;
import static io.vertx.spi.cluster.etcd.impl.Codec.toByteString;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import io.etcd.jetcd.api.DeleteRangeRequest;
import io.etcd.jetcd.api.KVGrpc;
import io.etcd.jetcd.api.PutRequest;
import io.etcd.jetcd.api.RangeRequest;
import com.google.common.collect.Maps;

import io.grpc.ManagedChannel;

/**
 * @author <a href="mailto:guoyu.511@gmail.com">Guo Yu</a>
 */
public class EtcdSyncMapImpl<K, V> implements Map<K, V> {

  private KeyPath keyPath;

  private long sharedLease;

  private KVGrpc.KVBlockingStub kvStub;

  public EtcdSyncMapImpl(KeyPath keyPath, long sharedLease, ManagedChannel channel) {
    this.keyPath = keyPath;
    this.sharedLease = sharedLease;
    kvStub = KVGrpc.newBlockingStub(channel);
  }

  @Override
  public int size() {
    return (int) kvStub.range(
      RangeRequest.newBuilder()
        .setKey(keyPath.rangeBegin())
        .setRangeEnd(keyPath.rangeEnd())
        .build())
      .getCount();
  }

  @Override
  public boolean isEmpty() {
    return kvStub.range(
      RangeRequest.newBuilder()
        .setKey(keyPath.rangeBegin())
        .setRangeEnd(keyPath.rangeEnd())
        .build())
      .getCount() == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return kvStub.range(
      RangeRequest.newBuilder()
        .setKey(keyPath.getKey(key.toString())) //TODO make it type safe
        .build())
      .getCount() == 0;
  }

  @Override
  public boolean containsValue(Object value) {
    return kvStub.range(
      RangeRequest.newBuilder()
        .setKey(keyPath.rangeBegin())
        .setRangeEnd(keyPath.rangeEnd())
        .build())
      .getKvsList()
      .stream()
      .anyMatch((kv) ->
        fromByteString(kv.getValue()).equals(value)
      );
  }

  @Override
  public V get(Object key) {
    return kvStub.range(
      RangeRequest.newBuilder()
        .setKey(keyPath.getKey(key))
        .build())
      .getKvsList()
      .stream()
      .findFirst()
      .<V>map(kv -> fromByteString(kv.getValue()))
      .orElse(null);
  }

  @Override
  public V put(K key, V value) {
    return fromByteString(kvStub.put(
      PutRequest.newBuilder()
        .setKey(keyPath.getKey(key))
        .setValue(toByteString(value))
        .setLease(sharedLease)
        .setPrevKv(true)
        .build())
      .getPrevKv().getValue());
  }

  @Override
  public V remove(Object key) {
    return kvStub.deleteRange(
      DeleteRangeRequest.newBuilder()
        .setKey(keyPath.getKey(key))
        .setPrevKv(true)
        .build())
      .getPrevKvsList()
      .stream()
      .findFirst()
      .<V>map(kv -> fromByteString(kv.getValue()))
      .orElse(null);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    map.forEach(this::put);
  }

  @Override
  public void clear() {
    kvStub.deleteRange(
      DeleteRangeRequest.newBuilder()
        .setKey(keyPath.rangeBegin())
        .setRangeEnd(keyPath.rangeEnd())
        .build());
  }

  @Override
  public Set<K> keySet() {
    return entrySet()
      .stream()
      .map(Map.Entry::getKey)
      .collect(toSet());
  }

  @Override
  public Collection<V> values() {
    return entrySet()
      .stream()
      .map(Map.Entry::getValue)
      .collect(toSet());
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return kvStub.range(
      RangeRequest.newBuilder()
        .setKey(keyPath.rangeBegin())
        .setRangeEnd(keyPath.rangeEnd())
        .build())
      .getKvsList()
      .stream()
      .<Entry<K, V>>map(kv -> Maps.immutableEntry(
        keyPath.getRawKey(kv.getKey()),
        fromByteString(kv.getValue())
      ))
      .collect(toSet());
  }

}
