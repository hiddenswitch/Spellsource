package com.hiddenswitch.cluster.functions;

import net.demilich.metastone.game.statistics.SimulationResult;
import org.apache.spark.api.java.function.Function;


public class Simulator implements Function<Object, SimulationResult> {
	@Override
	public SimulationResult call(Object gameConfig) {
		// TODO: Rewrite
		throw new UnsupportedOperationException();
	}
}
