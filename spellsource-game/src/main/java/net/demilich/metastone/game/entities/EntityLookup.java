package net.demilich.metastone.game.entities;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A fast int-keyed lookup table for entities, backed by a plain array. Entity IDs are non-negative dense integers, so
 * direct array indexing is O(1) with no hashing overhead.
 */
public final class EntityLookup implements Serializable, Cloneable, Iterable<Entity> {
	private Entity[] table;
	private int size;

	public EntityLookup(int initialCapacity) {
		this.table = new Entity[Math.max(initialCapacity, 16)];
	}

	public void put(int id, Entity entity) {
		if (id < 0) {
			return;
		}
		ensureCapacity(id);
		if (table[id] == null) {
			size++;
		}
		table[id] = entity;
	}

	public Entity get(int id) {
		if (id < 0 || id >= table.length) {
			return null;
		}
		return table[id];
	}

	public void remove(int id) {
		if (id < 0 || id >= table.length || table[id] == null) {
			return;
		}
		table[id] = null;
		size--;
	}

	public int size() {
		return size;
	}

	/**
	 * Returns a stream of all non-null entities in this lookup. Replaces the old {@code Map.values().stream()} pattern.
	 */
	public Stream<Entity> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	@Override
	public Iterator<Entity> iterator() {
		return new Iterator<>() {
			private int nextIndex = findNext(0);

			@Override
			public boolean hasNext() {
				return nextIndex >= 0;
			}

			@Override
			public Entity next() {
				if (nextIndex < 0) {
					throw new NoSuchElementException();
				}
				Entity e = table[nextIndex];
				nextIndex = findNext(nextIndex + 1);
				return e;
			}
		};
	}

	private int findNext(int fromIndex) {
		for (int i = fromIndex; i < table.length; i++) {
			if (table[i] != null) {
				return i;
			}
		}
		return -1;
	}

	private void ensureCapacity(int id) {
		if (id >= table.length) {
			int newLength = Math.max(table.length * 2, id + 1);
			table = Arrays.copyOf(table, newLength);
		}
	}

	@Override
	public EntityLookup clone() {
		try {
			EntityLookup clone = (EntityLookup) super.clone();
			clone.table = table.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}
}
