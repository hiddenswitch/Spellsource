package com.hiddenswitch.deckgeneration;

/**
 * A list of decision types that can be specified for the
 * player and opponent AI
 */

public enum DecisionType {
	/**
	 * The cards that the player should keep at the beginning of the game
	 */
	KEEP_CARDS_ON_MULLIGAN,
	/**
	 * The minions that should be allowed to attack the enemy hero
	 */
	SOME_MINIONS_DO_NOT_ATTACK_ENEMY_HERO,
	/**
	 * The minions that should be allowed to attack an enemy minion
	 */
	SOME_MINIONS_DO_NOT_ATTACK_ENEMY_MINION,
	/**
	 * No minions should ever attack an opponent's minion
	 */
	ALWAYS_ATTACK_ENEMY_HERO
}
