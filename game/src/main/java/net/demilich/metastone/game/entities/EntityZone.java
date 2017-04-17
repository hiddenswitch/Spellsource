package net.demilich.metastone.game.entities;

import net.demilich.metastone.game.targeting.PlayerZones;

import java.io.Serializable;
import java.util.*;

/**
 * Created by bberman on 4/16/17.
 */
public class EntityZone<E extends Entity> extends AbstractList<E> implements
		List<E>, Cloneable, Serializable {
	private final PlayerZones zone;
	private int player = -1;
	private List<E> internal = new ArrayList<>();

	public EntityZone(int player, PlayerZones zone) {
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
		if (!element.getEntityLocation().equals(EntityLocation.NONE)
				&& element.getEntityLocation().getZone() != getZone()) {
			throw new ArrayStoreException("You must (1) remove an entity from its previous zone or " +
					"(2) clone the entity in order to add it to another zone.");
		}
	}

	protected E setUnchecked(int index, E element) {
		if (internal.size() > index) {
			internal.get(index).setEntityLocation(EntityLocation.NONE);
		} else if (internal.size() == index) {
			if (element == null) {
				remove(index);
				return element;
			}

			uncheckedAdd(0, element);
			return element;
		}
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
		internal.add(index, element);
		for (int i = index; i < internal.size(); i++) {
			internal.get(i).setEntityLocation(new EntityLocation(zone, player, i));
		}
	}

	@Override
	public E remove(int index) {
		E result = internal.remove(index);
		result.setEntityLocation(EntityLocation.NONE);
		for (int i = index; i < internal.size(); i++) {
			internal.get(i).setEntityLocation(new EntityLocation(zone, player, i));
		}
		return result;
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

	public PlayerZones getZone() {
		return zone;
	}

	public int getPlayer() {
		return player;
	}
}

