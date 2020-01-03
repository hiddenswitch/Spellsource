package com.hiddenswitch.spellsource.micro;

//import io.micronaut.core.annotation.Introspected;
import net.demilich.metastone.game.logic.Trace;

//@Introspected
public class Payload {
	private Trace trace;
	private int playerId;

	public Payload() {
	}

	public Trace getTrace() {
		return trace;
	}

	public Payload setTrace(Trace trace) {
		this.trace = trace;
		return this;
	}

	public int getPlayerId() {
		return playerId;
	}

	public Payload setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}
}
