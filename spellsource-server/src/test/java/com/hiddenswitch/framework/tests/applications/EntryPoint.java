package com.hiddenswitch.framework.tests.applications;

import org.slf4j.LoggerFactory;

public class EntryPoint {
	public static void main(String[] args) {
		var application = new StandaloneApplication();
		// deploy() captures exceptions in its future; without observing it, a failed deployment
		// exits the JVM silently with code 0
		application.deploy().onFailure(t -> {
			LoggerFactory.getLogger(EntryPoint.class).error("deploy failed", t);
			System.exit(1);
		});
	}
}
