package com.hiddenswitch.spellsource.core;

/**
 * Allows the code to easily access version information.
 */
public interface Version {
	/**
	 * Retrieve this server version (without necessarily depending on reflection like examples on the Internet do)
	 *
	 * @return A semver version.
	 */
	static String version() {
		return "0.8.87";
	}
}
