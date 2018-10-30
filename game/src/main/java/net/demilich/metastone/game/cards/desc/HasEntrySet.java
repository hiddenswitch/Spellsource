package net.demilich.metastone.game.cards.desc;

import java.util.Map;
import java.util.Set;

/**
 * Indicates the implementing class can be iterated through using its {@link #entrySet()} method.
 *
 * @param <T> A enum representing the keys of this instance
 * @param <V> A value type, which can be Object
 */
public interface HasEntrySet<T extends Enum<T>, V> {
	Set<Map.Entry<T, V>> entrySet();
}
