package net.demilich.metastone.game.entities.minions;

import java.util.Objects;

public class Race {

	public static final String NONE = "NONE";
	public static final String ALL = "ALL";

	public static boolean hasRace(String og, String comparedTo) {
		if (Objects.equals(og, ALL) && !Objects.equals(comparedTo, NONE)) {
			return true;
		}
		if (Objects.equals(comparedTo, ALL) && !Objects.equals(og, NONE)) {
			return true;
		}

		if (og.contains("&")) {
			String[] races = og.split("&");
			for (String race : races) {
				if (Objects.equals(race, comparedTo)) {
					return true;
				}
			}
		}
		if (comparedTo.contains("&")) {
			String[] races = comparedTo.split("&");
			for (String race : races) {
				if (Objects.equals(race, og)) {
					return true;
				}
			}
		}

		return Objects.equals(og, comparedTo);
	}
}
