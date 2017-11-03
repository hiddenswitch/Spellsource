package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.spells.CastFromGroupSpell;

/**
 * The different types of cards.
 * <p>
 * {@link #CHOOSE_ONE} refers to choose one spells and hero powers, not choose one minions (which are still just {@link
 * #MINION} cards.)
 */
public enum CardType {
	HERO,
	MINION,
	SPELL,
	WEAPON,
	HERO_POWER,
	/**
	 * A group card represents multiple cards that are referenced together, like the {@link CastFromGroupSpell}
	 * mechanic.
	 */
	GROUP,
	CHOOSE_ONE;

	public boolean isCardType(CardType cardType) {
		if (this == CHOOSE_ONE && cardType == SPELL) {
			return true;
		} else if (this == cardType) {
			return true;
		}
		return false;
	}
}
