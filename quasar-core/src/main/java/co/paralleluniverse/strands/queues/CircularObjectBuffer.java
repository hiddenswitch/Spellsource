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
public class CircularObjectBuffer<E> extends CircularBuffer<E> {
    private final Object[] array;

    public CircularObjectBuffer(int size, boolean singleProducer) {
        super(size, singleProducer);
        this.array = new Object[capacity];
    }

    @Override
    public boolean enq(E elem) {
        long index = preEnq();
        orderedSet((int) index & mask, elem); // must be orderedSet so as to not be re-ordered with tail bump in postEnq
        postEnq();
        return true;
    }

    @Override
    public Consumer newConsumer() {
        return new ObjectConsumer();
    }

    private class ObjectConsumer extends Consumer {
        private Object value;

        @Override
        protected void grabValue(int index) {
            value = array[index];
        }

        @Override
        protected void clearValue() {
            value = null;
        }

        @Override
        protected E getValue() {
            return (E) value;
        }
    }
    //////////////////////////
    private static final VarHandle ARRAY = MethodHandles.arrayElementVarHandle(Object[].class);
    
    private void orderedSet(int i, Object value) {
        ARRAY.setOpaque(array, i, value);
    }
    
//    private static final int base;
//    private static final int shift;
//
//    static {
//        try {
//            base = UNSAFE.arrayBaseOffset(Object[].class);
//            int scale = UNSAFE.arrayIndexScale(Object[].class);
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
//    private void orderedSet(int i, Object value) {
//        UNSAFE.putOrderedObject(array, byteOffset(i), value);
//    }
}
