package net.demilich.metastone.game.entities.minions;

public enum Race {
	NONE,
	BEAST,
	MURLOC,
	PIRATE,
	DEMON,
	DRAGON,
	TOTEM,
	MECH,
	ELEMENTAL,
	ALL;

	public boolean hasRace(Race comparedTo) {
		if (this == ALL && comparedTo != NONE && comparedTo != null) {
			return true;
		}

		if (comparedTo == ALL
				&& this != NONE) {
			return true;
		}

		return this == comparedTo;
	}
}
