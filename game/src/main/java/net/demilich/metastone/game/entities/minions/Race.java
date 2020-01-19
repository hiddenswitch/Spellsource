package net.demilich.metastone.game.entities.minions;

import com.google.common.collect.ObjectArrays;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.MenagerieMogulAura;

import java.util.List;
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
	public static final String DEMON = "DEMON";

	/**
	 * Returns {@code true} if the given race string is considered as having the race {@code rhs}, accounting for whether
	 * or not the specification is {@code "ALL"}, {@code "NONE"}.
	 * <p>
	 * If {@code lhs} contains an ampersand-separated list of races, the {@code lhs} counts as having both races, and can
	 * match as long as {@code rhs} is any one of the races in {@code lhs}.
	 *
	 * @param gameContext A context that will be scanned for auras that potentially change the result of this evaluation.
	 * @param entity
	 * @param rhs
	 * @return {@code true} if {@code lhs} has the race specified in {@code rhs}, including ALL.
	 */
	public static boolean hasRace(GameContext gameContext, Entity entity, String rhs) {
		if (Objects.equals(entity.getRace(), ALL) && !Objects.equals(rhs, NONE)) {
			return true;
		}
		if (Objects.equals(rhs, ALL) && !Objects.equals(entity.getRace(), NONE)) {
			return true;
		}

		String[] lhsRaces;
		String lhsRaceStr = entity.getRace();
		if (lhsRaceStr.contains("&")) {
			lhsRaces = lhsRaceStr.split("&");
		} else {
			lhsRaces = new String[]{lhsRaceStr};
		}

		final List<MenagerieMogulAura> auras = SpellUtils.getAuras(gameContext, MenagerieMogulAura.class, entity);
		if (!auras.isEmpty()) {
			for (MenagerieMogulAura aura : auras) {
				if (aura.isExpired()) {
					continue;
				}

				lhsRaces = ObjectArrays.concat(lhsRaces, aura.getRaces(), String.class);
			}
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
