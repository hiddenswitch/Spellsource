package net.demilich.metastone.game.entities;

import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;

public interface EntityZoneTable {
	/**
	 * Retrieves an {@link EntityZone} for the provided owner and zone.
	 *
	 * @param owner The owner of the zone.
	 * @param zone  The {@link Zones} key.
	 * @param <E>   The type of entity hosted by this zone.
	 * @return An {@link EntityZone} reference.
	 */
	<E extends Entity> EntityZone<E> getZone(int owner, Zones zone);
}
