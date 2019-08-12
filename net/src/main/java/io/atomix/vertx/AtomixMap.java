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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import io.atomix.core.map.AtomicMap;
import io.atomix.utils.time.Versioned;
import io.vertx.core.Vertx;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Atomix synchronous map.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class AtomixMap<K, V> implements Map<K, V> {
  private final AtomicMap<K, V> map;

  public AtomixMap(Vertx vertx, AtomicMap<K, V> map) {
    this.map = checkNotNull(map, "map cannot be null");
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean containsKey(Object key) {
    return map.containsKey((K) key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean containsValue(Object value) {
    return map.containsValue((V) value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public V get(Object key) {
    return Versioned.valueOrNull(map.get((K) key));
  }

  @Override
  public V put(K key, V value) {
    return Versioned.valueOrNull(map.put(key, value));
  }

  @Override
  @SuppressWarnings("unchecked")
  public V remove(Object key) {
    return Versioned.valueOrNull(map.remove((K) key));
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
      map.put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<V> values() {
    return map.values()
        .stream()
        .map(Versioned::valueOrNull)
        .collect(Collectors.toList());
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return map.entrySet()
        .stream()
        .map(entry -> Maps.immutableEntry(entry.getKey(), Versioned.valueOrNull(entry.getValue())))
        .collect(Collectors.toSet());
  }

}
