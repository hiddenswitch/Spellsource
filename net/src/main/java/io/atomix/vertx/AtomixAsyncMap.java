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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.atomix.core.map.AsyncAtomicMap;
import io.atomix.utils.time.Versioned;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Atomix async map.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class AtomixAsyncMap<K, V> implements AsyncMap<K, V> {
  private final Vertx vertx;
  private final AsyncAtomicMap<K, V> map;

  AtomixAsyncMap(Vertx vertx, AsyncAtomicMap<K, V> map) {
    this.vertx = checkNotNull(vertx, "vertx cannot be null");
    this.map = checkNotNull(map, "map cannot be null");
  }

  @Override
  public void get(K k, Handler<AsyncResult<V>> handler) {
    map.get(k).whenComplete(VertxFutures.versionedResultHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void put(K k, V v, Handler<AsyncResult<Void>> handler) {
    map.put(k, v).whenComplete(VertxFutures.voidHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void put(K k, V v, long l, Handler<AsyncResult<Void>> handler) {
    map.put(k, v, Duration.ofMillis(l)).whenComplete(VertxFutures.voidHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> handler) {
    map.putIfAbsent(k, v).whenComplete(VertxFutures.versionedResultHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void putIfAbsent(K k, V v, long l, Handler<AsyncResult<V>> handler) {
    map.putIfAbsent(k, v, Duration.ofMillis(l)).whenComplete(VertxFutures.versionedResultHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void remove(K k, Handler<AsyncResult<V>> handler) {
    map.remove(k).whenComplete(VertxFutures.versionedResultHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void removeIfPresent(K k, V v, Handler<AsyncResult<Boolean>> handler) {
    map.remove(k, v).whenComplete(VertxFutures.resultHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void replace(K k, V v, Handler<AsyncResult<V>> handler) {
    map.replace(k, v).whenComplete(VertxFutures.versionedResultHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void replaceIfPresent(K k, V oldValue, V newValue, Handler<AsyncResult<Boolean>> handler) {
    map.replace(k, oldValue, newValue).whenComplete(VertxFutures.resultHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> handler) {
    map.clear().whenComplete(VertxFutures.voidHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> handler) {
    map.size().whenComplete(VertxFutures.resultHandler(handler, vertx.getOrCreateContext()));
  }

  @Override
  public void keys(Handler<AsyncResult<Set<K>>> handler) {
    try {
      handler.handle(Future.succeededFuture(map.keySet().stream().collect(Collectors.toSet())));
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  @Override
  public void values(Handler<AsyncResult<List<V>>> handler) {
    try {
      handler.handle(Future.succeededFuture(map.values().stream().map(Versioned::valueOrNull).collect(Collectors.toList())));
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  @Override
  public void entries(Handler<AsyncResult<Map<K, V>>> handler) {
    try {
      handler.handle(Future.succeededFuture(map.entrySet()
          .stream()
          .collect(Collectors.toMap(e -> e.getKey(), e -> Versioned.valueOrNull(e.getValue())))));
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }
}
