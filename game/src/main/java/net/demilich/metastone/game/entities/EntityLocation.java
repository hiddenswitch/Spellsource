package net.demilich.metastone.game.entities;

import net.demilich.metastone.game.targeting.Zones;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Created by bberman on 4/16/17.
 */
public final class EntityLocation implements Serializable {
	public static final EntityLocation NONE = new EntityLocation(Zones.NONE, -1, -1);
	private final Zones zone;
	private final int player;
	private final int index;

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

	public Zones getZone() {
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
