package net.demilich.metastone.game.cards.desc.compiler;

import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.EntityZoneTable;
import net.demilich.metastone.game.targeting.IdFactory;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * Arena-backed {@code EntityZone} template woven into {@code net.demilich.metastone.game.entities.EntityZone}
 * by {@link SpellsourceClassfileWeaver}.
 * <p>
 * Stores zone elements inside {@link GameStateArena#zoneArrays} and {@link GameStateArena#zoneSizes} at
 * {@code slot = (player & 1) * 16 + zone.ordinal()}, with per-zone and per-entity Copy-on-Write sharing across
 * {@link net.demilich.metastone.game.GameContext#clone()}.
 */
public class EntityZoneTemplate<E extends Entity> extends AbstractList<E> implements
		List<E>, RandomAccess, Iterable<E>, Cloneable, Serializable, Comparable<EntityZoneTemplate<? extends Entity>> {

	public final Zones zone;
	public EntityLookupTemplate lookup;
	public int player = -1;
	public GameStateArena arena;
	public int slot;

	public EntityZoneTemplate(int player, Zones zone, EntityLookupTemplate lookup) {
		this.lookup = lookup;
		this.zone = zone;
		this.player = player;
		this.arena = lookup != null ? lookup.arena : new GameStateArena(16);
		this.slot = GameStateArena.slot(player, zone);
	}

	public EntityZoneTemplate(int player, Zones zone, EntityLookupTemplate lookup, GameStateArena arena) {
		this.lookup = lookup;
		this.zone = zone;
		this.player = player;
		this.arena = arena;
		this.slot = GameStateArena.slot(player, zone);
	}

	@NotNull
	public static Comparator<Card> getManaCostComparator() {
		return (card1, card2) -> {
			Integer manaCost1 = card1.getBaseManaCost();
			Integer manaCost2 = card2.getBaseManaCost();
			return manaCost1.compareTo(manaCost2);
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	public EntityZoneTemplate<E> clone() {
		EntityZoneTemplate<E> cloneZone = new EntityZoneTemplate<>(getPlayer(), getZone(), null);
		int size = arena.zoneSizes[slot];
		Entity[] srcArr = arena.zoneArrays[slot];
		for (int i = 0; i < size; i++) {
			E cloned = (E) srcArr[i].clone();
			cloneZone.arena.zoneAdd(cloneZone.slot, player, zone, i, cloned, false);
		}
		return cloneZone;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final E get(int index) {
		return (E) arena.zoneGet(slot, player, zone, index);
	}

	@Override
	public E set(int index, E element) {
		checkElement(element);
		return setUnchecked(index, element);
	}

	@Override
	public void sort(Comparator<? super E> c) {
		int size = arena.zoneSizes[slot];
		if (size <= 1) {
			return;
		}
		Object[] a = new Object[size];
		for (int i = 0; i < size; i++) {
			a[i] = get(i);
		}
		@SuppressWarnings("unchecked")
		Comparator<Object> c1 = (Comparator<Object>) c;
		Arrays.sort(a, c1);
		for (int i = 0; i < size; i++) {
			@SuppressWarnings("unchecked")
			E element = (E) a[i];
			setUnchecked(i, element);
		}
	}

	private void checkElement(E element) {
		if (contains(element)) {
			throw new ArrayStoreException("You cannot add the same element twice to this zone.");
		}
		if (!element.getEntityLocation().equals(EntityLocation.UNASSIGNED)
				&& element.getEntityLocation().getZone() != getZone()) {
			throw new ArrayStoreException("You must (1) remove an entity from its previous zone or " +
					"(2) clone the entity in order to add it to another zone.");
		}
	}

	protected E setUnchecked(int index, E element) {
		arena.zoneSetUnchecked(slot, player, zone, index, element, lookup != null);
		return element;
	}

	@Override
	public boolean add(E element) {
		checkElement(element);
		uncheckedAdd(arena.zoneSizes[slot], element);
		return true;
	}

	@Override
	public void add(int index, E element) {
		checkElement(element);
		uncheckedAdd(index, element);
	}

	protected void uncheckedAdd(int index, E element) {
		arena.zoneAdd(slot, player, zone, index, element, lookup != null);
	}

	protected void updateLookup(E element) {
		if (lookup != null && element.getId() != IdFactory.UNASSIGNED) {
			lookup.put(element.getId(), element);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public E remove(int index) {
		return (E) arena.zoneRemove(slot, player, zone, index, lookup != null);
	}

	@Override
	public void clear() {
		arena.zoneClear(slot, player, lookup != null);
	}

	@Override
	public int indexOf(Object e) {
		return arena.zoneIndexOf(slot, player, zone, e);
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	@Override
	public boolean remove(Object e) {
		int index = indexOf(e);
		if (index == -1) {
			return false;
		} else {
			remove(index);
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	public void move(int index, EntityZoneTemplate destination, int destinationIndex) {
		if (destination.arena == this.arena) {
			arena.zoneMove(
					slot, player, zone, index, lookup != null,
					destination.slot, destination.player, destination.zone, destinationIndex, destination.lookup != null);
		} else {
			Entity result = arena.zoneRemove(slot, player, zone, index, destination.getPlayer() != getPlayer() && lookup != null);
			destination.uncheckedAdd(destinationIndex, result);
		}
	}

	@SuppressWarnings("unchecked")
	public void move(Entity source, EntityZoneTemplate destination) {
		if (source.getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
			destination.uncheckedAdd(destination.size(), source);
		} else if (source.getEntityLocation().getZone() != getZone()
				|| source.getEntityLocation().getPlayer() != getPlayer()) {
			throw new ArrayStoreException("Cannot move an element that could not possible be inside this EntityZone.");
		} else {
			move(indexOf(source), destination, destination.size());
		}
	}

	protected void exchange(E entity1, E entity2) {
		arena.zoneExchange(slot, player, zone, entity1, entity2);
	}

	@Override
	public int size() {
		return arena.zoneSizes[slot];
	}

	@Override
	public boolean isEmpty() {
		return arena.zoneSizes[slot] == 0;
	}

	public void setPlayer(int playerIndex) {
		if (player != -1) {
			throw new UnsupportedOperationException("You cannot change the player index on an already-initialized zone.");
		}
		int oldSlot = this.slot;
		this.player = playerIndex;
		int newSlot = GameStateArena.slot(playerIndex, zone);
		this.slot = newSlot;
		if (lookup != null) {
			lookup.player = playerIndex;
		}
		arena.updateZonePlayer(oldSlot, newSlot, playerIndex, zone);
	}

	public Zones getZone() {
		return zone;
	}

	public int getPlayer() {
		return player;
	}

	public static EntityZoneTemplate<Entity> empty(Zones zone, int player) {
		return new EntityZoneTemplate<>(player, zone, null);
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> void swap(E sourceEntity, E targetEntity, EntityZoneTable context) {
		EntityZoneTemplate<E> sourceZone = (EntityZoneTemplate<E>) (Object) context.getZone(
				sourceEntity.getEntityLocation().getPlayer(), sourceEntity.getEntityLocation().getZone());
		EntityZoneTemplate<E> targetZone = (EntityZoneTemplate<E>) (Object) context.getZone(
				targetEntity.getEntityLocation().getPlayer(), targetEntity.getEntityLocation().getZone());
		int sourceIndex = sourceEntity.getEntityLocation().getIndex();
		int targetIndex = targetEntity.getEntityLocation().getIndex();
		int sourceOwner = sourceEntity.getOwner();
		int targetOwner = targetEntity.getOwner();
		if (sourceEntity.getOwner() != targetOwner) {
			sourceEntity.setOwner(targetOwner);
		}
		if (targetEntity.getOwner() != sourceOwner) {
			targetEntity.setOwner(sourceOwner);
		}

		if (sourceZone == targetZone) {
			sourceZone.exchange(sourceEntity, targetEntity);
		} else {
			sourceZone.move(sourceIndex, targetZone, targetIndex);
			targetZone.move(targetIndex + 1, sourceZone, sourceIndex);
		}
	}

	@Override
	public int compareTo(@NotNull EntityZoneTemplate<? extends Entity> o) {
		int min = Math.min(this.size(), o.size());
		for (int i = 0; i < min; i++) {
			E e1 = this.get(i);
			Entity e2 = o.get(i);
			int comparison = Objects.requireNonNull(e1) == Objects.requireNonNull(e2) ? 0 : e1.compareTo(e2);
			if (comparison != 0) {
				return comparison;
			}
		}
		return this.size() - o.size();
	}

	public EntityZoneTemplate<E> setLookup(EntityLookupTemplate lookup) {
		this.lookup = lookup;
		if (lookup != null) {
			this.arena = lookup.arena;
		}
		return this;
	}
}
