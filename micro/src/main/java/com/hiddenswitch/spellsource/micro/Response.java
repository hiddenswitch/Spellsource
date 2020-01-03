package com.hiddenswitch.spellsource.micro;

import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
public class Response {
	private List<Integer> actions;

	public Response() {
	}

	public List<Integer> getActions() {
		return actions;
	}

	public Response setActions(List<Integer> actions) {
		this.actions = actions;
		return this;
	}
}
