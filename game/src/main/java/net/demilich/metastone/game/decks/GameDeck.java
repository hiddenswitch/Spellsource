package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A deck that contains cards, a name, a hero card, etc. that can be actually used to populate a player's deck in a
 * {@link net.demilich.metastone.game.GameContext}.
 * <p>
 * Create an instance of this class and add cards using {@link #getCards()}'s {@link CardList#addCard(String)} method.
 * Set the hero class using {@link #setHeroClass(HeroClass)}. Set the format using {@link #setFormat(DeckFormat)}. This
 * format should correspond to the one the {@link GameContext#getDeckFormat()} uses.
 */
public class GameDeck implements Serializable, Cloneable, Deck {
	public static final GameDeck EMPTY;
	protected String deckId;
	protected CardList cards = new CardArrayList();

	private String name = "";
	private HeroClass heroClass;
	private Card heroCard;
	private DeckFormat format;
	private String description;
	private String filename;
	private boolean arbitrary;

	static {
		EMPTY = new GameDeck(HeroClass.ANY);
	}

	public GameDeck() {
	}

	public GameDeck(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	public GameDeck(HeroClass heroClass, boolean arbitrary) {
		this.heroClass = heroClass;
		this.arbitrary = arbitrary;
	}

	public int containsHowMany(Card card) {
		int count = 0;
		for (Card cardInDeck : cards) {
			if (card.getCardId().equals(cardInDeck.getCardId())) {
				count++;
			}
		}
		return count;
	}

	public CardList getCards() {
		return cards;
	}

	public CardList getCardsCopy() {
		return getCards().clone();
	}

	public String getDescription() {
		return description;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public String getName() {
		return name;
	}

	public boolean isArbitrary() {
		return arbitrary;
	}

	public boolean isComplete() {
		return cards.getCount() == GameLogic.DECK_SIZE;
	}

	public boolean isFull() {
		return cards.getCount() == GameLogic.MAX_DECK_SIZE;
	}

	public boolean isTooBig() {
		return cards.getCount() > GameLogic.DECK_SIZE;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setHeroClass(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[Deck - ");
		builder.append("name: ");
		builder.append(name);
		builder.append("; cards: ");
		for (Card card : cards) {
			builder.append(card.getCardId());
			builder.append(", ");
		}
		builder.append("]");
		return builder.toString();
	}

	public Card getHeroCard() {
		if (heroCard == null) {
			return HeroClass.getHeroCard(getHeroClass());
		}
		return heroCard;
	}

	public void setHeroCard(Card heroCard) {
		this.heroCard = heroCard;
	}

	public DeckFormat getFormat() {
		if (format == null) {
			// Retrieve the format that is implied by the cards inside this deck.
			Set<CardSet> cardSets = getCards().stream().map(Card::getCardSet).collect(Collectors.toSet());
			return DeckFormat.getSmallestSupersetFormat(cardSets);
		}

		return format;
	}

	public void setFormat(DeckFormat format) {
		this.format = format;
	}

	@Override
	public Deck clone() {
		try {
			GameDeck clone = (GameDeck) super.clone();
			clone.cards = cards.getCopy();
			clone.heroCard = heroCard == null ? null : heroCard.clone();
			clone.format = format == null ? null : format.clone();
			return clone;
		} catch (CloneNotSupportedException ignored) {
			return null;
		}
	}

	@Override
	public String getDeckId() {
		return deckId;
	}

	public Deck setDeckId(String deckId) {
		this.deckId = deckId;
		return this;
	}

	public Deck setCards(CardList cards) {
		this.cards = cards;
		return this;
	}

	public Deck setArbitrary(boolean arbitrary) {
		this.arbitrary = arbitrary;
		return this;
	}
}
