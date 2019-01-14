package net.demilich.metastone.game.entities.minions;

public enum Race {
	NONE,
	BEAST,
	BEAST_DRAGON,
	MURLOC,
	PIRATE,
	DEMON,
	DRAGON,
	TOTEM,
	MECH,
	ELEMENTAL,
	TITAN,
	ALL;

	public boolean hasRace(Race comparedTo) {
		if (this == ALL && comparedTo != NONE && comparedTo != null) {
			return true;
		}

		if (comparedTo == ALL
				&& this != NONE) {
			return true;
		}

		if (this == BEAST_DRAGON && (comparedTo == BEAST || comparedTo == DRAGON)) {
			return true;
		}

		if ((this == DRAGON || this == BEAST) && comparedTo == BEAST_DRAGON) {
			return true;
		}

		return this == comparedTo;
	}
}
