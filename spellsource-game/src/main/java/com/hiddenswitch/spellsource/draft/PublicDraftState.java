package com.hiddenswitch.spellsource.draft;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.GameDeck;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Public information about the player's draft.
 * <p>
 * This information contains no secret information the player could cheat with. It also includes the win and loss
 * history.
 * <p>
 * By default, the draft status is initialized to {@link DraftStatus#NOT_STARTED}.
 * <p>
 * The public draft state is populated by the {@link DraftLogic}. Use a {@link DraftContext} to populate the state.
 *
 * @see DraftContext for using this state data
 * @see PrivateDraftState for the private / secret information, like the cards that will be shown to the user throughout
 * 		the draft.
 */
public class PublicDraftState implements Serializable {
	private List<String> heroClassChoices;
	private List<String> currentCardChoices;
	private String heroClass;
	private DraftStatus status;
	private List<String> selectedCards;
	private int cardsRemaining;
	private int draftIndex;
	private int wins;
	private int losses;
	private String deckId;

	/**
	 * Creates a new public draft state that represents a draft that has {@link DraftStatus#NOT_STARTED}.
	 */
	public PublicDraftState() {
		this.setCurrentCardChoices(Collections.emptyList());
		this.setHeroClassChoices(Collections.emptyList());
		this.setStatus(DraftStatus.NOT_STARTED);
		this.setSelectedCards(new ArrayList<>());
		this.setCardsRemaining(DraftLogic.ROUNDS);
		this.setDraftIndex(0);
	}

	/**
	 * Creates a {@link Deck} that corresponds to the current deck built by the cards selected in this state.
	 *
	 * @return A {@link GameDeck} instance (usable by a {@link net.demilich.metastone.game.GameContext}).
	 */
	public Deck createDeck() {
		GameDeck deck = new GameDeck(this.getHeroClass());
		this.getSelectedCards().forEach(c -> deck.getCards().addCard(CardCatalogue.getCardById(c)));
		return deck;
	}

	/**
	 * Returns the hero class choices if the draft state is {@link DraftStatus#SELECT_HERO}, or an empty list if there are
	 * no choices, the choice has already been made, or the state is not valid for choosing heroes.
	 *
	 * @return
	 */
	public List<String> getHeroClassChoices() {
		return heroClassChoices;
	}

	public void setHeroClassChoices(List<String> heroClassChoices) {
		this.heroClassChoices = heroClassChoices;
	}

	/**
	 * Gets the current card choices for the current round of the draft, or an empty list if there are no choices, the
	 * draft is over or otherwise the state is not valid for choosing cards.
	 *
	 * @return
	 */
	public List<String> getCurrentCardChoices() {
		return currentCardChoices;
	}

	public void setCurrentCardChoices(List<String> currentCardChoices) {
		this.currentCardChoices = currentCardChoices;
	}

	/**
	 * Gets the chosen hero class or {@code null} if one has not been chosen yet.
	 *
	 * @return
	 */
	public String getHeroClass() {
		return heroClass;
	}

	public void setHeroClass(String heroClass) {
		this.heroClass = heroClass;
	}

	/**
	 * Returns the current status of the draft.
	 *
	 * @return
	 */
	public DraftStatus getStatus() {
		return status;
	}

	public void setStatus(DraftStatus status) {
		this.status = status;
	}

	/**
	 * Gets a list of card IDs the player has chosen so far.
	 *
	 * @return
	 */
	public List<String> getSelectedCards() {
		return selectedCards;
	}

	public void setSelectedCards(List<String> selectedCards) {
		this.selectedCards = selectedCards;
	}

	/**
	 * Gets how many cards remain to be chosen.
	 *
	 * @return
	 */
	public int getCardsRemaining() {
		return cardsRemaining;
	}

	public void setCardsRemaining(int cardsRemaining) {
		this.cardsRemaining = cardsRemaining;
	}

	/**
	 * Returns the current round.
	 *
	 * @return
	 */
	public int getDraftIndex() {
		return draftIndex;
	}

	public void setDraftIndex(int draftIndex) {
		this.draftIndex = draftIndex;
	}

	/**
	 * Gets the number of wins the player has had with the currently drafted deck.
	 *
	 * @return
	 */
	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	/**
	 * Gets the number of losses the player has had with the currently drafted deck.
	 *
	 * @return
	 */
	public int getLosses() {
		return losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	/**
	 * Gets the {@code CollectionRecord} that corresponds to this draft's deck in the {@code net} services for
	 * Spellsource.
	 *
	 * @return
	 */
	public String getDeckId() {
		return deckId;
	}

	public void setDeckId(String deckId) {
		this.deckId = deckId;
	}
}
