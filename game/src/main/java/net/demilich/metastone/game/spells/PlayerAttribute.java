package net.demilich.metastone.game.spells;

/**
 * Indicates a measurement about a player to use with {@link net.demilich.metastone.game.spells.desc.valueprovider.PlayerAttributeValueProvider}.
 * <p>
 * For <b>example,</b> to retrieve the amount of mana a player has:
 * <pre>
 *   {
 *     "class": "PlayerAttributeValueProvider",
 *     "playerAttribute": "MANA"
 *   }
 * </pre>
 */
public enum PlayerAttribute {
	/**
	 * The current amount of mana the player has.
	 */
	MANA,
	/**
	 * The number of mana crystals the player has.
	 */
	MAX_MANA,
	/**
	 * The number of cards in the player's hand.
	 */
	HAND_COUNT,
	/**
	 * The number of times the player's hero power has been used this turn.
	 */
	HERO_POWER_USED,
	/**
	 * The number of cards in the player's deck.
	 */
	DECK_COUNT,
	/**
	 * The last amount of mana spent on a card by the player.
	 */
	LAST_MANA_COST,
	/**
	 * The number of secrets the player has.
	 */
	SECRET_COUNT,
	/**
	 * The number of spells the player has cast this game.
	 */
	SPELLS_CAST,
	/**
	 * The number of mana crystals the player has overloaded this game.
	 */
	OVERLOADED_THIS_GAME,
	/**
	 * The number of cards the player has discarded this game.
	 */
	CARDS_DISCARDED,
	/**
	 * The number of cards the player has {@link net.demilich.metastone.game.cards.Attribute#INVOKED} this game.
	 */
	INVOKED_CARDS,
	/**
	 * The number of {@link SupremacySpell} effects this player has done this game.
	 */
	SUPREMACIES_THIS_GAME,
	/**
	 * Indicates the amount of damage the player's hero has taken this turn.
	 */
	DAMAGE_THIS_TURN,
	/**
	 * The current amount of overloaded/locked mana the player has.
	 */
	LOCKED_MANA,
	/**
	 * The total damage dealt by the player's hero power this game
	 */
	HERO_POWER_DAMAGE_DEALT,
	/**
	 * The total number of cards the player has drawn
	 */
	CARDS_DRAWN,
	/**
	 * The total healing done by friendly sources
	 */
	HEALING_DONE,
	/**
	 * The total amount of armor lost this game
	 */
	ARMOR_LOST
}
