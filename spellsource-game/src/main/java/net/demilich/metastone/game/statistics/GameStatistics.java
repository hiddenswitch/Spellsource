package net.demilich.metastone.game.statistics;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.entities.weapons.Weapon;
import net.demilich.metastone.game.logic.GameLogic;

import java.io.Serializable;
import java.util.*;

/**
 * This class collects a player's actions in a game.
 *
 * @see #merge(GameStatistics) to collect the statistics across multiple games correctly.
 * @see SimulationResult for
 */
public class GameStatistics implements Cloneable, Serializable {
	private final Map<Statistic, Object> stats = new EnumMap<>(Statistic.class);
	private final Map<String, Map<Integer, Integer>> cardsPlayed = new LinkedHashMap<>();
	private final Map<String, Integer> minionsSummoned = new LinkedHashMap<>();

	private void add(Statistic key, long value) {
		if (!stats.containsKey(key)) {
			stats.put(key, 0L);
		}
		long newValue = getLong(key) + value;
		stats.put(key, newValue);
	}

	public void armorGained(int armor) {
		add(Statistic.ARMOR_GAINED, armor);
	}

	public void heroPowerDamage(int damage) {
		add(Statistic.HERO_POWER_DAMAGE_DEALT, damage);
	}

	public void cardDrawn() {
		add(Statistic.CARDS_DRAWN, 1);
	}

	public void cardDiscarded() {
		add(Statistic.CARDS_DISCARDED, 1);
	}

	public void cardPlayed(Card card, int turn) {
		add(Statistic.CARDS_PLAYED, 1);

		switch (card.getCardType()) {
			case HERO_POWER:
				add(Statistic.HERO_POWER_USED, 1);
				break;
			case MINION:
				add(Statistic.MINIONS_PLAYED, 1);
				break;
			case SPELL:
			case CHOOSE_ONE:
				add(Statistic.SPELLS_CAST, 1);
				break;
			case WEAPON:
				add(Statistic.WEAPONS_PLAYED, 1);
			case HERO:
				break;
			case GROUP:
				break;
			case CLASS:
				break;
			case FORMAT:
				break;
		}
		increaseCardCount(card, turn);
	}

	public GameStatistics clone() {
		GameStatistics clone = new GameStatistics();
		clone.stats.putAll(stats);
		clone.cardsPlayed.putAll(getCardsPlayed());
		for (var kv : cardsPlayed.entrySet()) {
			var m = new LinkedHashMap<Integer, Integer>();
			for (var kv1 : kv.getValue().entrySet()) {
				m.put(kv1.getKey(), kv1.getValue());
			}
			clone.cardsPlayed.put(kv.getKey(), m);
		}
		clone.getMinionsSummoned().putAll(getMinionsSummoned());
		return clone;
	}

	public boolean contains(Statistic key) {
		return stats.containsKey(key);
	}

	public void damageDealt(int damage) {
		add(Statistic.DAMAGE_DEALT, damage);
	}

	public void equipWeapon(Weapon weapon) {
		add(Statistic.WEAPONS_EQUIPPED, 1);
	}

	public void fatigueDamage(int fatigueDamage) {
		add(Statistic.FATIGUE_DAMAGE, fatigueDamage);
	}

	public void gameLost() {
		add(Statistic.GAMES_LOST, 1);
		updateWinRate();
	}

	public void gameWon() {
		add(Statistic.GAMES_WON, 1);
		updateWinRate();
	}

	public Object get(Statistic key) {
		return stats.get(key);
	}

	public Map<String, Map<Integer, Integer>> getCardsPlayed() {
		return cardsPlayed;
	}

	public Map<String, Integer> getMinionsSummoned() {
		return minionsSummoned;
	}

	public double getDouble(Statistic key) {
		return stats.containsKey(key) ? (double) stats.get(key) : 0.0;
	}

	public long getLong(Statistic key) {
		return stats.containsKey(key) ? (long) stats.get(key) : 0L;
	}

	public void heal(int healing) {
		add(Statistic.HEALING_DONE, healing);
	}

	public void loseArmor(int armorLost) {
		add(Statistic.ARMOR_LOST, armorLost);
	}

	private void increaseCardCount(Card card, int turn) {
		if (GameLogic.isCardType(card.getCardType(), CardType.HERO_POWER)) {
			return;
		}
		String cardId = card.getCardId();
		if (!getCardsPlayed().containsKey(cardId)) {
			getCardsPlayed().put(cardId, new LinkedHashMap<>());
		}
		if (!getCardsPlayed().get(cardId).containsKey(turn)) {
			getCardsPlayed().get(cardId).put(turn, 0);
		}
		getCardsPlayed().get(cardId).put(turn, getCardsPlayed().get(cardId).get(turn) + 1);
	}

	private void increaseMinionCount(Minion minion) {
		String cardId = minion.getSourceCard().getCardId();
		if (!getMinionsSummoned().containsKey(cardId)) {
			getMinionsSummoned().put(cardId, 0);
		}
		getMinionsSummoned().put(cardId, getMinionsSummoned().get(cardId) + 1);
	}

	public void manaSpent(int mana) {
		add(Statistic.MANA_SPENT, mana);
	}

	public GameStatistics merge(GameStatistics otherStatistics) {
		for (Statistic stat : otherStatistics.stats.keySet()) {
			Object value = get(stat);
			if (value != null) {
				if (value instanceof Long) {
					add(stat, otherStatistics.getLong(stat));
				}
			} else {
				stats.put(stat, otherStatistics.get(stat));
			}
		}
		for (String cardId : otherStatistics.getCardsPlayed().keySet()) {
			if (!getCardsPlayed().containsKey(cardId)) {
				getCardsPlayed().put(cardId, new LinkedHashMap<Integer, Integer>());
			}
			for (int turn : otherStatistics.getCardsPlayed().get(cardId).keySet()) {
				if (!getCardsPlayed().get(cardId).containsKey(turn)) {
					getCardsPlayed().get(cardId).put(turn, 0);
				}
				getCardsPlayed().get(cardId).put(turn, getCardsPlayed().get(cardId).get(turn) + otherStatistics.getCardsPlayed().get(cardId).get(turn));
			}
		}
		updateWinRate();
		return this;
	}

	public void minionSummoned(Minion minion) {
		add(Statistic.MINIONS_PLAYED, 1);

		increaseMinionCount(minion);
	}

	public long getTurnsTaken() {
		return getLong(Statistic.TURNS_TAKEN);
	}

	public void set(Statistic key, Object value) {
		stats.put(key, value);
	}

	public void startTurn() {
		add(Statistic.TURNS_TAKEN, 1);
	}

	public void endTurn(GameContext context) {
		set(Statistic.LAST_TURN, (long) context.getTurn());
	}

	/**
	 * The last turn the player took, or {@code -1L} if the player has not taken a turn yet.
	 *
	 * @return
	 */
	public long getLastTurn() {
		return (long) stats.getOrDefault(Statistic.LAST_TURN, -1L);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[GameStatistics]\n");
		for (Statistic stat : stats.keySet()) {
			builder.append(stat);
			builder.append(": ");
			builder.append(stats.get(stat));
			builder.append("\n");
		}
		return builder.toString();
	}

	private void updateWinRate() {
		double winRate = getLong(Statistic.GAMES_WON) / (double) (getLong(Statistic.GAMES_WON) + getLong(Statistic.GAMES_LOST));
		set(Statistic.WIN_RATE, winRate);
	}

	public Map<Statistic, Object> getStats() {
		return Collections.unmodifiableMap(stats);
	}
}
