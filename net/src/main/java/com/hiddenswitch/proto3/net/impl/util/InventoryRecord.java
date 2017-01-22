package com.hiddenswitch.proto3.net.impl.util;

import net.demilich.metastone.game.cards.Card;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Created by bberman on 1/22/17.
 */
public class InventoryRecord {
	public final String instanceId;
	public final Card card;

	public InventoryRecord(Card card) {
		this.instanceId = RandomStringUtils.randomAlphanumeric(48).toLowerCase();
		this.card = card;
	}

	@Override
	public int hashCode() {
		return instanceId.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		InventoryRecord rhs = (InventoryRecord) other;
		return this.instanceId.equals(rhs.instanceId);
	}
}
