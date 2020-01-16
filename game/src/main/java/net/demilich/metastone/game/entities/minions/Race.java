package net.demilich.metastone.game.entities.minions;

import java.util.Objects;

/**
 * The race / tribe of a card.
 * <p>
 * Cards can belong to a specific tribe by specifying a string in the {@link net.demilich.metastone.game.cards.desc.CardDesc#race}
 * field.
 * <p>
 * This previously used to be an enumeration but now is a string. Races must be all-capitals.
 */
public class Race {

	public static final String NONE = "NONE";
	public static final String ALL = "ALL";
	public static final String FAE = "FAE";
	public static final String SPIRIT = "SPIRIT";
	public static final String MURLOC = "MURLOC";
	public static final String TOTEM = "TOTEM";

	/**
	 * Returns {@code true} if the given race string is considered as having the race {@code rhs}, accounting for whether
	 * or not the specification is {@code "ALL"}, {@code "NONE"}.
	 * <p>
	 * If {@code lhs} contains an ampersand-separated list of races, the {@code lhs} counts as having both races, and can
	 * match as long as {@code rhs} is any one of the races in {@code lhs}.
	 *
	 * @param lhs
	 * @param rhs
	 * @return {@code true} if {@code lhs} has the race specified in {@code rhs}, including ALL.
	 */
	public static boolean hasRace(String lhs, String rhs) {
		if (Objects.equals(lhs, ALL) && !Objects.equals(rhs, NONE)) {
			return true;
		}
		if (Objects.equals(rhs, ALL) && !Objects.equals(lhs, NONE)) {
			return true;
		}

		String[] lhsRaces;
		if (lhs.contains("&")) {
			lhsRaces = lhs.split("&");
		} else {
			lhsRaces = new String[]{lhs};
		}

		String[] rhsRaces;
		if (rhs.contains("&")) {
			rhsRaces = rhs.split("&");
		} else {
			rhsRaces = new String[]{rhs};
		}

		for (String lhsRace : lhsRaces) {
			for (String rhsRace : rhsRaces) {
				if (Objects.equals(lhsRace, rhsRace)) {
					return true;
				}
			}
		}
		return false;
	}
}
