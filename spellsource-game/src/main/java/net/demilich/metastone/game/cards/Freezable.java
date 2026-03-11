package net.demilich.metastone.game.cards;

/**
 * An object that can be frozen to become read-only. Once frozen, any mutation attempt throws
 * {@link UnsupportedOperationException}.
 * <p>
 * Used to protect shared catalogue card descriptors from accidental mutation.
 */
public interface Freezable {
	/**
	 * Freezes this object, making it read-only. Subclasses should recursively freeze nested {@link Freezable} children.
	 */
	void freeze();

	/**
	 * Returns {@code true} if this object is frozen (read-only).
	 */
	boolean isReadOnly();
}
