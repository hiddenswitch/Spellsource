package net.demilich.metastone.game.entities.heroes;

public enum HeroClass {
	ANY,
	DECK_COLLECTION,
	BROWN,
	GREEN,
	BLUE,
	GOLD,
	WHITE,
	BLACK,
	SILVER,
	VIOLET,
	RED,
	SPIRIT,
	SELF,
	OPPONENT,
	INHERIT;

	public boolean isBaseClass() {
		HeroClass[] nonBaseClasses = {ANY, SELF, DECK_COLLECTION, OPPONENT, INHERIT, SPIRIT};
		for (int i = 0; i < nonBaseClasses.length; i++) {
			if (nonBaseClasses[i] == this) {
				return false;
			}
		}
		return true;
	}
}
