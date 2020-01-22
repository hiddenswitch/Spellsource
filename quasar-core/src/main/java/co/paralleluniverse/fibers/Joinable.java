/*
 * Quasar: lightweight threads and actors for the JVM.
 * Copyright (c) 2013-2014, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *  
 *   or (per the licensee's choosing)
 *  
 * under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package co.paralleluniverse.fibers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author pron
 */
public interface Joinable<V> {
    @Suspendable
    void join() throws ExecutionException, InterruptedException;

    @Suspendable
    void join(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException;

    @Suspendable
    V get() throws ExecutionException, InterruptedException;

    @Suspendable
    V get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException;

    boolean isDone();
}
