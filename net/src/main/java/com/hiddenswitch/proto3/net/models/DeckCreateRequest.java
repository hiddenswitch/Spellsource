package com.hiddenswitch.proto3.net.models;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeckCreateRequest implements Serializable, Cloneable {
	private String userId;
	private String name;
	private HeroClass heroClass;
	private boolean draft;
	private List<String> inventoryIds;
	private List<String> cardIds;

	public static DeckCreateRequest fromDeckList(String deckList) throws Exception {
		DeckCreateRequest request = new DeckCreateRequest()
				.withCardIds(new ArrayList<>());
		// Parse with a regex
		String regex = "(?:###\\s?(?<name>.+$))|(?:Class:\\s?(?<heroClass>\\w+))|(?:(?<count>\\d+)x[\\s\\(\\)\\d]+(?<cardName>.+$))";
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(deckList);
		while (matcher.find()) {
			if (matcher.group("name") != null) {
				request.setName(matcher.group("name"));
				continue;
			}

			if (matcher.group("heroClass") != null) {
				request.setHeroClass(HeroClass.valueOf(matcher.group("heroClass").toUpperCase()));
				continue;
			}

			if (matcher.group("count") != null
					&& matcher.group("cardName") != null) {
				int count = Integer.parseInt(matcher.group("count"));
				String cardId = CardCatalogue.getCardByName(matcher.group("cardName")).getCardId();
				for (int i = 0; i < count; i++) {
					request.getCardIds().add(cardId);
				}
			}
		}

		return request;
	}

	public String getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public void setHeroClass(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	public List<String> getInventoryIds() {
		return inventoryIds;
	}

	public void setInventoryIds(List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
	}

	public DeckCreateRequest withUserId(final String userId) {
		this.userId = userId;
		return this;
	}

	public DeckCreateRequest withName(final String name) {
		this.name = name;
		return this;
	}

	public DeckCreateRequest withHeroClass(final HeroClass heroClass) {
		this.heroClass = heroClass;
		return this;
	}

	public DeckCreateRequest withInventoryIds(final List<String> inventoryIds) {
		this.inventoryIds = inventoryIds;
		return this;
	}

	public List<String> getCardIds() {
		return cardIds;
	}

	public void setCardIds(List<String> cardIds) {
		this.cardIds = cardIds;
	}

	public DeckCreateRequest withCardIds(final List<String> cardIds) {
		this.cardIds = cardIds;
		return this;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public DeckCreateRequest withDraft(final boolean draft) {
		this.draft = draft;
		return this;
	}

	@Override
	public DeckCreateRequest clone() {
		try {
			return (DeckCreateRequest) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}