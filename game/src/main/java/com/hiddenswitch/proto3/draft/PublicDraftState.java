package com.hiddenswitch.proto3.draft;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bberman on 12/14/16.
 */
public class PublicDraftState {
	private List<HeroClass> heroClassChoices;
	private List<String> currentCardChoices;
	private HeroClass heroClass;
	private DraftStatus status;
	private List<String> selectedCards;
	private int cardsRemaining;
	private int draftIndex;

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
}
