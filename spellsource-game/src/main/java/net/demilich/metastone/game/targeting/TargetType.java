package net.demilich.metastone.game.targeting;

/**
 * The possible values for an enchantment's "hostTargetType".
 * <p>
 * See {@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#HOST_TARGET_TYPE} for more about host
 * target types.
 */
public enum TargetType {
	/**
	 * Indicates that the trigger doesn't fire if the {@link EntityReference#EVENT_TARGET} is this trigger's host.
	 */
	IGNORE_AS_TARGET,
	/**
	 * Indicates that the trigger doesn't fire if the {@link EntityReference#EVENT_SOURCE} is this trigger's host.
	 */
	IGNORE_AS_SOURCE,
	/**
	 * Indicates that the trigger doesn't fire if the {@link EntityReference#EVENT_SOURCE} is this trigger's host's source
	 * card.
	 */
	IGNORE_AS_SOURCE_CARD,
	/**
	 * Indicates that the trigger doesn't fire if the {@link EntityReference#EVENT_TARGET} is this trigger's host's source
	 * card.
	 */
	IGNORE_AS_TARGET_CARD,
	/**
	 * Indicates that the trigger fires only if the {@link EntityReference#EVENT_TARGET} is this trigger's host.
	 */
	IGNORE_OTHER_TARGETS,
	/**
	 * Indicates that the trigger fires only if the {@link EntityReference#EVENT_TARGET} is this trigger's host's source
	 * card.
	 */
	IGNORE_OTHER_TARGET_CARDS,
	/**
	 * Indicates that the trigger fires only if the {@link EntityReference#EVENT_SOURCE} is this trigger's host.
	 */
	IGNORE_OTHER_SOURCES
}
