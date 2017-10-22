package net.demilich.metastone.game.entities.heroes;

public enum HeroClass {
	ANY,
	DECK_COLLECTION,

	NEUTRAL,

	DRUID,
	HUNTER,
	MAGE,
	PALADIN,
	PRIEST,
	ROGUE,
	SHAMAN,
	WARLOCK,
	WARRIOR,

	SELF,
	OPPONENT,
	INHERIT,
	DEATH_KNIGHT;

	public boolean isBaseClass() {
		HeroClass[] nonBaseClasses = {ANY, NEUTRAL, SELF, DECK_COLLECTION, OPPONENT, INHERIT, DEATH_KNIGHT};
		for (int i=0; i<nonBaseClasses.length; i++) {
			if (nonBaseClasses[i] == this) {
				return false;
			}
		}
		return true;
	}
}
