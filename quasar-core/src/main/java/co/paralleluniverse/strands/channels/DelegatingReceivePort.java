/*
 * Quasar: lightweight threads and actors for the JVM.
 * Copyright (c) 2013-2015, Parallel Universe Software Co. All rights reserved.
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
package co.paralleluniverse.strands.channels;

import co.paralleluniverse.common.util.DelegatingEquals;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Timeout;
import java.util.concurrent.TimeUnit;

public class DelegatingReceivePort<T> implements ReceivePort<T>, DelegatingEquals {
    protected final ReceivePort<T> target;

    public DelegatingReceivePort(ReceivePort<T> target) {
        if (target == null)
            throw new IllegalArgumentException("target can't be null");
        this.target = target;
    }

    @Override
    public T receive() throws SuspendExecution, InterruptedException {
        return target.receive();
    }

    @Override
    public T receive(long timeout, TimeUnit unit) throws SuspendExecution, InterruptedException {
        return target.receive(timeout, unit);
    }

    @Override
    public T receive(Timeout timeout) throws SuspendExecution, InterruptedException {
        return target.receive(timeout);
    }

    @Override
    public T tryReceive() {
        return target.tryReceive();
    }

    @Override
    public void close() {
        target.close();
    }

    @Override
    public boolean isClosed() {
        return target.isClosed();
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return Channels.delegatingEquals(target, obj);
    }

    @Override
    public String toString() {
        return Channels.delegatingToString(this, target);
    }
}
