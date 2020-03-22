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

public final class RuntimeSuspendExecution extends RuntimeException { // InterruptedException {
    static final RuntimeSuspendExecution PARK = new RuntimeSuspendExecution(SuspendExecution.PARK);
    static final RuntimeSuspendExecution YIELD = new RuntimeSuspendExecution(SuspendExecution.YIELD);

    public static RuntimeSuspendExecution of(SuspendExecution e) {
        if (e == SuspendExecution.PARK)
            return PARK;
        else if (e == SuspendExecution.YIELD)
            return YIELD;
        else
            return new RuntimeSuspendExecution(e);
    }

    private RuntimeSuspendExecution(SuspendExecution e) {
        super(e);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
