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
package co.paralleluniverse.strands.queues;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 *
 * @author pron
 */
abstract class CircularDWordBuffer<E> extends CircularBuffer<E> {
    private final long[] array;

    public CircularDWordBuffer(int size, boolean singleProducer) {
        super(size, singleProducer);
        this.array = new long[capacity];
    }

    void enqRaw(long elem) {
        long index = preEnq();
        orderedSet((int) index & mask, elem); // must be orderedSet so as to not be re-ordered with tail bump in postEnq
        postEnq();
    }

    abstract class DWordConsumer extends Consumer {
        private long value;

        @Override
        protected void grabValue(int index) {
            value = array[index];
        }

        @Override
        protected void clearValue() {
        }

        long getRawValue() {
            return value;
        }
    }
    //////////////////////////

    private static final VarHandle ARRAY = MethodHandles.arrayElementVarHandle(long[].class);

    private void orderedSet(int i, long value) {
        ARRAY.setOpaque(array, i, value);
    }
    
//    private static final int base;
//    private static final int shift;
//
//    static {
//        try {
//            base = UNSAFE.arrayBaseOffset(long[].class);
//            int scale = UNSAFE.arrayIndexScale(long[].class);
//            if ((scale & (scale - 1)) != 0)
//                throw new Error("data type scale not a power of two");
//            shift = 31 - Integer.numberOfLeadingZeros(scale);
//        } catch (Exception ex) {
//            throw new Error(ex);
//        }
//    }
//
//    private static long byteOffset(int i) {
//        return ((long) i << shift) + base;
//    }
//
//    private void orderedSet(int i, long value) {
//        UNSAFE.putOrderedLong(array, byteOffset(i), value);
//    }
}
