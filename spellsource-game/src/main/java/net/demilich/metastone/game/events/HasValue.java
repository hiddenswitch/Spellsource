package net.demilich.metastone.game.events;

/**
 * Indicates an event has a value, typically the damage dealt or mana changed, etc.
 * <p>
 * Implemented by the {@link ValueEvent}.
 */
public interface HasValue {
	/**
	 * Gets the value associated with the event.
	 *
	 * @return
	 */
	int getValue();
}
