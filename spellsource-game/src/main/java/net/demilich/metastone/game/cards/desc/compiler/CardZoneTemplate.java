package net.demilich.metastone.game.cards.desc.compiler;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.logic.XORShiftRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 * Arena-backed {@code CardZone} template woven into {@code net.demilich.metastone.game.cards.CardZone}
 * by {@link SpellsourceClassfileWeaver}.
 * <p>
 * Provides zero-unshare read-only fast paths for {@link #containsCard(String)}, {@link #hasCardOfType(CardType)},
 * and {@link #getCopy()} directly on {@link GameStateArena#zoneArrays}.
 */
public final class CardZoneTemplate extends EntityZoneTemplate<Card> implements CardList {

	public CardZoneTemplate(int player, Zones zone, EntityLookupTemplate lookup) {
		super(player, zone, lookup);
	}

	public CardZoneTemplate(int player, Zones zone, EntityLookupTemplate lookup, GameStateArena arena) {
		super(player, zone, lookup, arena);
	}

	public CardZoneTemplate(int player, Zones zone, CardList cardsCopy, EntityLookupTemplate lookup) {
		super(player, zone, lookup);
		arena.zoneClear(slot, player, lookup != null);
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
	public CardZoneTemplate clone() {
		CardZoneTemplate cloneZone = new CardZoneTemplate(getPlayer(), getZone(), (EntityLookupTemplate) null);
		int size = arena.zoneSizes[slot];
		Entity[] srcArr = arena.zoneArrays[slot];
		for (int i = 0; i < size; i++) {
			Card cloned = ((Card) srcArr[i]).clone();
			cloneZone.arena.zoneAdd(cloneZone.slot, getPlayer(), getZone(), i, cloned, false);
		}
		return cloneZone;
	}

	@Override
	public boolean contains(Card card) {
		return super.contains(card);
	}

	@Override
	public boolean containsCard(String cardId) {
		if (cardId == null) {
			return false;
		}
		int size = arena.zoneSizes[slot];
		Entity[] arr = arena.zoneArrays[slot];
		for (int i = 0; i < size; i++) {
			Card c = (Card) arr[i];
			if (cardId.equals(c.getCardId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasCardOfType(CardType cardType) {
		int size = arena.zoneSizes[slot];
		Entity[] arr = arena.zoneArrays[slot];
		for (int i = 0; i < size; i++) {
			Card c = (Card) arr[i];
			if (GameLogic.isCardType(c.getCardType(), cardType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public CardList getCopy() {
		int n = arena.zoneSizes[slot];
		CardList copiedCards = new CardArrayList(n);
		Entity[] arr = arena.zoneArrays[slot];
		for (int i = 0; i < n; i++) {
			copiedCards.addCard(((Card) arr[i]).getCopy());
		}
		return copiedCards;
	}

	@Override
	public int getCount() {
		return arena.zoneSizes[slot];
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
	public CardList shuffle(XORShiftRandom random) {
		random.shuffle((CardList) this);
		return this;
	}

	public void shuffleInternal(Random random) {
		arena.zoneShuffle(slot, player, zone, random);
	}

	@Override
	public List<Card> toList() {
		return new ArrayList<>(this);
	}

	@Override
	public Stream<Card> stream() {
		return super.stream();
	}
}
