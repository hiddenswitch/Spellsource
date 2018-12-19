package net.demilich.metastone.game.targeting;

import net.demilich.metastone.game.targeting.EntityReference;

/**
 * This exception is thrown when a given target is not found and the game requires one to be found. It is considered a
 * subclass of {@link NullPointerException} to stay consistent with the spirit of the Java exception hierarchy.
 */
public class TargetNotFoundException extends NullPointerException {
	private final EntityReference reference;

	/**
	 * Creates a new instance of this exception.
	 * @param message
	 * @param reference
	 */
	public TargetNotFoundException(String message, EntityReference reference) {
		super(message);
		this.reference = reference;
	}

	/**
	 * The reference that was failed to be found in the game state.
	 *
	 * @return The reference. It may be a {@link EntityReference#isTargetGroup()} {@code = true} reference (i.e., a "group
	 * 		reference").
	 */
	public EntityReference getReference() {
		return reference;
	}
}
