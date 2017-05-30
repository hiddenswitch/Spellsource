package net.demilich.metastone.game.entities;

import com.hiddenswitch.proto3.net.common.GameState;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * The location of an entity inside a {@link net.demilich.metastone.game.GameContext}.
 * <p>
 * Every entity has an {@link EntityLocation} that starts as {@link #UNASSIGNED}. The creator of the {@link Entity} object
 * should add it to an {@link EntityZone}, which sets that entity's {@link Entity#entityLocation}.
 * <p>
 * A location has a {@link Player}, {@link Zones} and {@link Integer} {@link #index} tuple that are comparable and
 * exclusive—only one entity can be in a given location at a given time. These locations are used to find entities like
 * {@link GameContext#tryFind(EntityReference)}, to support effects like {@link net.demilich.metastone.game.spells.AdjacentEffectSpell},
 * and to support networking effects and diffing for game state.
 * @see Entity#getEntityLocation() to look up an entity's current location.
 * @see Entity#getEntityLocation()#getIndex() to get the entity's index in an entity zone.
 * @see EntityZone for the {@link java.util.List} that sets this property on an {@link Entity} whenever it is the
 * argument to an {@link EntityZone#add(Object)} or {@link EntityZone#move(Entity, EntityZone)}.
 * @see EntityZone#move(Entity, EntityZone) for the method to use to move an entity from one entity to another when
 * you already know which zone the entity is in.
 * @see Entity#moveOrAddTo(GameContext, Zones) for the method that moves an entity from its current zone to the newly
 * specified zone.
 * @see EntityZone#indexOf(Object) to get the index of the an entity in O(1) time (it just uses its {@link Entity#getEntityLocation()#getIndex()}}
 * method to find the index quickly).
 * @see Player#getZone(Zones) for the method that gets the {@link EntityZone} from a player object.
 * @see GameContext#getEntities() for a method that iterates through all the entities in all the zones in a {@link GameContext}
 * @see GameState#getEntities() for a method that iterates through all the entities in all the zones for a {@link GameState}
 * (a {@link GameState} stores all the data/model that is needed to reconstruct a {@link GameContext}.
 */
public final class EntityLocation implements Serializable {
	/**
	 * An unassigned location.
	 */
	public static final EntityLocation UNASSIGNED = new EntityLocation(Zones.NONE, -1, -1);
	private final Zones zone;
	private final int player;
	private final int index;

	/**
	 * Create the location with the specified zone, player and index.
	 * @param zone The zone in the game.
	 * @param player The player's index, or {@link net.demilich.metastone.game.targeting.IdFactory#UNASSIGNED} if the
	 *               owner is not yet known.
	 * @param index The index of the object, or {@code -1} if it is not yet known.
	 */
	public EntityLocation(Zones zone, int player, int index) {
		this.zone = zone;
		this.player = player;
		this.index = index;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			throw new RuntimeException();
		}

		EntityLocation rhs = (EntityLocation) other;
		return rhs.zone == zone && rhs.player == player && rhs.index == index;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(index)
				.append(zone)
				.append(player).toHashCode();
	}

	/**
	 * Gets the zone of this location.
	 * @return The zone.
	 * @see Zones for a complete description of each zone.
	 */
	public Zones getZone() {
		return zone;
	}

	/**
	 * Gets the index of the player for this location, or {@code -1} if it has not yet been assigned. (With the exception
	 * of {@link #UNASSIGNED}, a {@link #player} fields with a value of {@code -1} is an invalid location.
	 * @return The player index, or {@link -1} if it has not yet been assigned.
	 * @see GameContext#getPlayer(int) to get the player pointed to by this index.
	 */
	public int getPlayer() {
		return player;
	}

	/**
	 * Gets the index in the {@link EntityZone} for this location.
	 * @return The index.
	 * @see EntityZone#get(int) to get the entity pointed to by this index.
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return String.format("[player:%d zone:%s index:%d]", player, zone, index);
	}
}
