package com.hiddenswitch.spellsource;

public interface Port {
	/**
	 * Gets the port we're supposed to be serving from in the environment
	 *
	 * @return A port number.
	 */
	static int port() {
		String environmentPort = System.getenv("PORT");
		int port = 80;
		if (environmentPort != null) {
			port = Integer.parseInt(environmentPort);
		}
		return port;
	}
}
