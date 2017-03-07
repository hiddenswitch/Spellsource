package com.hiddenswitch.proto3.net.models;

import net.demilich.metastone.game.events.GameEvent;

import java.io.Serializable;

/**
 * Created by bberman on 2/19/17.
 */
public class EventLogicRequest<T extends GameEvent> extends LogicRequest implements Serializable {
	private String cardInventoryId;
	private int entityId;
	private T event;

	public String getCardInventoryId() {
		return cardInventoryId;
	}

	public void setCardInventoryId(String cardInventoryId) {
		this.cardInventoryId = cardInventoryId;
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public EventLogicRequest withCardInstanceId(final String cardInstanceId) {
		this.cardInventoryId = cardInstanceId;
		return this;
	}

	public EventLogicRequest withEntityId(final int entityId) {
		this.entityId = entityId;
		return this;
	}

	public T getEvent() {
		return event;
	}

	public void setEvent(T event) {
		this.event = event;
	}

	public EventLogicRequest withEvent(final T event) {
		this.event = event;
		return this;
	}
}
