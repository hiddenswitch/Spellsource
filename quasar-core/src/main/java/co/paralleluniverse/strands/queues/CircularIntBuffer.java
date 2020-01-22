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

/**
 *
 * @author pron
 */
public class CircularIntBuffer extends CircularWordBuffer<Integer> implements BasicSingleConsumerIntQueue {
    public CircularIntBuffer(int size, boolean singleProducer) {
        super(size, singleProducer);
    }

    @Override
    public boolean enq(Integer elem) {
        return enq(elem.intValue());
    }

    @Override
    public boolean enq(int elem) {
        enqRaw(elem);
        return true;
    }

    @Override
    public int pollInt() {
        return ((IntConsumer)consumer).pollInt();
    }

    @Override
    public IntConsumer newConsumer() {
        return new IntConsumer();
    }

    public class IntConsumer extends WordConsumer {
        public int getIntValue() {
            return getRawValue();
        }

        @Override
        protected Integer getValue() {
            return getIntValue();
        }

        public int pollInt() {
            poll0();
            return getIntValue();
        }
    }
}
