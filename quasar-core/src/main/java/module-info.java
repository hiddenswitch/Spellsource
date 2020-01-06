/*
 * Quasar: lightweight threads and actors for the JVM.
 * Copyright (c) 2018, Parallel Universe Software Co. All rights reserved.
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
open module co.paralleluniverse.quasar.core {
	requires java.management;
	requires java.instrument;
	requires jdk.unsupported; // needed for ThreadAccess and ExtendedStackTraceHotSpot
	requires com.google.common;
	requires org.objectweb.asm;
	requires org.objectweb.asm.util;
	requires org.objectweb.asm.commons;
	requires objenesis;

	exports co.paralleluniverse.fibers;
	exports co.paralleluniverse.fibers.futures;
	exports co.paralleluniverse.fibers.io;
	exports co.paralleluniverse.fibers.instrument;
	exports co.paralleluniverse.remote;
	exports co.paralleluniverse.strands;
	exports co.paralleluniverse.strands.channels;
	exports co.paralleluniverse.strands.channels.transfer;
	exports co.paralleluniverse.strands.concurrent;
	exports co.paralleluniverse.strands.dataflow;

	exports co.paralleluniverse.common.util;
	exports co.paralleluniverse.common.monitoring;
	exports co.paralleluniverse.common.reflection;
	exports co.paralleluniverse.common.test;
	exports co.paralleluniverse.concurrent.util;
	exports co.paralleluniverse.io.serialization;
	exports co.paralleluniverse.strands.queues;

	uses co.paralleluniverse.fibers.instrument.SuspendableClassifier;
}