package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.Zones;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * An interface describing common actions for a collection of cards. This abstracts away the difference between an
 * {@link net.demilich.metastone.game.entities.EntityZone}, which enforces that its containing {@link
 * net.demilich.metastone.game.entities.Entity} objects can only be in one {@link net.demilich.metastone.game.entities.EntityZone}
 * at a time, versus a plain {@link CardArrayList}, which is just an array of cards that various pieces of logic might
 * want to {@link #addCard(Card)} to.
 * <p>
 * Use {@link CardZone} for the {@link Zones#HAND}, {@link Zones#DECK} and {@link Zones#DISCOVER} zones--when a card
 * should only be in one place at a time. Use a {@link CardArrayList} for situations where you need to e.g., get a list
 * of cards from an {@link EntityFilter}, shuffle them, and choose one from the top.
 *
 * @see CardZone for the entity zone that implements this interface.
 * @see CardArrayList for a {@link List} implementation of this interface.
 */
public interface CardList extends Iterable<Card>, List<Card>, Serializable {
	/**
	 * Adds the card fluently.
	 *
	 * @param card The card
	 * @return This instance.
	 */
	CardList addCard(@NotNull Card card);

	default CardList addCard(String cardId) {
		Card cardById = CardCatalogue.getCardById(cardId);
		if (cardById == null) {
			throw new NullPointerException(cardId);
		}
		addCard(cardById);
		return this;
	}

	/**
	 * Adds all the cards from the given list.
	 *
	 * @param cardList The cards to add.
	 * @return This instance.
	 */
	CardList addAll(CardList cardList);

	/**
	 * Calls {@link Card#clone()} on every card in this list and returns a new copy of this list.
	 *
	 * @return A clone of the list and all its contents. Generally only helpful for "immutable views" of this list.
	 * @see #getCopy() for a more game logic useful version of this method.
	 */
	CardList clone();

	/**
	 * Checks if the list has the specific reference to a card. Does not use the card's {@link
	 * net.demilich.metastone.game.entities.Entity#id} or its {@link Card#getCardId()}, which may be more helpful.
	 *
	 * @param card The card instance to check.
	 * @return {@code true} if the specific instance is inside this list.
	 * @see #containsCard(Card) for a more game logic useful version of this method.
	 */
	boolean contains(Card card);

	/**
	 * Checks if there is a card in this list whose {@link Card#getCardId()} matches the specified instance of a card.
	 *
	 * @param card The card instance to compare.
	 * @return {@code true} if the there is a card with a matching {@link Card#getCardId()}.
	 */
	default boolean containsCard(Card card) {
		if (card == null) {
			return false;
		}
		return containsCard(card.getCardId());
	}

	/**
	 * Checks if there is a card in this list whose {@link Card#getCardId()} matches the specified card ID.
	 *
	 * @param cardId The card ID
	 * @return {@code true} if the there is a card with a matching {@link Card#getCardId()}.
	 */
	default boolean containsCard(String cardId) {
		if (cardId == null) {
			return false;
		}
		for (Card other : this) {
			if (other.getCardId().equals(cardId)) {
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
	 *
	 * @param card The card to remove.
	 * @return {@code true} if the card was removed.
	 */
	boolean remove(Card card);

	/**
	 * Removes all the cards from this instance.
	 */
	void removeAll() throws Exception;

	/**
	 * Removes the first card. Implements {@link net.demilich.metastone.game.spells.PutRandomSecretIntoPlaySpell}, used by
	 * 3 Hearthstone cards.
	 *
	 * @return The card that is now removed.
	 */
	Card removeFirst();

	/**
	 * Replaces a card by index.
	 *
	 * @param oldCard The card to find and replace.
	 * @param newCard The new card to replace it with.
	 * @return {@code true} if the replacement was successful.
	 */
	boolean replace(Card oldCard, Card newCard);

	/**
	 * Shuffles the instance with the given random number generator.
	 *
	 * @param random A {@link Random} instance.
	 */
	CardList shuffle(Random random);

	/**
	 * Gets a {@link List} that references the contents of this instance.
	 *
	 * @return A {@link List} whose contents are the same objects as this instance.
	 */
	List<Card> toList();

	/**
	 * Gets a {@link CardList} of this instance's cards filtered by {@code filter}
	 *
	 * @param filter A predicate.
	 * @return A new {@link CardList} filtered.
	 */
	default CardList filtered(Predicate<? super Card> filter) {
		CardList filteredCards = new CardArrayList();
		this.stream().filter(filter).forEach(filteredCards::addCard);
		return filteredCards;
	}

	/**
	 * Copies all the cards in this list and returns a new {@link CardList} (possibly of a different implementation)
	 * containing those copies.
	 *
	 * @return A copied list of cards.
	 * @see Card#getCopy() for more about the difference between a copy and a clone.
	 */
	default CardList getCopy() {
		CardList copiedCards = new CardArrayList();
		this.stream().map(Card::getCopy).forEach(copiedCards::addCard);
		return copiedCards;
	}

	/**
	 * Gets the {@link Stream} API representation of this card list.
	 *
	 * @return The backing list's {@link List#stream()}.
	 */
	default Stream<Card> stream() {
		return toList().stream();
	}

	/**
	 * Returns the last card in the card list. This is the top of the deck.
	 *
	 * @return The last card, or null if the {@link #size()} is {@code 0}.
	 */
	default Card peek() {
		if (size() == 0) {
			return null;
		}
		return get(size() - 1);
	}
}