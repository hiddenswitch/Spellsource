package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.decks.DeckFormat;

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
	MEAN_STREETS_OF_GADGETZAN,
	PROCEDURAL_PREVIEW,
	JOURNEY_TO_UNGORO,
	KNIGHTS_OF_THE_FROZEN_THRONE,
	KOBOLDS_AND_CATACOMBS,
	WITCHWOOD,
	BOOMSDAY_PROJECT,
	BLIZZARD_ADVENTURE,
	HALL_OF_FAME,
	CUSTOM,
	ALTERNATIVE,
	UNNERFED,
	BATTLE_FOR_ASHENVALE,
	SANDS_OF_TIME,
	TEST,
	SPELLSOURCE;

	public static CardSet latestHearthstoneExpansion() {
		return BOOMSDAY_PROJECT;
	}

	public boolean isHearthstoneSet() {
		return DeckFormat.WILD.isInFormat(this);
	}
}