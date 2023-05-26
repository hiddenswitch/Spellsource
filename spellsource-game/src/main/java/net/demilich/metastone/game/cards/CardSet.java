package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;

/**
 * An enumeration of card sets from various games.
 */
public class CardSet {
	public static final String ANY = "ANY";
	public static final String BASIC = "BASIC";
	public static final String CLASSIC = "CLASSIC";
	public static final String REWARD = "REWARD";
	public static final String PROMO = "PROMO";
	public static final String NAXXRAMAS = "NAXXRAMAS";
	public static final String GOBLINS_VS_GNOMES = "GOBLINS_VS_GNOMES";
	public static final String BLACKROCK_MOUNTAIN = "BLACKROCK_MOUNTAIN";
	public static final String THE_GRAND_TOURNAMENT = "THE_GRAND_TOURNAMENT";
	public static final String LEAGUE_OF_EXPLORERS = "LEAGUE_OF_EXPLORERS";
	public static final String THE_OLD_GODS = "THE_OLD_GODS";
	public static final String ONE_NIGHT_IN_KARAZHAN = "ONE_NIGHT_IN_KARAZHAN";
	public static final String MEAN_STREETS_OF_GADGETZAN = "MEAN_STREETS_OF_GADGETZAN";
	public static final String PROCEDURAL_PREVIEW = "PROCEDURAL_PREVIEW";
	public static final String JOURNEY_TO_UNGORO = "JOURNEY_TO_UNGORO";
	public static final String KNIGHTS_OF_THE_FROZEN_THRON = "KNIGHTS_OF_THE_FROZEN_THRON";
	public static final String KOBOLDS_AND_CATACOMBS = "KOBOLDS_AND_CATACOMBS";
	public static final String WITCHWOOD = "WITCHWOOD";
	public static final String BOOMSDAY_PROJECT = "BOOMSDAY_PROJECT";
	public static final String RASTAKHANS_RUMBLE = "RASTAKHANS_RUMBLE";
	public static final String RISE_OF_SHADOWS = "RISE_OF_SHADOWS";
	public static final String BLIZZARD_ADVENTURE = "BLIZZARD_ADVENTURE";
	public static final String HALL_OF_FAME = "HALL_OF_FAME";
	public static final String CUSTOM = "CUSTOM";
	public static final String ALTERNATIVE = "ALTERNATIVE";
	public static final String UNNERFED = "UNNERFED";
	public static final String BATTLE_FOR_ASHENVALE = "BATTLE_FOR_ASHENVALE";
	public static final String SANDS_OF_TIME = "SANDS_OF_TIME";
	public static final String VERDANT_DREAMS = "VERDANT_DREAMS";
	public static final String TEST = "TEST";
	/**
	 * Cards in the Spellsource basic set.
	 */
	public static final String SPELLSOURCE_BASIC = "SPELLSOURCE_BASIC";
	/**
	 * Spellsource / Minionate legacy cards.
	 */
	public static final String SPELLSOURCE = "SPELLSOURCE";
	/**
	 * Indicates the card should no longer be in any user's collections or appear in any formats.
	 */
	public static final String GRAVEYARD = "GRAVEYARD";

	public static boolean isHearthstoneSet(String set) {
		DeckFormat wild = ClasspathCardCatalogue.CLASSPATH.getFormat("Wild");
		if (wild == null) {
			return false;
		}
		return wild.isInFormat(set);
	}
}