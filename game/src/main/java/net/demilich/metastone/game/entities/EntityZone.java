package net.demilich.metastone.game.entities;

import net.demilich.metastone.game.targeting.Zones;

import java.io.Serializable;
import java.util.*;

/**
 * Created by bberman on 4/16/17.
 */
public class EntityZone<E extends Entity> extends AbstractList<E> implements
		List<E>, Iterable<E>, Cloneable, Serializable {
	protected final Zones zone;
	protected int player = -1;
	protected List<E> internal = new ArrayList<>();

	public EntityZone(int player, Zones zone) {
		this.zone = zone;
		this.player = player;
	}

	@SuppressWarnings("unchecked")
	public EntityZone<E> clone() {
		// Clone all the cards too
		EntityZone<E> zone = new EntityZone<>(getPlayer(), getZone());
		for (E e : this) {
			zone.uncheckedAdd(zone.size(), (E) e.clone());
		}
		return zone;
	}

	@Override
	public E get(int index) {
		return internal.get(index);
	}

	public E set(int index, E element) {
		checkElement(element);
		return setUnchecked(index, element);
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
		internal.set(index, element);
		element.setEntityLocation(new EntityLocation(zone, player, index));
		return element;
	}

	@Override
	public void add(int index, E element) {
		checkElement(element);
		uncheckedAdd(index, element);
	}

	protected void uncheckedAdd(int index, E element) {
		if (index > size()) {
			throw new IndexOutOfBoundsException();
		}
		internal.add(index, element);
		for (int i = index; i < internal.size(); i++) {
			internal.get(i).setEntityLocation(new EntityLocation(zone, player, i));
		}
	}

	@Override
	public E remove(int index) {
		E result = internal.remove(index);
		result.setEntityLocation(EntityLocation.UNASSIGNED);
		for (int i = index; i < internal.size(); i++) {
			internal.get(i).setEntityLocation(new EntityLocation(zone, player, i));
		}
		return result;
	}

	@Override
	public int indexOf(Object e) {
		if (e == null) {
			return -1;
		}
		if (Entity.class.isAssignableFrom(e.getClass())) {
			final Entity entity = (Entity) e;
			final EntityLocation location = entity.getEntityLocation();
			if (location.getZone() == getZone()
					&& location.getPlayer() == getPlayer()) {
				return location.getIndex();
			}
		}

		return super.indexOf(e);
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
	public void move(int index, EntityZone destination, int destinationIndex) {
		Entity result = internal.remove(index);
		for (int i = index; i < internal.size(); i++) {
			internal.get(i).setEntityLocation(new EntityLocation(zone, player, i));
		}
		destination.uncheckedAdd(destinationIndex, result);
	}

	@SuppressWarnings("unchecked")
	public void move(Entity source, EntityZone destination) {
		if (source.getEntityLocation().equals(EntityLocation.UNASSIGNED)) {
			destination.uncheckedAdd(destination.size(), source);
		} else if (source.getEntityLocation().getZone() != getZone()
				|| source.getEntityLocation().getPlayer() != getPlayer()) {
			throw new ArrayStoreException("Cannot move an element that could not possible be inside this EntityZone.");
		} else {
			move(indexOf(source), destination, destination.size());
		}

	}

	@Override
	public int size() {
		return internal.size();
	}

	public void setPlayer(int playerIndex) {
		if (player != -1) {
			throw new UnsupportedOperationException("You cannot change the player index on an already-initialized zone.");
		}

		player = playerIndex;
		for (int i = 0; i < internal.size(); i++) {
			internal.get(i).setEntityLocation(new EntityLocation(zone, player, i));
		}
	}

	public Zones getZone() {
		return zone;
	}

	public int getPlayer() {
		return player;
	}

	public static EntityZone empty(int player) {
		return new EntityZone(player, Zones.NONE);
	}
}

