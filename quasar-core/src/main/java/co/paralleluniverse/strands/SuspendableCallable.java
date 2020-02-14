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

import co.paralleluniverse.fibers.SuspendExecution;

/**
 * This interface can represent any operation that may suspend the currently executing {@link Strand} (i.e. thread or fiber).
 * Unlike {@link SuspendableRunnable}, the operation represented by this interface returns a result.
 * This is just like a {@link java.util.concurrent.Callable}, only suspendable.
 * 
 * @author pron
 */
public interface SuspendableCallable<V> extends java.io.Serializable {
    V run() throws SuspendExecution, InterruptedException;
}
