package net.demilich.metastone.game.entities;

import co.paralleluniverse.common.util.Objects;

public enum EntityType {
	ANY,
	ACTOR,
	HERO,
	MINION,
	WEAPON,
	CARD,
	PLAYER,
	ENCHANTMENT,
	QUEST,
	SECRET;

	public boolean hasEntityType(EntityType other) {
		if (this == ANY || other == ANY) {
			return true;
		}

		if (this == ACTOR) {
			return other == HERO || other == MINION || other == WEAPON;
		}

		if (other == ACTOR) {
			return this == HERO || this == MINION || this == WEAPON;
		}

		return Objects.equal(this, other);
	}
}
