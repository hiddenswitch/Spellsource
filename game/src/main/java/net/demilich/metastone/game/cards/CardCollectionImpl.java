package net.demilich.metastone.game.cards;

import org.apache.commons.lang3.RandomUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class CardCollectionImpl implements Cloneable, Serializable, CardCollection {
	private static final long serialVersionUID = 1L;

	private List<Card> cards = new ArrayList<Card>();

	public CardCollectionImpl() {
	}

	public CardCollectionImpl(List<Card> cards) {
		this.cards = new ArrayList<>(cards);
	}

	@Override
	public CardCollection addCard(Card card) {
		cards.add(card);
		return this;
	}

	@Override
	public CardCollection addAll(CardCollection cardCollection) {
		for (Card card : cardCollection) {
			cards.add(card.clone());
		}
		return this;
	}

	@Override
	public void addRandomly(Card card) {
		int index = ThreadLocalRandom.current().nextInt(cards.size() + 1);
		cards.add(index, card);
	}

	@Override
	public CardCollection clone() {
		CardCollection clone = new CardCollectionImpl();
		for (Card card : cards) {
			clone.addCard(card.clone());
		}

		return clone;
	}

	@Override
	public boolean contains(Card card) {
		return cards.contains(card);
	}

	@Override
	public Card get(int index) {
		return cards.get(index);
	}

	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public boolean isEmpty() {
		return cards.isEmpty();
	}

	@Override
	public Iterator<Card> iterator() {
		return cards.iterator();
	}

	@Override
	public Card peekFirst() {
		return cards.get(0);
	}

	@Override
	public boolean remove(Card card) {
		return cards.remove(card);
	}

	@Override
	public void removeAll() {
		cards.clear();
	}

	@Override
	public void removeAll(Predicate<Card> filter) {
		cards.removeIf(filter);
	}

	@Override
	public Card removeFirst() {
		return cards.remove(0);
	}

	@Override
	public boolean replace(Card oldCard, Card newCard) {
		int index = cards.indexOf(oldCard);
		if (index != -1) {
			cards.set(index, newCard);
			return true;
		}
		return false;
	}

	@Override
	public void shuffle() {
		Collections.shuffle(cards);
	}

	@Override
	public void shuffle(Random random) {
		Collections.shuffle(cards, random);
	}

	@Override
	public void sortByManaCost() {
		Comparator<Card> manaComparator = (card1, card2) -> {
			Integer manaCost1 = card1.getBaseManaCost();
			Integer manaCost2 = card2.getBaseManaCost();
			return manaCost1.compareTo(manaCost2);
		};

		cards.sort(manaComparator);
	}

	@Override
	public void sortByName() {
		cards.sort((card1, card2) -> card1.getName().compareTo(card2.getName()));
	}

	@Override
	public List<Card> toList() {
		return new ArrayList<>(cards);
	}

}
