package com.hiddenswitch.spellsource;

import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.util.MethodContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class EnvelopeMap {
	Map<Function, SuspendableAction1> routes = new HashMap<>();

	public void put(Function route, SuspendableAction1 handler) {
		routes.put(route, handler);
	}

	public Set<Map.Entry<Function, SuspendableAction1>> entries() {
		return routes.entrySet();
	}
}
