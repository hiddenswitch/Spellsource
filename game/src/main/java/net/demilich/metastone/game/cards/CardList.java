package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.IdFactory;
import net.demilich.metastone.game.targeting.Zones;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;


/**
 * An interface describing common actions for a collection of cards. This abstracts away the difference between an
 * {@link net.demilich.metastone.game.entities.EntityZone}, which enforces that its containing {@link net.demilich.metastone.game.entities.Entity}
 * objects can only be in one {@link net.demilich.metastone.game.entities.EntityZone} at a time, versus a plain {@link CardArrayList},
 * which is just an array of cards that various pieces of logic might want to {@link #shuffle()} or {@link #addRandomly(Card)} into.
 * <p>
 * Use {@link CardZone} for the {@link Zones#HAND}, {@link Zones#DECK} and {@link Zones#DISCOVER} zones—when a card should
 * only be in one place at a time. Use a {@link CardArrayList} for situations where you need to e.g., get a list of cards from
 * an {@link EntityFilter}, shuffle them, and choose one from the top.
 *
 * @see CardZone for the entity zone that implements this interface.
 * @see CardArrayList for a {@link List} implementation of this interface.
 */
public interface CardList extends Iterable<Card> {
	/**
	 * Adds the card fluently.
	 *
	 * @param card The card
	 * @return This instance.
	 */
	CardList addCard(Card card);

	/**
	 * Adds all the cards from the given list.
	 *
	 * @param cardList The cards to add.
	 * @return This instance.
	 */
	CardList addAll(CardList cardList);

	/**
	 * Used for shuffling a card into this list. Problematic because it uses ThreadLocalRandom instead of a random
	 * seed from a {@link net.demilich.metastone.game.logic.GameLogic} instance.
	 *
	 * @param card The card to shuffle randomly into this list.
	 */
	@Deprecated
	void addRandomly(Card card);

	/**
	 * Calls {@link Card#clone()} on every card in this list and returns a new copy of this list.
	 *
	 * @return A clone of the list and all its contents. Generally only helpful for "immutable views" of this list.
	 * @see #getCopy() for a more game logic useful version of this method.
	 */
	CardList clone();

	/**
	 * Checks if the list has the specific reference to a card. Does not use the card's {@link net.demilich.metastone.game.entities.Entity#id}
	 * or its {@link Card#cardId}, which may be more helpful.
	 *
	 * @param card The card instance to check.
	 * @return {@code true} if the specific instance is inside this list.
	 * @see #containsCard(Card) for a more game logic useful version of this method.
	 */
	boolean contains(Card card);

	/**
	 * Checks if there is a card in this list whose {@link Card#cardId} matches the specified instance of a card.
	 *
	 * @param card The card instance to compare.
	 * @return {@code true} if the there is a card with a matching {@link Card#cardId}.
	 */
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

	/**
	 * Gets a card at the specified index.
	 *
	 * @param index The index.
	 * @return The card.
	 */
	Card get(int index);

	/**
	 * Gets the size of this list.
	 *
	 * @return The list size.
	 */
	int getCount();

	/**
	 * Gets a random {@link Card} in this instance. Uses Apache's {@link RandomUtils} internally, though it should
	 * probably use a random value provider from a {@link net.demilich.metastone.game.logic.GameLogic} instance.
	 * <p>
	 * If you plan to use a copy of this card, make sure to call {@link Card#getCopy()} and assign its ID to {@link IdFactory#generateId()}.
	 * and owner to the appropriate owner.
	 *
	 * @return A randomly selected card (not cloned or copied).
	 */
	default Card getRandom() {
		if (isEmpty()) {
			return null;
		}
		return get(RandomUtils.nextInt(0, getCount()));
	}

	/**
	 * Gets a random {@link Card} in this instance of the specified type.
	 *
	 * @param cardType The {@link CardType} to filter with.
	 * @return A card, or {@code null} if none is found.
	 * @see #getRandom() for a complete usage description.
	 */
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

	/**
	 * Checks if this instance contains a {@link Card} of the specified type.
	 *
	 * @param cardType The type to check.
	 * @return {@code true} if this instance contains a card of the specified type.
	 */
	default boolean hasCardOfType(CardType cardType) {
		for (Card card : this) {
			if (card.getCardType().isCardType(cardType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the instance is empty.
	 *
	 * @return {@code true} if the list is empty.
	 */
	boolean isEmpty();

	/**
	 * Provides support for Java language features that require an {@link Iterator}
	 *
	 * @return The iterator.
	 */
	Iterator<Card> iterator();

	/**
	 * Gets the first card in this instance.
	 *
	 * @return The first card in this instance.
	 * @throws ArrayIndexOutOfBoundsException if the list is empty.
	 */
	Card peekFirst() throws ArrayIndexOutOfBoundsException;

	/**
	 * Removes the specified card instance by reference.
	 * @param card The card to remove.
	 * @return {@link true} if the card was removed.
	 */
	boolean remove(Card card);

	/**
	 * Removes all the cards from this instance.
	 */
	void removeAll();

	/**
	 * Removes the first card. Implements {@link net.demilich.metastone.game.spells.PutRandomSecretIntoPlaySpell}, used
	 * by 3 Hearthstone cards.
	 * @return The card that is now removed.
	 */
	Card removeFirst();

	/**
	 * Replaces a card by index.
	 * @param oldCard The card to find and replace.
	 * @param newCard The new card to replace it with.
	 * @return {@code true} if the replacement was successful.
	 */
	boolean replace(Card oldCard, Card newCard);

	/**
	 * Shuffles the instance.
	 */
	void shuffle();

	/**
	 * Shuffles the instance with the given random number generator.
	 * @param random A {@link Random} instance.
	 */
	void shuffle(Random random);

	/**
	 * Sorts the cards in this list by their {@link Card#getBaseManaCost()}. Typically not used in gameplay.
	 */
	void sortByManaCost();

	/**
	 * Sorts the cards by name.
	 */
	void sortByName();

	/**
	 * Gets a {@link List} that references the contents of this instance.
	 * @return A {@link List} whose contents are the same objects as this instance.
	 */
	List<Card> toList();

	/**
	 * Copies all the cards in this list and returns a new {@link CardList} (possibly of a different implementation)
	 * containing those copies.
	 * @return A copied list of cards.
	 * @see Card#getCopy() for more about the difference between a copy and a clone.
	 */
	default CardList getCopy() {
		CardList copiedCards = new CardArrayList();
		toList().stream().map(Card::getCopy).forEach(copiedCards::addCard);
		return copiedCards;
	}
}