package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;

import java.io.Serializable;
import java.util.*;

/**
 * The sets that are available to build decks from and generate cards from.
 * <p>
 *
 * @see GameContext#getDeckFormat() for the property on the game context where the deck format is set
 * @see CardCatalogue#getSmallestSupersetFormat(GameDeck...) to determine the smallest format that can be used for the
 * specified decks
 */
public class DeckFormat implements Serializable, Cloneable {
	private String name = "";
	private Set<String> sets;
	private String[] secondPlayerBonusCards = new String[0];
	private ConditionDesc validDeckCondition;


	public DeckFormat() {
		setSets(new HashSet<>());
	}

	public DeckFormat addSet(String cardSet) {
		getSets().add(cardSet);
		return this;
	}

	public boolean isInFormat(Card card) {
		if (getSets().contains(card.getCardSet())) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isInFormat(String set) {
		return set != null && getSets().contains(set);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DeckFormat withName(String name) {
		this.name = name;
		return this;
	}

	public DeckFormat withCardSets(String... cardSets) {
		for (String cardSet : cardSets) {
			addSet(cardSet);
		}
		return this;
	}

	public DeckFormat withCardSets(Iterable<String> cardSets) {
		for (String cardSet : cardSets) {
			addSet(cardSet);
		}
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DeckFormat that = (DeckFormat) o;
		return Objects.equals(name, that.name) && Objects.equals(sets, that.sets) && Arrays.equals(secondPlayerBonusCards, that.secondPlayerBonusCards) && Objects.equals(validDeckCondition, that.validDeckCondition);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(name, sets, validDeckCondition);
		result = 31 * result + Arrays.hashCode(secondPlayerBonusCards);
		return result;
	}

	public String[] getSecondPlayerBonusCards() {
		return secondPlayerBonusCards;
	}

	public DeckFormat setSecondPlayerBonusCards(String[] secondPlayerBonusCards) {
		this.secondPlayerBonusCards = secondPlayerBonusCards;
		return this;
	}

	public ConditionDesc getValidDeckCondition() {
		return validDeckCondition;
	}

	public DeckFormat setValidDeckCondition(ConditionDesc validDeckCondition) {
		this.validDeckCondition = validDeckCondition;
		return this;
	}

	public Set<String> getSets() {
		return sets;
	}

	public void setSets(Set<String> sets) {
		this.sets = sets;
	}

	@Override
	public DeckFormat clone() {
		DeckFormat clone = null;
		try {
			clone = (DeckFormat) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
		clone.sets = new HashSet<>(sets);
		return clone;
	}
}

