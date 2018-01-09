package com.hiddenswitch.spellsource.impl.server;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.utils.AttributeMap;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class PregamePlayerConfiguration implements Serializable {
	private final Deck deck;
	private final String name;
	private AttributeMap attributes;
	private boolean isAI;

	public PregamePlayerConfiguration(Deck deck, String name) {
		this.deck = deck;
		this.name = name;
		this.attributes = new AttributeMap();
	}

	public Deck getDeck() {
		return deck;
	}

	public String getName() {
		return name;
	}

	public boolean isAI() {
		return isAI;
	}

	public void setAI(boolean AI) {
		isAI = AI;
	}

	public PregamePlayerConfiguration withAI(boolean AI) {
		isAI = AI;
		return this;
	}

	public PregamePlayerConfiguration withAttributes(final AttributeMap attributes) {
		this.attributes = attributes;
		return this;
	}

	public AttributeMap getAttributes() {
		return attributes;
	}

	public void setAttributes(AttributeMap attributes) {
		this.attributes = attributes;
	}

	public String getUserId() {
		if (getAttributes() != null) {
			return (String) getAttributes().get(Attribute.USER_ID);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("deck", deck)
				.append("name", name)
				.append("attributes", attributes)
				.append("isAi", isAI)
				.toString();
	}
}
