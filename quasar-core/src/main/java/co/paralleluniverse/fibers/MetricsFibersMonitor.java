package co.paralleluniverse.fibers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class MetricsFibersMonitor implements FibersMonitor {
	public static final String NUM_ACTIVE_FIBERS = "numActiveFibers";
	public static final String NUM_WAITING_FIBERS = "numWaitingFibers";
	public static final String SPURIOUS_WAKEUPS = "spuriousWakeups";
	public static final String TIMED_PARK_LATENCY = "timedParkLatency";
	public static final String RUNAWAY_FIBERS = "runawayFibers";
	protected Map<Fiber, StackTraceElement[]> problemFibers;

	protected final String metric(String poolName, String name) {
		return "co.paralleluniverse.fibers." + poolName + "." + name;
	}

	@Override
	public void setRunawayFibers(Collection<Fiber> fs) {
		if (fs == null || fs.isEmpty())
			this.problemFibers = null;
		else {
			this.problemFibers = new HashMap<>();
			for (Fiber f : fs)
				problemFibers.put(f, f.getStackTrace());
		}
	}
}
