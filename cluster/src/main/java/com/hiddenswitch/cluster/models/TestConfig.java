package com.hiddenswitch.cluster.models;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class TestConfig implements Serializable {
	private String deckId1;
	private String deckId2;

	public String getDeckId1() {
		return deckId1;
	}

	public void setDeckId1(String deckId1) {
		this.deckId1 = deckId1;
	}

	public String getDeckId2() {
		return deckId2;
	}

	public void setDeckId2(String deckId2) {
		this.deckId2 = deckId2;
	}

	@Override
	public String toString() {
		return String.format("[TestConfig deckIdToTest = %s, deckId2 = %s]\n", deckId1, deckId2);
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(deckId1);
		builder.append(deckId2);
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		TestConfig rhs = (TestConfig) obj;
		EqualsBuilder builder = new EqualsBuilder();
		return builder
				.append(deckId1, rhs.deckId1)
				.append(deckId2, rhs.deckId2)
				.isEquals();
	}
}
