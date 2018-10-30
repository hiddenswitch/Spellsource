package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.server.Configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bberman on 2/11/17.
 */
public class StartGameResponse implements Serializable {
	private List<Configuration> configurations = new ArrayList<>();

	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public StartGameResponse setConfigurations(List<Configuration> configurations) {
		this.configurations = configurations;
		return this;
	}
}
