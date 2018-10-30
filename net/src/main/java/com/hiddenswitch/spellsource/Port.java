package com.hiddenswitch.spellsource;

/**
 * Retrieves the port from the environment.
 */
public interface Port {
	/**
	 * Gets the port we're supposed to be serving from in the environment
	 *
	 * @return A port number.
	 */
	static int port() {
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
}
