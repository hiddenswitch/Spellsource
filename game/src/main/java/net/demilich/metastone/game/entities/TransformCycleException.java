package net.demilich.metastone.game.entities;

import com.google.common.base.MoreObjects;

/**
 * Indicates that an infinite loop occurred trying to follow the link from an entity that was removed from play due to
 * being transformed towards the entity it was eventually transformed into.
 */
public class TransformCycleException extends RuntimeException {
	private final Entity source;

	public TransformCycleException(Entity source) {
		super("transform cycle");
		this.source = source;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("source", source)
				.toString();
	}
}
