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
package co.paralleluniverse.strands;

import co.paralleluniverse.common.util.UtilUnsafe;
import co.paralleluniverse.fibers.SuspendExecution;
import sun.misc.Unsafe;

/**
 *
 * @author pron
 */
public class OwnedSynchronizer extends ConditionSynchronizer implements Condition {
    private volatile Strand waiter;

    public OwnedSynchronizer(Object owner) {
        super(owner);
    }

    @Override
    public Object register() {
        final Strand currentStrand = Strand.currentStrand();
        record("register", "%s register %s", this, currentStrand);
        if (!casWaiter(null, currentStrand))
            throw new IllegalMonitorStateException("attempt by " + currentStrand + " but owned by " + waiter);
        return null;
    }

    @Override
    public void unregister(Object registrationToken) {
        final Strand currentStrand = Strand.currentStrand();
        record("unregister", "%s unregister %s", this, currentStrand);
        if (!Strand.equals(waiter, currentStrand))
            throw new IllegalMonitorStateException("attempt by " + Strand.currentStrand() + " but owned by " + waiter);
        waiter = null;
    }

    @Override
    public void signalAll() {
        signal();
    }

    @Override
    public void signal() {
        final Strand s = waiter;
        record("signal", "%s signalling %s", this, s);
        if (s != null)
            Strand.unpark(s, owner);
    }

    public void signalAndWait() throws SuspendExecution {
        final Strand s = waiter;
        if (s != null) {
            record("signal", "%s signalling %s", this, s);
            Strand.yieldAndUnpark(s, owner);
        }
    }
    private static final Unsafe UNSAFE = UtilUnsafe.getUnsafe();
    private static final long waiterOffset;

    static {
        try {
            waiterOffset = UNSAFE.objectFieldOffset(OwnedSynchronizer.class.getDeclaredField("waiter"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private boolean casWaiter(Strand expected, Strand update) {
        return UNSAFE.compareAndSwapObject(this, waiterOffset, expected, update);
    }
}
