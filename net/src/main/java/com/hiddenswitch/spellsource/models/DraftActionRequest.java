package com.hiddenswitch.spellsource.models;

import java.io.Serializable;

public class DraftActionRequest implements Serializable {
	private String userId;
	private int heroIndex = -1;
	private int cardIndex = -1;

	public int getHeroIndex() {
		return heroIndex;
	}

	public void setHeroIndex(int heroIndex) {
		this.heroIndex = heroIndex;
	}

	public int getCardIndex() {
		return cardIndex;
	}

	public void setCardIndex(int cardIndex) {
		this.cardIndex = cardIndex;
	}

	public DraftActionRequest withUserId(String userId) {
		this.setUserId(userId);
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public DraftActionRequest withHeroIndex(final int heroIndex) {
		this.heroIndex = heroIndex;
		return this;
	}

	public DraftActionRequest withCardIndex(final int cardIndex) {
		this.cardIndex = cardIndex;
		return this;
	}
}
