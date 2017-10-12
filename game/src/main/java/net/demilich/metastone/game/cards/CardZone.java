package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.targeting.Zones;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;

/**
 * This class is a {@link CardList} that represents the {@link Zones#HAND} and {@link Zones#DECK}. It is implemented
 * as an {@link EntityZone} because it represents an in-game zone; i.e., an entity can only be in one zone at once.
 *
 * @see CardArrayList for a {@link List} that implements {@link CardList} that does not represent a zone in the game
 * and can be used for all sorts of game logic that needs to deal with lists of cards.
 */
public class CardZone extends EntityZone<Card> implements CardList {
	public CardZone(int player, Zones zone) {
		super(player, zone);
	}

	public CardZone(int player, Zones zone, CardList cardsCopy) {
		super(player, zone);
		addAll(cardsCopy);
	}

	@Override
	public CardList addCard(Card card) {
		super.add(card);
		return this;
	}

	@Override
	public CardList addAll(CardList cardList) {
		for (Card card : cardList) {
			super.add(card.clone());
		}
		return this;
	}

	@Override
	public void addRandomly(Card card) {
		int index = RandomUtils.nextInt(0, size());
		add(index, card);
	}

	/**
	 * Creates a new zone and adds a clone of all the cards to it. Skips checks on the zone to prevent entities from
	 * being in two places at once.
	 * @return The cloned {@link CardZone}.
	 */
	@Override
	public CardZone clone() {
		// Clone all the cards too
		CardZone zone = new CardZone(getPlayer(), getZone());
		for (Card e : this) {
			zone.uncheckedAdd(zone.size(), e.clone());
		}
		return zone;
	}

	@Override
	public boolean contains(Card card) {
		return super.contains(card);
	}

	@Override
	public int getCount() {
		return super.size();
	}

	@Override
	public Card peekFirst() {
		return get(0);
	}

	@Override
	public boolean remove(Card card) {
		return super.remove(card);
	}

	@Override
	@Deprecated
	public void removeAll() throws Exception {
		super.clear();
	}

	/**
	 * Removes the first card in this instance and sets its location to {@link EntityLocation#UNASSIGNED}, so that
	 * it can be added to another zone.
	 * @return The card that was removed.
	 */
	@Override
	public Card removeFirst() {
		return super.remove(0);
	}

	/**
	 * Replaces a card in this zone, setting the old card's {@link net.demilich.metastone.game.entities.Entity#entityLocation}
	 * to {@link EntityLocation#UNASSIGNED} so that it can be added to another zone.
	 * @param oldCard The card to find and replace.
	 * @param newCard The new card to replace it with.
	 * @return {@code true} if the old card was found and replaced. {@code false} if the old card was not found and
	 * not replaced. (You'll never find the old card and not replace it).
	 */
	@Override
	public boolean replace(Card oldCard, Card newCard) {
		int index = indexOf(oldCard);
		if (index != -1) {
			oldCard.resetEntityLocations();
			set(index, newCard);
			return true;
		}
		return false;
	}

	@Override
	public void shuffle() {
		shuffle(new Random());
	}

	@Override
	public void shuffle(Random random) {
		Collections.shuffle(internal, random);
		for (int i = 0; i < internal.size(); i++) {
			internal.get(i).setEntityLocation(new EntityLocation(getZone(), getPlayer(), i));
		}
	}

	@Override
	public void sortByManaCost() {
		Comparator<Card> manaComparator = (card1, card2) -> {
			Integer manaCost1 = card1.getBaseManaCost();
			Integer manaCost2 = card2.getBaseManaCost();
			return manaCost1.compareTo(manaCost2);
		};

		sort(manaComparator);
	}

	@Override
	public void sortByName() {
		sort((card1, card2) -> card1.getName().compareTo(card2.getName()));
	}

	@Override
	public List<Card> toList() {
		return new ArrayList<>(this);
	}
}
