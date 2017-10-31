package com.hiddenswitch.spellsource.draft;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PublicDraftState implements Serializable {
	private List<HeroClass> heroClassChoices;
	private List<String> currentCardChoices;
	private HeroClass heroClass;
	private DraftStatus status;
	private List<String> selectedCards;
	private int cardsRemaining;
	private int draftIndex;
	private int wins;
	private int losses;
	private String deckId;

	public PublicDraftState() {
		this.setCurrentCardChoices(Collections.emptyList());
		this.setHeroClassChoices(Collections.emptyList());
		this.setStatus(DraftStatus.NOT_STARTED);
		this.setSelectedCards(new ArrayList<>());
		this.setCardsRemaining(DraftLogic.DRAFTS);
		this.setDraftIndex(0);
	}

	public Deck createDeck() {
		Deck deck = new Deck(this.getHeroClass());
		this.getSelectedCards().forEach(c -> deck.getCards().addCard(CardCatalogue.getCardById(c)));
		return deck;
	}

	public List<HeroClass> getHeroClassChoices() {
		return heroClassChoices;
	}

	public void setHeroClassChoices(List<HeroClass> heroClassChoices) {
		this.heroClassChoices = heroClassChoices;
	}

	public List<String> getCurrentCardChoices() {
		return currentCardChoices;
	}

	public void setCurrentCardChoices(List<String> currentCardChoices) {
		this.currentCardChoices = currentCardChoices;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public void setHeroClass(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	public DraftStatus getStatus() {
		return status;
	}

	public void setStatus(DraftStatus status) {
		this.status = status;
	}

	public List<String> getSelectedCards() {
		return selectedCards;
	}

	public void setSelectedCards(List<String> selectedCards) {
		this.selectedCards = selectedCards;
	}

	public int getCardsRemaining() {
		return cardsRemaining;
	}

	public void setCardsRemaining(int cardsRemaining) {
		this.cardsRemaining = cardsRemaining;
	}

	public int getDraftIndex() {
		return draftIndex;
	}

	public void setDraftIndex(int draftIndex) {
		this.draftIndex = draftIndex;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public int getLosses() {
		return losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public String getDeckId() {
		return deckId;
	}

	public void setDeckId(String deckId) {
		this.deckId = deckId;
	}
}
