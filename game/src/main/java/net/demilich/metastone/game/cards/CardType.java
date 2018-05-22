package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.heroes.Hero;
import net.demilich.metastone.game.entities.weapons.Weapon;

/**
 * The different types of cards.
 * <p>
 * {@link #CHOOSE_ONE} refers to choose one spells and hero powers, not choose one minions (which are still just {@link
 * #MINION} cards.)
 *
 * @see Card#play() for more about how the card type is used to affect the way a card is played.
 */
public enum CardType {
	/**
	 * A hero card can represent both the base heroes that are set at the beginning of the game and a playable hero card
	 * from the hand.
	 */
	HERO,
	/**
	 * A minion card summons a minion onto the battlefield.
	 */
	MINION,
	/**
	 * A spell card puts the text on the card into play as a one-time effect.
	 */
	SPELL,
	/**
	 * A weapon puts an {@link Actor} called a {@link Weapon} into play, which gives the {@link Hero} that weapon and its
	 * attack.
	 */
	WEAPON,
	/**
	 * A hero power behaves like a spell card that is permanently in your hand. It can only be used once per turn.
	 */
	HERO_POWER,
	/**
	 * A group card represents multiple cards that are referenced together, like the {@link
	 * net.demilich.metastone.game.spells.CastFromGroupSpell} mechanic.
	 */
	GROUP,
	/**
	 * A choose one card is a spell card that can assume the identity of two others. The base choose one card is put into
	 * your graveyard and returned to your hand by return hand effects. The chosen card is cast, and therefore is the card
	 * that interacts with spell casting effects and triggers.
	 */
	CHOOSE_ONE;

	/**
	 * Indicates whether or not the instance is of the specified card type.
	 * <p>
	 * Use this instead of direct comparisons to interpret a {@link #CHOOSE_ONE} card as a {@link #SPELL} card.
	 *
	 * @param cardType The card type to compare to.
	 * @return {@code true} if this instance is of the {@code cardType}.
	 */
	public boolean isCardType(CardType cardType) {
		if (this == CHOOSE_ONE && cardType == SPELL) {
			return true;
		} else if (this == cardType) {
			return true;
		}
		return false;
	}
}
