package com.hiddenswitch.spellsource.applications;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.spellsource.util.Logging;
import py4j.GatewayServer;
import py4j.Py4JNetworkException;

public class PythonBridge {
	public static void main(String[] args) {
		Logging.setLoggingLevel(Level.OFF);
		GatewayServer gatewayServer = new GatewayServer();
		try {
			gatewayServer.start();
			System.out.println("{\"status\":\"ready\"}");
			Runtime.getRuntime().addShutdownHook(new Thread(() -> gatewayServer.shutdown(true)));
		} catch (Py4JNetworkException ex) {
			System.out.println(String.format("{\"status\":\"failed\", \"message\": \"%s\"}", ex.getCause().getMessage()));
		}
	}
}
