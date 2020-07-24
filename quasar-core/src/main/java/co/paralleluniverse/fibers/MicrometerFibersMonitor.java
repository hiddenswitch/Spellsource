package co.paralleluniverse.fibers;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Tags;

import java.util.concurrent.atomic.AtomicInteger;

import static io.micrometer.core.instrument.Metrics.*;

class MicrometerFibersMonitor extends MetricsFibersMonitor {
	private final AtomicInteger activeCount = new AtomicInteger();
	private final AtomicInteger waitingCount = new AtomicInteger();
	private final Counter spuriousWakeups;
	private final DistributionSummary timedParkLatency;

	public MicrometerFibersMonitor(String name, FiberScheduler scheduler, boolean detailedInfo) {
		gauge(metric(name, NUM_ACTIVE_FIBERS), activeCount);
		gauge(metric(name, NUM_WAITING_FIBERS), waitingCount);
		spuriousWakeups = counter(metric(name, SPURIOUS_WAKEUPS));
		timedParkLatency = summary(metric(name, TIMED_PARK_LATENCY));
		gaugeMapSize(metric(name, RUNAWAY_FIBERS), Tags.empty(), problemFibers);
	}

	@Override
	public void fiberStarted(Fiber fiber) {
		activeCount.incrementAndGet();
	}

	@Override
	public void fiberResumed() {
		activeCount.decrementAndGet();
	}

	@Override
	public void fiberSuspended() {
		waitingCount.incrementAndGet();
	}

	@Override
	public void fiberTerminated(Fiber fiber) {
		waitingCount.decrementAndGet();
	}

	@Override
	public void spuriousWakeup() {
		spuriousWakeups.increment();
	}

	@Override
	public void timedParkLatency(long ns) {
		timedParkLatency.record(ns);
	}

	@Override
	public void unregister() {
	}
}
