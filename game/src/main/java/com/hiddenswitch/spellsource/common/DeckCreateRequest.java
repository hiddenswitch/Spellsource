package com.hiddenswitch.spellsource.common;

import com.hiddenswitch.spellsource.util.CommunityDeckStringSerializer;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a request to create a game with the specified deck list.
 */
public class DeckCreateRequest implements Serializable, Cloneable {
	private String userId;
	private String name;
	private String heroClass;
	private String heroCardId;
	private boolean draft;
	private String format;
	private boolean isStandardDeck;
	private List<String> inventoryIds = new ArrayList<>();
	private List<String> cardIds = new ArrayList<>();

	public static DeckCreateRequest fromDeckList(String deckList) throws DeckListParsingException {
		if (!deckList.startsWith("#")
				&& !deckList.contains("\n")) {
			try {
				return new CommunityDeckStringSerializer().toDeckCreateRequest(null, "Decklist (" + deckList + ")", deckList);
			} catch (Throwable ex) {
				throw new DeckListParsingException(Collections.singletonList(ex));
			}
		}

		DeckCreateRequest request = new DeckCreateRequest()
				.withCardIds(new ArrayList<>());
		// Parse with a regex
		String regex = "(?:^(?:###*|#*\\s?[Nn]ame:)\\s?(?<name>.+)$)|(?:[Cc]lass:\\s?(?<heroClass>\\w+)$)|(?:[Hh]ero:\\s?(?<heroCard>.+$))|(?:[Ff]ormat:\\s?(?<format>\\w+))|(?:(?<count>\\d+)x(\\s?\\(\\d*\\))?\\s?(?<cardName>.+$))";
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(deckList);
		List<Throwable> errors = new ArrayList<>();
		while (matcher.find()) {
			if (matcher.group("name") != null) {
				request.setName(matcher.group("name"));
				continue;
			}

			if (matcher.group("heroClass") != null) {
				String heroClass = matcher.group("heroClass").toUpperCase();
				switch (heroClass.toLowerCase()) {
					case "warlock":
						heroClass = "VIOLET";
						break;
					case "warrior":
						heroClass = "RED";
						break;
					case "priest":
						heroClass = "WHITE";
						break;
					case "rogue":
						heroClass = "BLACK";
						break;
					case "mage":
						heroClass = "BLUE";
						break;
					case "paladin":
						heroClass = "GOLD";
						break;
					case "shaman":
						heroClass = "SILVER";
						break;
					case "druid":
						heroClass = "BROWN";
						break;
					case "hunter":
						heroClass = "GREEN";
						break;
				}
				try {
					request.setHeroClass(heroClass);
				} catch (IllegalArgumentException ex) {
					errors.add(new IllegalArgumentException(String.format("No class named %s could be found", heroClass), ex));
				} catch (NullPointerException ex) {
					errors.add(new NullPointerException("No hero class specified"));
				}
				continue;
			}

			if (matcher.group("format") != null) {
				final String format = matcher.group("format");
				try {
					request.setFormat(format);
					if (!DeckFormat.formats().keySet().contains(format)) {
						throw new IllegalArgumentException();
					}
				} catch (IllegalArgumentException ex) {
					errors.add(new IllegalArgumentException(String.format("No format named %s could be found", format), ex));
				}

				continue;
			}

			if (matcher.group("heroCard") != null) {
				final String heroCard = matcher.group("heroCard");
				try {
					request.setHeroCardId(CardCatalogue.getCardByName(heroCard).getCardId());
				} catch (NullPointerException ex) {
					errors.add(new NullPointerException(String.format("No hero card named %s could be found", heroCard)));
				}
				continue;
			}

			String cardName = matcher.group("cardName");
			if (matcher.group("count") != null
					&& cardName != null) {
				int count = Integer.parseInt(matcher.group("count"));
				String cardId;
				try {
					cardId = CardCatalogue.getCardByName(cardName, request.getHeroClass()).getCardId();
				} catch (NullPointerException ex) {
					String message = String.format("Could not find a card named %s%s", cardName, request.getName() == null ? "" : " while reading deck list " + request.getName());
					errors.add(new NullPointerException(message));
					continue;
				}

				for (int i = 0; i < count; i++) {
					request.getCardIds().add(cardId);
				}
			}
		}

		if (request.getCardIds().size() == 0) {
			errors.add(new IllegalArgumentException(String.format("You must specify a deck with at least 1 card.")));
		}

		if (request.getHeroClass() == null) {
			errors.add(new NullPointerException("You must specify a hero class."));
		}

		if (request.getName() == null || request.getName().length() == 0) {
			errors.add(new NullPointerException("You must specify a name for the deck."));
		}

		if (errors.size() > 0) {
			throw new DeckListParsingException(errors);
		}

		return request;
	}

	public static DeckCreateRequest fromCardIds(String heroClass, String... cardIds) {
		return new DeckCreateRequest()
				.withCardIds(Arrays.asList(cardIds))
				.withHeroClass(heroClass);
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

	public String getHeroClass() {
		return heroClass;
	}

	public void setHeroClass(String heroClass) {
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

	public DeckCreateRequest withHeroClass(final String heroClass) {
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

	public GameDeck toGameDeck() {
		GameDeck deck = new GameDeck(getHeroClass());
		deck.setName(getName());
		if (getHeroCardId() != null) {
			deck.setHeroCard((Card) CardCatalogue.getCardById(getHeroCardId()));
		}
		getCardIds().forEach(cardId -> deck.getCards().addCard(CardCatalogue.getCardById(cardId)));
		deck.setFormat(DeckFormat.getFormat(format));
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

	public boolean isValid() {
		return getName() != null
				&& getHeroClass() != null
				&& HeroClass.getBaseClasses(DeckFormat.getFormat("All")).contains(getHeroClass())
				&& (getCardIds().size() + getInventoryIds().size()) == 30;
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this).toString();
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public DeckCreateRequest withFormat(String format) {
		this.format = format;
		return this;
	}

	public static DeckCreateRequest empty(String userId, String name, String heroClass) {
		return new DeckCreateRequest()
				.withFormat("Standard")
				.withDraft(false)
				.withHeroClass(heroClass)
				.withName(name)
				.withUserId(userId);
	}

	public boolean isStandardDeck() {
		return isStandardDeck;
	}

	public DeckCreateRequest setStandardDeck(boolean standardDeck) {
		isStandardDeck = standardDeck;
		return this;
	}
}