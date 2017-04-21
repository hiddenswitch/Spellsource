package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.EntityZone;
import net.demilich.metastone.game.targeting.PlayerZones;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;
import java.util.function.Predicate;

/**
 * Created by bberman on 4/16/17.
 */
public class CardZone extends EntityZone<Card> implements CardCollection {
	public CardZone(int player, PlayerZones zone) {
		super(player, zone);
	}

	public CardZone(int player, PlayerZones zone, CardCollection cardsCopy) {
		super(player, zone);
		addAll(cardsCopy);
	}

	@Override
	public CardCollection addCard(Card card) {
		super.add(card);
		return this;
	}

	@Override
	public CardCollection addAll(CardCollection cardCollection) {
		for (Card card : cardCollection) {
			super.add(card.clone());
		}
		return this;
	}

	@Override
	public void addRandomly(Card card) {
		int index = RandomUtils.nextInt(0, size());
		add(index, card);
	}

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
	public void removeAll() {
		super.clear();
	}

	@Override
	public void removeAll(Predicate<Card> filter) {
		super.removeIf(filter);
	}

	@Override
	public Card removeFirst() {
		return super.remove(0);
	}

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
			internal.get(i).pushEntityLocation(new EntityLocation(getZone(), getPlayer(), i));
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
