package com.hiddenswitch.spellsource.models;

import net.demilich.metastone.game.Attribute;

import java.io.Serializable;
import java.util.List;

/**
 * Created by bberman on 6/6/17.
 */
public class PersistAttributeRequest implements Serializable {
	private String id;
	private EventLogicRequest request;
	private List<String> inventoryIds;
	private Attribute attribute;
	private Object newValue;

	public PersistAttributeRequest withId(String id) {
		this.id = id;
		return this;
	}

	public PersistAttributeRequest withRequest(EventLogicRequest request) {
		this.request = request;
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public EventLogicRequest getRequest() {
		return request;
	}

	public void setRequest(EventLogicRequest request) {
		this.request = request;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public void setInventoryIds(List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public Object getNewValue() {
		return newValue;
	}

	public void setNewValue(Object newValue) {
		this.newValue = newValue;
	}

	public PersistAttributeRequest withInventoryIds(final List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
		return this;
	}

	public PersistAttributeRequest withAttribute(final Attribute attribute) {
		this.attribute = attribute;
		return this;
	}

	public PersistAttributeRequest withNewValue(final Object newValue) {
		this.newValue = newValue;
		return this;
	}
}
