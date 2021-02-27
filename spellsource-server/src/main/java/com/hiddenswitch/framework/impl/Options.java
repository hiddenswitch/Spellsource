package com.hiddenswitch.framework.impl;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

public final class Options extends OptionsBase {
	@Option(name = "migrate",
			abbrev = 'm',
			help = "Execute a migration and exit on success or error. Reads the Spellsource configuration files as expected.",
			category = "Startup",
			defaultValue = "false")
	public boolean migrate;

	@Option(name = "write-default-config",
			abbrev = 'd',
			help = "Writes the default configuration as JSON to the path specified",
			category = "Configuration",
			defaultValue = "")
	public String writeDefaultConfig;

	@Option(name = "write-current-config",
			abbrev = 'o',
			help = "Reads the configuration from the environment as JSON and writes it to the path specified",
			category = "Configuration",
			defaultValue = "")
	public String writeCurrentConfig;

	public Options() {
	}
}
