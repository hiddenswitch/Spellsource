package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;

/**
 * An enumeration of card sets from various games.
 */
public class CardSet {
	public static final String ANY = "ANY";
	public static final String BASIC = "HS_BASIC";
	public static final String CLASSIC = "HS_CLASSIC";
	public static final String REWARD = "HS_REWARD";
	public static final String PROMO = "HS_PROMO";
	public static final String NAXXRAMAS = "HS_NAXXRAMAS";
	public static final String GOBLINS_VS_GNOMES = "HS_GOBLINS_VS_GNOMES";
	public static final String BLACKROCK_MOUNTAIN = "HS_BLACKROCK_MOUNTAIN";
	public static final String THE_GRAND_TOURNAMENT = "HS_THE_GRAND_TOURNAMENT";
	public static final String LEAGUE_OF_EXPLORERS = "HS_LEAGUE_OF_EXPLORERS";
	public static final String THE_OLD_GODS = "HS_THE_OLD_GODS";
	public static final String ONE_NIGHT_IN_KARAZHAN = "HS_ONE_NIGHT_IN_KARAZHAN";
	public static final String MEAN_STREETS_OF_GADGETZAN = "HS_MEAN_STREETS_OF_GADGETZAN";
	public static final String PROCEDURAL_PREVIEW = "PROCEDURAL_PREVIEW";
	public static final String JOURNEY_TO_UNGORO = "HS_JOURNEY_TO_UNGORO";
	public static final String KNIGHTS_OF_THE_FROZEN_THRONE = "HS_KNIGHTS_OF_THE_FROZEN_THRONE";
	public static final String KOBOLDS_AND_CATACOMBS = "HS_KOBOLDS_AND_CATACOMBS";
	public static final String WITCHWOOD = "HS_WITCHWOOD";
	public static final String BOOMSDAY_PROJECT = "HS_BOOMSDAY_PROJECT";
	public static final String RASTAKHANS_RUMBLE = "HS_RASTAKHANS_RUMBLE";
	public static final String RISE_OF_SHADOWS = "HS_RISE_OF_SHADOWS";
	public static final String SAVIORS_OF_ULDUM = "HS_SAVIORS_OF_ULDUM";
	public static final String DESCENT_OF_DRAGONS = "HS_DESCENT_OF_DRAGONS";
	public static final String ASHES_OF_OUTLAND = "HS_ASHES_OF_OUTLAND";
	public static final String SCHOLOMANCE_ACADEMY = "HS_SCHOLOMANCE_ACADEMY";
	public static final String MADNESS_AT_THE_DARKMOON_FAIRE = "HS_MADNESS_AT_THE_DARKMOON_FAIRE";
	public static final String FORGED_IN_THE_BARRENS = "HS_FORGED_IN_THE_BARRENS";
	public static final String UNITED_IN_STORMWIND = "HS_UNITED_IN_STORMWIND";
	public static final String FRACTURED_IN_ALTERAC_VALLEY = "HS_FRACTURED_IN_ALTERAC_VALLEY";
	public static final String VOYAGE_TO_THE_SUNKEN_CITY = "HS_VOYAGE_TO_THE_SUNKEN_CITY";
	public static final String MURDER_AT_CASTLE_NATHRIA = "HS_MURDER_AT_CASTLE_NATHRIA";
	public static final String MARCH_OF_THE_LICH_KING = "HS_MARCH_OF_THE_LICH_KING";
	public static final String FESTIVAL_OF_LEGENDS = "HS_FESTIVAL_OF_LEGENDS";
	public static final String TITANS = "HS_TITANS";
	public static final String SHOWDOWN_IN_THE_BADLANDS = "HS_SHOWDOWN_IN_THE_BADLANDS";
	public static final String WHIZBANGS_WORKSHOP = "HS_WHIZBANGS_WORKSHOP";
	public static final String PERILS_IN_PARADISE = "HS_PERILS_IN_PARADISE";
	public static final String SPACE = "HS_SPACE";
	public static final String EMERALD_DREAM = "HS_EMERALD_DREAM";
	public static final String THE_LOST_CITY = "HS_THE_LOST_CITY";
	public static final String TIME_TRAVEL = "HS_TIME_TRAVEL";
	public static final String CORE = "HS_CORE";
	public static final String LEGACY = "HS_LEGACY";
	public static final String WONDERS = "HS_WONDERS";
	public static final String DEMON_HUNTER_INITIATE = "HS_DEMON_HUNTER_INITIATE";
	public static final String PATH_OF_ARTHAS = "HS_PATH_OF_ARTHAS";
	public static final String YEAR_OF_THE_DRAGON = "HS_YEAR_OF_THE_DRAGON";
	public static final String BLIZZARD_ADVENTURE = "HS_BLIZZARD_ADVENTURE";
	public static final String HALL_OF_FAME = "HS_HALL_OF_FAME";
	public static final String CUSTOM = "CUSTOM";
	public static final String ALTERNATIVE = "HS_ALTERNATIVE";
	public static final String UNNERFED = "HS_UNNERFED";
	public static final String BATTLE_FOR_ASHENVALE = "HS_BATTLE_FOR_ASHENVALE";
	public static final String SANDS_OF_TIME = "HS_SANDS_OF_TIME";
	public static final String VERDANT_DREAMS = "HS_VERDANT_DREAMS";
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
	public static final String GRAVEYARD = "HS_GRAVEYARD";

	public static boolean isHearthstoneSet(String set) {
		DeckFormat wild = ClasspathCardCatalogue.INSTANCE.getFormat("Wild");
		if (wild == null) {
			return false;
		}
		return wild.isInFormat(set);
	}
}
