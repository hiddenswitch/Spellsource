package net.demilich.metastone.game.entities;

import net.demilich.metastone.game.targeting.PlayerZones;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created by bberman on 4/16/17.
 */
public final class EntityLocation implements Serializable {
	public static final EntityLocation NONE = new EntityLocation(PlayerZones.NONE, -1, -1);
	private final PlayerZones zone;
	private final int player;
	private final int index;

	public EntityLocation(PlayerZones zone, int player, int index) {
		this.zone = zone;
		this.player = player;
		this.index = index;
	}

	public EntityLocation(EntityLocation original, int newIndex) {
		this.zone = original.getZone();
		this.player = original.getPlayer();
		this.index = newIndex;
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
		return new HashCodeBuilder().append(zone).append(player).toHashCode();
	}

	public PlayerZones getZone() {
		return zone;
	}

	public int getPlayer() {
		return player;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return String.format("[player:%d zone:%s index:%d]", player, zone, index);
	}
}
