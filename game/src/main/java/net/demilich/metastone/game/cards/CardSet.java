package net.demilich.metastone.game.cards;

/**
 * An enumeration of card sets from various games.
 */
public enum CardSet {
	ANY,
	BASIC,
	CLASSIC,
	REWARD,
	PROMO,
	NAXXRAMAS,
	GOBLINS_VS_GNOMES,
	BLACKROCK_MOUNTAIN,
	THE_GRAND_TOURNAMENT,
	LEAGUE_OF_EXPLORERS,
	THE_OLD_GODS,
	ONE_NIGHT_IN_KARAZHAN,
	MEAN_STREETS_OF_GADGETZHAN,
	PROCEDURAL_PREVIEW,
	JOURNEY_TO_UNGORO,
	KNIGHTS_OF_THE_FROZEN_THRONE,
	HALL_OF_FAME,
	CUSTOM,
	TEST,
	SPELLSOURCE;

	public static CardSet latest() {
		return KNIGHTS_OF_THE_FROZEN_THRONE;
	}
}
