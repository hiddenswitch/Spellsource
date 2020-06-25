package co.paralleluniverse.common.monitoring;

import java.util.concurrent.ForkJoinPool;

public class MicrometerForkJoinPoolMonitor extends ForkJoinPoolMonitor {
	public MicrometerForkJoinPoolMonitor(String name, ForkJoinPool fjPool) {
		super(name, fjPool);
	}
}
