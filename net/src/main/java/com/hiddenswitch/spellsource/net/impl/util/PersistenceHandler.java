package com.hiddenswitch.spellsource.net.impl.util;

import co.paralleluniverse.strands.SuspendableAction1;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;

public class PersistenceHandler<T extends GameEvent> {
	private SuspendableAction1<PersistenceContext<T>> handler;
	private String id;
	private EventTypeEnum type;
	private Attribute attribute;

	public PersistenceHandler(SuspendableAction1<PersistenceContext<T>> handler, String id, EventTypeEnum type, Attribute attribute) {
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

	public EventTypeEnum getType() {
		return type;
	}

	public void setType(EventTypeEnum type) {
		this.type = type;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
}
