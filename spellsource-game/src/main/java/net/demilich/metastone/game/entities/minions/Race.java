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
		if (rhs == null) {
			return false;
		}
		if (entity instanceof net.demilich.metastone.game.cards.Card card && !card.getAttributes().containsKey(net.demilich.metastone.game.cards.Attribute.RACE)) {
			var desc = card.getDesc();
			String singleRace = desc.getRace();
			if (matchesRaceSpec(singleRace != null ? singleRace : NONE, rhs)) {
				return true;
			}
			String[] descRaces = desc.getRaces();
			if (descRaces != null) {
				for (String r : descRaces) {
					if (matchesRaceSpec(r, rhs)) {
						return true;
					}
				}
			}
		} else {
			String singleRace = entity.getRace();
			if (matchesRaceSpec(singleRace, rhs)) {
				return true;
			}
			if (entity instanceof net.demilich.metastone.game.cards.Card card) {
				String[] descRaces = card.getDesc().getRaces();
				if (descRaces != null) {
					for (String r : descRaces) {
						if (matchesRaceSpec(r, rhs)) {
							return true;
						}
					}
				}
			}
		}

		if (gameContext != null) {
			int id = entity.getId();
			int sourceId = entity.getSourceCard() != null ? entity.getSourceCard().getId() : net.demilich.metastone.game.targeting.IdFactory.UNASSIGNED;
			if (id != net.demilich.metastone.game.targeting.IdFactory.UNASSIGNED
					|| sourceId != net.demilich.metastone.game.targeting.IdFactory.UNASSIGNED) {
				final List<MenagerieMogulAura> auras = SpellUtils.getAuras(gameContext, MenagerieMogulAura.class, entity);
				if (!auras.isEmpty()) {
					for (int i = 0, n = auras.size(); i < n; i++) {
						MenagerieMogulAura aura = auras.get(i);
						if (!aura.isExpired()) {
							for (String auraRace : aura.getRaces()) {
								if (matchesRaceSpec(auraRace, rhs)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private static boolean matchesRaceSpec(String lhs, String rhs) {
		if (lhs == null) {
			lhs = NONE;
		}
		if (lhs.indexOf('&') < 0) {
			return matchesSingleLhs(lhs, rhs);
		}
		int start = 0;
		int len = lhs.length();
		while (start <= len) {
			int amp = lhs.indexOf('&', start);
			String token = amp < 0 ? lhs.substring(start) : lhs.substring(start, amp);
			if (matchesSingleLhs(token, rhs)) {
				return true;
			}
			if (amp < 0) {
				break;
			}
			start = amp + 1;
		}
		return false;
	}

	private static boolean matchesSingleLhs(String lhsRace, String rhs) {
		if (ALL.equals(lhsRace) && !NONE.equals(rhs)) {
			return true;
		}
		if (ALL.equals(rhs) && !NONE.equals(lhsRace)) {
			return true;
		}
		if (rhs.indexOf('&') < 0) {
			return Objects.equals(lhsRace, rhs);
		}
		int start = 0;
		int len = rhs.length();
		while (start <= len) {
			int amp = rhs.indexOf('&', start);
			String token = amp < 0 ? rhs.substring(start) : rhs.substring(start, amp);
			if (Objects.equals(lhsRace, token)) {
				return true;
			}
			if (amp < 0) {
				break;
			}
			start = amp + 1;
		}
		return false;
	}
}
