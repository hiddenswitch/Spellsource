package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.common.GameState;

import java.io.Serializable;

public class PerformGameActionResponse implements Serializable {
	private GameState state;

	public GameState getState() {
		return state;
	}

	public void setState(GameState state) {
		this.state = state;
	}
}
