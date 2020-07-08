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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.impl.clustered.ClusterNodeInfo;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.spi.cluster.ChoosableIterable;

/**
 * Implementor must be Thread-Safe
 * <p/>
 * <code>T<code> is ClusterNodeInfo
 * 
 * @see io.vertx.spi.cluster.hazelcast.impl.ChoosableSet
 * @see io.vertx.core.eventbus.impl.clustered.ClusterNodeInfo
 * 
 * @author <a href="mailto:leo.tu.taipei@gmail.com">Leo Tu</a>
 */
class ChoosableSet<T> implements ChoosableIterable<T> {

  private final Vertx vertx;

  private final Set<T> ids;
  private volatile Iterator<T> iter;

  public ChoosableSet(Vertx vertx, Collection<? extends T> values) {
    Objects.requireNonNull(values, "values");
    this.vertx = vertx;
    this.ids = new ConcurrentHashSet<>(values.size());
    this.ids.addAll(values);
  }

  public int size() {
    return ids.size();
  }

  public void add(T elem) {
    ids.add(elem);
  }

  public void remove(T elem) {
    ids.remove(elem);
  }

  public void merge(ChoosableSet<T> toMerge) {
    ids.addAll(toMerge.ids);
  }

  public boolean isEmpty() {
    return ids.isEmpty();
  }

  @Override
  public Iterator<T> iterator() {
    return ids.iterator();
  }

  public synchronized T choose() {
    T elem = chooseOne();
    if (elem != null && elem instanceof ClusterNodeInfo) {
      ClusterNodeInfo ci = (ClusterNodeInfo) elem;
      final String selfNodeId = Utility.clusterManager(vertx).getNodeID();
      if (!ci.nodeId.equals(selfNodeId)) {
        final Map<String, String> clusterMap = Utility.clusterMap(vertx);
        if (!clusterMap.isEmpty() && !clusterMap.containsKey(ci.nodeId)) {  // filter
          int max = clusterMap.size();
          for (int i = 0; i < max - 1; i++) {
            T next = chooseOne();
            if (next == null) {
              break;
            }
            ClusterNodeInfo nextci = (ClusterNodeInfo) next;
            if (nextci.nodeId.equals(selfNodeId) || clusterMap.containsKey(nextci.nodeId)) {
              elem = next;
              break;
            }
          }
        }
      }
    }
    return elem;
  }

  private T chooseOne() {
    if (!ids.isEmpty()) {
      if (iter == null || !iter.hasNext()) {
        iter = ids.iterator();
      }
      try {
        return iter.next();
      } catch (NoSuchElementException e) {
        return null;
      }
    } else {
      return null;
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ChoosableSet other = (ChoosableSet) obj;
    if (ids.size() != other.size()) {
      return false;
    }
    return ids.containsAll(other.ids) && other.ids.containsAll(ids);
  }

  @Override
  public String toString() {
    List<String> strs = new ArrayList<>();
    Arrays.asList(ids.toArray()).forEach(e -> strs.add(e.toString()));
    return super.toString() + "{ids=" + strs.toString() + "}";
  }
}
