package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A deck that contains cards, a name, a hero card, etc. that can be actually used to populate a player's deck in a
 * {@link net.demilich.metastone.game.GameContext}.
 * <p>
 * Create an instance of this class and add cards using {@link #getCards()}'s {@link CardList#addCard(CardCatalogue, String)} method.
 * Set the hero class using {@link #setHeroClass(String)}. Set the format using {@link #setFormat(DeckFormat)}. This
 * format should correspond to the one the {@link GameContext#getDeckFormat()} uses.
 */
public class GameDeck implements Serializable, Cloneable, Deck {
	public static final GameDeck EMPTY;
	protected String deckId;
	protected CardList cards = new CardArrayList();

	private String name = "";
	private String heroClass;
	private Card heroCard;
	private DeckFormat format;
	private String description;
	private AttributeMap playerAttributes;

	static {
		EMPTY = new GameDeck(HeroClass.ANY);
	}

	public GameDeck() {
	}

	public GameDeck(String heroClass) {
		this.heroClass = heroClass;
	}

	public GameDeck(String heroClass, boolean arbitrary) {
		this.heroClass = heroClass;
	}

	public GameDeck(CardCatalogue cardCatalogue, String heroClass1, List<String> cardIds1) {
		this.heroClass = heroClass1;
		for (String cardId : cardIds1) {
			getCards().addCard(cardCatalogue, cardId);
		}
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

	public String getHeroClass() {
		return heroClass;
	}

	public String getName() {
		return name;
	}

	public boolean isComplete() {
		return cards.getCount() == GameLogic.DECK_SIZE;
	}

	public boolean isFull() {
		return cards.getCount() == GameLogic.MAX_DECK_SIZE;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHeroClass(String heroClass) {
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

	public Card getHeroCard(CardCatalogue cardCatalogue) {
		if (heroCard == null) {
			String heroClass1 = getHeroClass();
			return cardCatalogue.getHeroCard(heroClass1);
		}
		return heroCard;
	}

	public void setHeroCard(Card heroCard) {
		this.heroCard = heroCard;
	}

	public DeckFormat getFormat() {
		if (format == null) {
			// Retrieve the format that is implied by the cards inside this deck.
			Set<String> cardSets = getCards().stream().map(Card::getCardSet).collect(Collectors.toSet());
			return ClasspathCardCatalogue.INSTANCE.getSmallestSupersetFormat(cardSets);
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

	public AttributeMap getPlayerAttributes() {
		return playerAttributes;
	}

	public GameDeck setPlayerAttributes(AttributeMap playerAttributes) {
		this.playerAttributes = playerAttributes;
		return this;
	}
}
