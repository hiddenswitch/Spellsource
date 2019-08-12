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

import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.atomix.core.multimap.AsyncAtomicMultimap;
import io.atomix.utils.concurrent.Futures;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ChoosableIterable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Atomix async multi map.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class AtomixAsyncMultiMap<K, V> implements AsyncMultiMap<K, V> {
  private final Vertx vertx;
  private final AsyncAtomicMultimap<K, V> map;

  public AtomixAsyncMultiMap(Vertx vertx, AsyncAtomicMultimap<K, V> map) {
    this.vertx = checkNotNull(vertx, "vertx cannot be null");
    this.map = checkNotNull(map, "map cannot be null");
  }

  @Override
  public void add(K k, V v, Handler<AsyncResult<Void>> handler) {
    map.put(k, v).whenComplete(VertxFutures.voidHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void get(K k, Handler<AsyncResult<ChoosableIterable<V>>> handler) {
    map.get(k).whenComplete(VertxFutures.convertHandler(
        handler,
        collection -> collection != null
            ? new AtomixChoosableIterable(collection.value())
            : new AtomixChoosableIterable<V>(Collections.emptyList()),
        vertx.getOrCreateContext()));
  }

  @Override
  public void remove(K k, V v, Handler<AsyncResult<Boolean>> handler) {
    map.remove(k, v).whenComplete(VertxFutures.resultHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void removeAllForValue(V v, Handler<AsyncResult<Void>> handler) {
    Futures.allOf(map.keySet().stream().map(key -> map.remove(key, v)).collect(Collectors.toList()))
        .whenComplete(VertxFutures.voidHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void removeAllMatching(Predicate<V> p, Handler<AsyncResult<Void>> handler) {
    Futures.allOf(map.entries().stream()
        .filter(e -> p.test(e.getValue()))
        .map(e -> map.remove(e.getKey(), e.getValue()))
        .collect(Collectors.toList()))
        .whenComplete(VertxFutures.voidHandler(handler, vertx.getOrCreateContext()));
  }

  public AsyncAtomicMultimap<K, V> getMap() {
    return map;
  }
}
