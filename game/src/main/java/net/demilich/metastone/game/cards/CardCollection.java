package net.demilich.metastone.game.cards;

import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Created by bberman on 4/16/17.
 */
public interface CardCollection extends Iterable<Card> {
	CardCollection addCard(Card card);

	CardCollection addAll(CardCollection cardCollection);

	void addRandomly(Card card);

	CardCollection clone();

	boolean contains(Card card);

	default boolean containsCard(Card card) {
		if (card == null) {
			return false;
		}
		for (Card other : this) {
			if (other.getCardId().equals(card.getCardId())) {
				return true;
			}
		}
		return false;
	}

	Card get(int index);

	int getCount();

	default Card getRandom() {
		if (isEmpty()) {
			return null;
		}
		return get(RandomUtils.nextInt(0, getCount()));
	}

	default Card getRandomOfType(CardType cardType) {
		List<Card> relevantCards = new ArrayList<>();
		for (Card card : this) {
			if (card.getCardType().isCardType(cardType)) {
				relevantCards.add(card);
			}
		}
		if (relevantCards.isEmpty()) {
			return null;
		}
		return relevantCards.get(RandomUtils.nextInt(0, relevantCards.size()));
	}

	default boolean hasCardOfType(CardType cardType) {
		for (Card card : this) {
			if (card.getCardType().isCardType(cardType)) {
				return true;
			}
		}
		return false;
	}

	boolean isEmpty();

	Iterator<Card> iterator();

	Card peekFirst();

	boolean remove(Card card);

	void removeAll();

	void removeAll(Predicate<Card> filter);

	Card removeFirst();

	boolean replace(Card oldCard, Card newCard);

	void shuffle();

	void shuffle(Random random);

	void sortByManaCost();

	void sortByName();

	List<Card> toList();

	default CardCollection getCopy() {
		CardCollection copiedCards = new CardCollectionImpl();
		toList().stream().map(Card::getCopy).forEach(copiedCards::addCard);
		return copiedCards;
	};
}