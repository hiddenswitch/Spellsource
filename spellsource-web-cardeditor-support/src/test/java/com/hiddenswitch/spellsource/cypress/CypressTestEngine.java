package com.hiddenswitch.spellsource.cypress;

import org.junit.platform.engine.*;

public class CypressTestEngine implements TestEngine {
	@Override
	public String getId() {
		return null;
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		return null;
	}

	@Override
	public void execute(ExecutionRequest request) {

	}
}
