package net.demilich.metastone.game.entities;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.targeting.Zones;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * EntityZone is an abstract list that enforces that (1) supports gameplay-safe cloning and (2) enforces that an {@link
 * Entity} object is only in one zone at any time.
 * <p>
 * Each zone has a corresponding {@link Zones} and owning {@link net.demilich.metastone.game.Player} ID.
 *
 * @param <E> The subclass of {@link Entity} that is stored. For example, {@link Zones#BATTLEFIELD} can only store
 *            {@link net.demilich.metastone.game.entities.minions.Minion} entities.
 * @see net.demilich.metastone.game.cards.CardList for an interface that adds additional features for lists of cards,
 * 		like the {@link Zones#HAND} and the {@link Zones#DECK}.
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

	@NotNull
	public static Comparator<Card> getManaCostComparator() {
		return (card1, card2) -> {
			Integer manaCost1 = card1.getBaseManaCost();
			Integer manaCost2 = card2.getBaseManaCost();
			return manaCost1.compareTo(manaCost2);
		};
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

	/**
	 * Swaps two entities with each other. They must be of the same type.
	 *
	 * @param sourceEntity One of the two entities to swap in location.
	 * @param targetEntity One of the two entities to swap in location.
	 * @param context      An {@link EntityZoneTable}, typically a {@link GameContext}, to look up entities inside of.
	 * @param <E>          The type of the entity to swap (must be the same).
	 */
	public static <E extends Entity> void swap(E sourceEntity, E targetEntity, EntityZoneTable context) {
		EntityZone<E> sourceZone = context.getZone(sourceEntity.getEntityLocation().getPlayer(), sourceEntity.getEntityLocation().getZone());
		EntityZone<E> targetZone = context.getZone(targetEntity.getEntityLocation().getPlayer(), targetEntity.getEntityLocation().getZone());
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
		sourceZone.move(sourceIndex, targetZone, targetIndex);
		targetZone.move(targetIndex + 1, sourceZone, sourceIndex);
	}
}

