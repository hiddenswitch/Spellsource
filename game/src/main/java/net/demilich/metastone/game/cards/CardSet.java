package net.demilich.metastone.game.cards;

import com.google.common.collect.Sets;

import java.util.EnumSet;
import java.util.Set;

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
	KOBOLDS_AND_CATACOMBS,
	HALL_OF_FAME,
	CUSTOM,
	TEST,
	SPELLSOURCE;

	static final Set<CardSet> hearthstoneSets = EnumSet.of(BASIC,
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
			JOURNEY_TO_UNGORO,
			KNIGHTS_OF_THE_FROZEN_THRONE,
			KOBOLDS_AND_CATACOMBS,
			HALL_OF_FAME);

	public static CardSet latest() {
		return KOBOLDS_AND_CATACOMBS;
	}

	public static Set<CardSet> hearthstone() {
		return hearthstoneSets;
	}

	public boolean isHearthstoneSet() {
		return hearthstoneSets.contains(this);
	}
}
