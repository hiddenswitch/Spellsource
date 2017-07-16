package com.hiddenswitch.spellsource.models;

import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.targeting.EntityReference;

import java.io.Serializable;
import java.util.*;

/**
 * Created by bberman on 2/19/17.
 */
public class LogicResponse implements Serializable {
	private Map<EntityReference, Map<Attribute, Object>> modifiedAttributes = new HashMap<>();
	private List<String> gameIdsAffected = new ArrayList<>();
	private List<Integer> entityIdsAffected = new ArrayList<>();

	public List<String> getGameIdsAffected() {
		return gameIdsAffected;
	}

	public void setGameIdsAffected(List<String> gameIdsAffected) {
		this.gameIdsAffected = gameIdsAffected;
	}

	public List<Integer> getEntityIdsAffected() {
		return entityIdsAffected;
	}

	public void setEntityIdsAffected(List<Integer> entityIdsAffected) {
		this.entityIdsAffected = entityIdsAffected;
	}

	public LogicResponse withGameIdsAffected(final List<String> gameIdsAffected) {
		this.gameIdsAffected = gameIdsAffected;
		return this;
	}

	public LogicResponse withEntityIdsAffected(final List<Integer> entityIdsAffected) {
		this.entityIdsAffected = entityIdsAffected;
		return this;
	}

	public Map<EntityReference, Map<Attribute, Object>> getModifiedAttributes() {
		return modifiedAttributes;
	}

	public void setModifiedAttributes(Map<EntityReference, Map<Attribute, Object>> modifiedAttributes) {
		this.modifiedAttributes = modifiedAttributes;
	}

	public LogicResponse withModifiedAttributes(final Map<EntityReference, Map<Attribute, Object>> modifiedAttributes) {
		this.modifiedAttributes = modifiedAttributes;
		return this;
	}

	public static LogicResponse empty() {
		return new LogicResponse()
				.withGameIdsAffected(Collections.emptyList())
				.withEntityIdsAffected(Collections.emptyList())
				.withModifiedAttributes(Collections.emptyMap());
	}
}
