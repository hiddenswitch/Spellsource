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
	ALWAYS_ATTACK_ENEMY_HERO,
	/**
	 * A player should be able to end their turn even if
	 * attacking the enemy hero is a valid action
	 */
	CANNOT_END_TURN_IF_ATTACKING_ENEMY_HERO_IS_VALID,

	/**
	 * A player should not be able to cast buff spells on
	 * enemy minions
	 */
	CANNOT_BUFF_ENEMY_MINIONS,

	/**
	 * A player should not be able to heal enemy minions
	 */
	CANNOT_HEAL_ENEMY_ENTITIES,
	/**
	 * A player should not be able to heal eneti
	 */
	CANNOT_HEAL_FULL_HEALTH_ENTITIES,

	/**
	 * Cards that cannot target enemy entities (spells, battlecries, etc.)
	 */
	SOME_CARDS_CANNOT_TARGET_ENEMY_ENTITIES,

	/**
	 * Cards that cannot target player entities (spells, battlecries, etc.)
	 */
	SOME_CARDS_CANNOT_TARGET_OWN_ENTITIES,

	/**
	 * Damage spells that cannot target minions below a certain HP
	 */
	SOME_DAMAGE_SPELLS_CANNOT_TARGET_WEAK_MINIONS
}
