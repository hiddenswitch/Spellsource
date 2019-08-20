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

import io.vertx.core.spi.cluster.ChoosableIterable;

import java.util.Collection;
import java.util.Iterator;

/**
 * Atomix choosable iterable.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
class AtomixChoosableIterable<T> implements ChoosableIterable<T> {
  private final Collection<T> collection;
  private Iterator<T> iterator;

  AtomixChoosableIterable(Collection<T> collection) {
    this.collection = collection;
    this.iterator = collection.iterator();
  }

  @Override
  public boolean isEmpty() {
    return collection.isEmpty();
  }

  @Override
  public T choose() {
    return iterator.next();
  }

  @Override
  public Iterator<T> iterator() {
    return collection.iterator();
  }

}
