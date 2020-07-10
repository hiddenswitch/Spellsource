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

import co.paralleluniverse.common.monitoring.Metrics;
import co.paralleluniverse.strands.Strand;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pron
 */
class DropwizardFibersMonitor extends MetricsFibersMonitor {
	private final Counter activeCount;
    //private final Counter runnableCount;
    private final Counter waitingCount;
    private final Meter spuriousWakeups;
    private final Histogram timedParkLatency;
    private final Gauge<Map<String, String>> runawayFibers;

	public DropwizardFibersMonitor(String name, FiberScheduler scheduler) {
        this.activeCount = Metrics.counter(metric(name, NUM_ACTIVE_FIBERS));
        this.waitingCount = Metrics.counter(metric(name, NUM_WAITING_FIBERS));
        this.spuriousWakeups = Metrics.meter(metric(name, SPURIOUS_WAKEUPS));
        this.timedParkLatency = Metrics.histogram(metric(name, TIMED_PARK_LATENCY));
        this.runawayFibers = new Gauge<Map<String, String>>() {
            @Override
            public Map<String, String> getValue() {
                Map<String, String> map = new HashMap<>();
                if (problemFibers != null) {
                    for (Map.Entry<Fiber, StackTraceElement[]> e : problemFibers.entrySet())
                        map.put(e.getKey().toString(), Strand.toString(e.getValue()));
                }
                return map;
            }
        };
        Metrics.register(RUNAWAY_FIBERS, runawayFibers);
    }

	@Override
    public void unregister() {
    }

    @Override
    public void fiberStarted(Fiber fiber) {
        activeCount.inc();
    }

    @Override
    public void fiberTerminated(Fiber fiber) {
        activeCount.dec();
        //runnableCount.dec();
    }

    @Override
    public void fiberSuspended() {
        //runnableCount.dec();
        waitingCount.inc();
    }

    @Override
    public void fiberResumed() {
        //runnableCount.inc();
        waitingCount.dec();
    }

    @Override
    public void spuriousWakeup() {
        spuriousWakeups.mark();
    }

    @Override
    public void timedParkLatency(long ns) {
        timedParkLatency.update(ns);
    }

}
