package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An implementation of {@link CardList} for easy shuffling, choosing and uniqueness testing of lists of cards.
 *
 * @see CardCatalogue#query(DeckFormat)  for an example of using this class to return a list of cards from a
 * 		function. By using this class instead of a plain {@link List}, the calling code can e.g. easily {@link #shuffle()}
 * 		the results.
 * @see net.demilich.metastone.game.spells.DiscoverFilteredCardSpell for a more advanced example of this class.
 */
public class CardArrayList extends AbstractList<Card> implements Cloneable, Serializable, CardList {
	private static final long serialVersionUID = 1L;
	private List<Card> cards = new ArrayList<Card>();

	public CardArrayList() {
	}

	@Override
	public boolean add(Card card) {
		return cards.add(card);
	}

	@Override
	public Stream<Card> stream() {
		return cards.stream();
	}

	/**
	 * Creates this instance from an existing list of cards.
	 *
	 * @param cards The list of cards.
	 */
	public CardArrayList(Collection<? extends Card> cards) {
		this.cards = new ArrayList<>(cards);
	}

	@Override
	public CardList addCard(Card card) {
		cards.add(card);
		return this;
	}

	@Override
	public CardList addAll(CardList cardList) {
		cards.addAll(cardList);
		return this;
	}

	@Override
	public CardList clone() {
		CardList clone = new CardArrayList();
		for (Card card : cards) {
			clone.addCard(card.clone());
		}

		return clone;
	}

	@Override
	public Card set(int index, Card element) {
		return cards.set(index, element);
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

	@NotNull
	@Override
	public Iterator<Card> iterator() {
		return cards.iterator();
	}

	@Override
	public int size() {
		return cards.size();
	}

	@Override
	public Card peekFirst() {
		return cards.get(0);
	}

	@Override
	public boolean remove(Object o) {
		return cards.remove(o);
	}

	@Override
	public boolean remove(Card card) {
		return cards.remove(card);
	}

	@Override
	public Card remove(int index) {
		return cards.remove(index);
	}

	@Override
	public void removeAll() {
		cards.clear();
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

	@Deprecated
	public CardList shuffle() {
		Collections.shuffle(cards);
		return this;
	}

	@Override
	public CardList shuffle(XORShiftRandom random) {
		Collections.shuffle(cards, random);
		return this;
	}

	@Override
	public List<Card> toList() {
		return new ArrayList<>(cards);
	}

}
