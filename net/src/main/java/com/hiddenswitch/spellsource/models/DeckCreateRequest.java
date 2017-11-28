package com.hiddenswitch.spellsource.models;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.HeroCard;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeckCreateRequest implements Serializable, Cloneable {
	static Logger logger = LoggerFactory.getLogger(DeckCreateRequest.class);
	private String userId;
	private String name;
	private HeroClass heroClass;
	private String heroCardId;
	private boolean draft;
	private List<String> inventoryIds = new ArrayList<>();
	private List<String> cardIds = new ArrayList<>();

	public static DeckCreateRequest fromDeckCatalogue(String name) {
		try {
			DeckCatalogue.loadDecksFromPackage();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		DeckCreateRequest request = new DeckCreateRequest()
				.withCardIds(new ArrayList<>());
		final Deck deck = DeckCatalogue.getDeckByName(name);
		for (Card card : deck.getCards()) {
			request.getCardIds().add(card.getCardId());
		}

		request.setName(deck.getName());
		request.setHeroClass(deck.getHeroClass());

		return request;
	}

	public static DeckCreateRequest fromDeckList(String deckList) throws Exception {
		DeckCreateRequest request = new DeckCreateRequest()
				.withCardIds(new ArrayList<>());
		// Parse with a regex
		String regex = "(?:###\\s?(?<name>.+$))|(?:Class:\\s?(?<heroClass>\\w+))|(?:Hero:\\s?(?<heroCard>\\w+))|(?:(?<count>\\d+)x[\\s\\(\\)\\d]+(?<cardName>.+$))";
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

			if (matcher.group("heroCard") != null) {
				request.setHeroCardId(CardCatalogue.getCardByName(matcher.group("heroCard")).getCardId());
				continue;
			}

			String cardName = matcher.group("cardName");
			if (matcher.group("count") != null
					&& cardName != null) {
				int count = Integer.parseInt(matcher.group("count"));
				String cardId;
				try {
					cardId = CardCatalogue.getCardByName(cardName).getCardId();
				} catch (NullPointerException ex) {
					logger.error(String.format("Could not find card with name %s while reading deck list %s", cardName, request.getName()));
					continue;
				}

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
		DeckCreateRequest clone;
		try {
			clone = (DeckCreateRequest) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
		clone.inventoryIds = new ArrayList<>();
		clone.cardIds = new ArrayList<>();
		clone.inventoryIds.addAll(inventoryIds);
		clone.cardIds.addAll(cardIds);
		return clone;
	}

	public Deck toGameDeck() {
		Deck deck = new Deck(getHeroClass());
		deck.setName(getName());
		if (getHeroCardId() != null) {
			deck.setHeroCard((HeroCard) CardCatalogue.getCardById(getHeroCardId()));
		}
		getCardIds().forEach(cardId -> deck.getCards().addCard(CardCatalogue.getCardById(cardId)));
		return deck;
	}

	public String getHeroCardId() {
		return heroCardId;
	}

	public void setHeroCardId(String heroCardId) {
		this.heroCardId = heroCardId;
	}

	public DeckCreateRequest withHeroCardId(final String heroCardId) {
		this.heroCardId = heroCardId;
		return this;
	}
}