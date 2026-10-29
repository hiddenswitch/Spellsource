package net.demilich.metastone.game.cards.desc.compiler;

import net.demilich.metastone.game.entities.Entity;

import java.io.Serializable;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Arena-backed {@code EntityLookup} template woven into {@code net.demilich.metastone.game.entities.EntityLookup}
 * by {@link SpellsourceClassfileWeaver}.
 * <p>
 * Stores entities in the unified {@link GameStateArena#entities} table with per-entity Copy-on-Write sharing across
 * {@link net.demilich.metastone.game.GameContext#clone()}.
 */
public final class EntityLookupTemplate implements Serializable, Cloneable, Iterable<Entity> {
	public GameStateArena arena;
	public int player;

	public EntityLookupTemplate(int initialCapacity) {
		this.arena = new GameStateArena(Math.max(initialCapacity, 64));
		this.player = -1;
	}

	public EntityLookupTemplate(GameStateArena arena, int player) {
		this.arena = arena;
		this.player = player;
	}

	public EntityLookupTemplate(int player, GameStateArena arena) {
		this.arena = arena;
		this.player = player;
	}

	public void put(int id, Entity entity) {
		if (id < 0) {
			return;
		}
		arena.putEntityDirect(id, entity);
	}

	public Entity get(int id) {
		if (player < 0) {
			return arena.getEntityById(id);
		}
		return arena.getEntityByIdForPlayer(player, id);
	}

	public void remove(int id) {
		if (id < 0) {
			return;
		}
		arena.removeEntityDirect(id);
	}

	public int size() {
		return arena.countEntitiesForPlayer(player);
	}

	public int capacity() {
		return arena.entities.length;
	}

	public Stream<Entity> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	@Override
	public Iterator<Entity> iterator() {
		return arena.entityIteratorForPlayer(player);
	}

	@Override
	public EntityLookupTemplate clone() {
		return new EntityLookupTemplate(arena.cloneArena(true), player);
	}
}
