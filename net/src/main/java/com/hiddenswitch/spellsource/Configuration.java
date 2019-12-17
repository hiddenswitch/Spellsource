package com.hiddenswitch.spellsource;

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

	static int atomixPort() {
		return Integer.parseInt(System.getenv().getOrDefault("ATOMIX_PORT", "5701"));
	}

	static int vertxClusterPort() {
		return Integer.parseInt(System.getenv().getOrDefault("VERTX_CLUSTER_PORT", "5710"));
	}

	static String atomixBootstrapNode() {
		return System.getenv().getOrDefault("ATOMIX_BOOTSTRAP_NODE", null);
	}

	static int maxBodyBytes() {return 10000;}
}
