package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a request to create a game with the specified deck list.
 * <p>
 * A deck list can be specified in a community format using {@link #fromDeckList(String)} and converted to a deck usable
 * by the engine using {@link #toGameDeck()}. Alternatively, the hero class and card IDs can be specified using
 * {@link #fromCardIds(String, String...)}.
 *
 * @see #fromDeckList(String) to learn more about the format for deck lists.
 * @see net.demilich.metastone.game.GameContext#fromDeckLists(List) to create a game context directly from community
 * deck lists.
 */
public class DeckCreateRequest implements Serializable, Cloneable {
	/**
	 * The maximum number of characters that can be specified in a name.
	 */
	public static final int NAME_MAX_LENGTH = 140;
	private String userId;
	private String name;
	private String heroClass;
	private String heroCardId;
	private boolean draft;
	private String format;
	private boolean isStandardDeck;
	private List<String> inventoryIds = new ArrayList<>();
	private List<String> cardIds = new ArrayList<>();

	/**
	 * Creates a deck list from a specified community format.
	 * <p>
	 * In this format, a deck is in one of two formats:
	 * <ul>
	 * <li>A <b>community decklist</b> format, which is a plain text list of cards.</li>
	 * <li>A <b>varint</b> format, which is a Base 64-encoded list of numbers that correspond to specific cards
	 * (deprecated).</li>
	 * </ul>
	 * <p>
	 * A <b>community decklist</b> format looks like:
	 * <pre>
	 *   Name: Name of the deck
	 *   Class: The color corresponding to the champion.
	 *   Format: The game format this deck belongs to, indicating which cards it can use
	 *   1x Card Name
	 *   2x Card Name
	 *   3x Card Name
	 * </pre>
	 * The specification for these lines:
	 * <ul>
	 * <li><b>Name</b>: The name of the deck that will appear in the client. This does not have to be a unique name. The
	 * name will begin after the first space in the line, if there is one. Whitespace is removed from the end and
	 * beginning of the name. The name is read until the end of the line. Names must be {@link #NAME_MAX_LENGTH}
	 * characters long or fewer.</li>
	 * <li><b>Class</b>: The color corresponding to the champion. Each champion has a color specified in the {@link
	 * CardDesc#getHeroClass()} field of a {@link CardType#CLASS}. You can find the appropriate, all-capitals color by
	 * searching for {@code CLASS} cards in the {@link CardCatalogue}.</li>
	 * <li><b>Format</b>: A format consisting of a set of {@link net.demilich.metastone.game.cards.CardSet}
	 * (strings) in the {@link CardCatalogue#formats()} map.</li>
	 * <li>A list of cards. These are each in the form {@code number}, followed immediately by an {@code x} character,
	 * followed by whitespace, followed by the case-sensitive card name in the {@link CardDesc#getName()} field. Whenever two
	 * cards share a name, the one that is collectible is preferred; otherwise, the chosen card is arbitrary and will
	 * <b>not</b> produce a warning.</li>
	 * </ul>
	 *
	 * @param deckList A community format deck list
	 * @return A request on which {@link DeckCreateRequest#toGameDeck()} can be called to get an actual deck, or which can
	 * be passed to certain network services to create decks for users.
	 * @throws DeckListParsingException that contains a list of more detailed exceptions as to why the deck failed to
	 *                                  parse or any other errors related to it. This ensures you will see all the errors
	 *                                  related to deck list parsing, not just one error.
	 */
	public static DeckCreateRequest fromDeckList(String deckList) throws DeckListParsingException {
		DeckCreateRequest request = new DeckCreateRequest()
				.withCardIds(new ArrayList<>());
		// Parse with a regex
		String regex = "(?:^(?:###*|#*\\s?[Nn]ame:)\\s?(?<name>.+)$)|(?:[Cc]lass:\\s?(?<heroClass>\\w+)$)|(?:[Hh]ero:\\s?(?<heroCard>.+$))|(?:[Ff]ormat:\\s?(?<format>\\w+))|(?:(?<count>\\d+)x(\\s?\\(\\d*\\))?\\s?(?<cardName>.+$))";
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(deckList);
		List<Throwable> errors = new ArrayList<>();
		while (matcher.find()) {
			if (matcher.group("name") != null) {
				try {
					String name = matcher.group("name").trim();
					if (name.length() == 0) {
						throw new IllegalArgumentException("Name must contain at least one non-whitespace character.");
					}
					request.setName(name.substring(0, Math.min(name.length(), NAME_MAX_LENGTH)));
				} catch (IllegalArgumentException ex) {
					errors.add(ex);
				}

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
					if (!ClasspathCardCatalogue.INSTANCE.formats().keySet().contains(format)) {
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
					request.setHeroCardId(ClasspathCardCatalogue.INSTANCE.getCardByName(heroCard).getCardId());
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
					cardId = ClasspathCardCatalogue.INSTANCE.getCardByName(cardName, request.getHeroClass()).getCardId();
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
			throw new DeckListParsingException(errors, deckList);
		}

		return request;
	}

	public static DeckCreateRequest fromCardIds(String heroClass, String... cardIds) {
		return new DeckCreateRequest()
				.withFormat("Spellsource")
				.withCardIds(Arrays.asList(cardIds))
				.withHeroClass(heroClass);
	}

	public static DeckCreateRequest fromCardIds(String heroClass, List<String> cardIds) {
		return new DeckCreateRequest()
				.withFormat("Spellsource")
				.withCardIds(cardIds)
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
			deck.setHeroCard(ClasspathCardCatalogue.INSTANCE.getCardById(getHeroCardId()));
		}
		getCardIds().forEach(cardId -> deck.getCards().addCard(ClasspathCardCatalogue.INSTANCE.getCardById(cardId)));
		deck.setFormat(ClasspathCardCatalogue.INSTANCE.getFormat(format));
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
				.withFormat("Spellsource")
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