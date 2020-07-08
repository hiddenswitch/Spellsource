package com.hiddenswitch.spellsource.net;

/**
 * Retrieves the server configuration from the environment.
 */
public interface Configuration {
	/**
	 * Gets the port we're supposed to be serving from in the environment (the HTTP gateway port)
	 *
	 * @return A port number, defaulting to 80 (which requires root).
	 */
	static int apiGatewayPort() {
		String environmentPort = System.getenv("PORT");
		String systemPropertyPort = System.getProperty("spellsource.port");
		int port = 80;
		if (environmentPort != null) {
			port = Integer.parseInt(environmentPort);
		}
		if (systemPropertyPort != null) {
			port = Integer.parseInt(systemPropertyPort);
		}
		return port;
	}

	static int clusterManagerPort() {
		return Integer.parseInt(System.getenv().getOrDefault("ATOMIX_PORT", "5701"));
	}

	static int vertxClusterPort() {
		return Integer.parseInt(System.getenv().getOrDefault("VERTX_CLUSTER_PORT", "5710"));
	}

	static int maxBodyBytes() {
		return 10000;
	}

	static int metricsPort() {
		return 8081;
	}
}
