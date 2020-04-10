package net.demilich.metastone.game.spells.desc.trigger;

/**
 * Keys for enumerating an {@link EnchantmentDesc}.
 */
public enum EnchantmentDescArg {
	/**
	 * @see EnchantmentDesc#spell
	 */
	SPELL,
	/**
	 * @see EnchantmentDesc#oneTurn
	 */
	ONE_TURN,
	/**
	 * @see EnchantmentDesc#persistentOwner
	 */
	PERSISTENT_OWNER,
	/**
	 * @see EnchantmentDesc#maxFires
	 */
	MAX_FIRES,
	/**
	 * @see EnchantmentDesc#countUntilCast
	 */
	COUNT_UNTIL_CAST,
	/**
	 * @see EnchantmentDesc#countByValue
	 */
	COUNT_BY_VALUE,
	/**
	 * @see EnchantmentDesc#eventTrigger
	 */
	EVENT_TRIGGER,
	/**
	 * @see EnchantmentDesc#maxFiresPerSequence
	 */
	MAX_FIRES_PER_SEQUENCE,
	/**
	 * @see EnchantmentDesc#expirationTriggers
	 */
	EXPIRATION_TRIGGERS,
	/**
	 * @see EnchantmentDesc#activationTriggers
	 */
	ACTIVATION_TRIGGERS
}
