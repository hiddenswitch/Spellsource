package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.strands.SuspendableAction1;
import io.vertx.core.Handler;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;

public class PersistenceHandler<T extends GameEvent> {
	private SuspendableAction1<PersistenceContext<T>> handler;
	private String id;
	private GameEventType type;
	private Attribute attribute;

	public PersistenceHandler(SuspendableAction1<PersistenceContext<T>> handler, String id, GameEventType type, Attribute attribute) {
		this.setHandler(handler);
		this.setId(id);
		this.setType(type);
		this.setAttribute(attribute);
	}

	public SuspendableAction1<PersistenceContext<T>> getHandler() {
		return handler;
	}

	public void setHandler(SuspendableAction1<PersistenceContext<T>> handler) {
		this.handler = handler;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public GameEventType getType() {
		return type;
	}

	public void setType(GameEventType type) {
		this.type = type;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
}
