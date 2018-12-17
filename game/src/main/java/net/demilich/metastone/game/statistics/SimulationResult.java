package net.demilich.metastone.game.statistics;


import net.demilich.metastone.game.GameContext;

import java.io.Serializable;

/**
 * Summarizing the results of one or more games.
 *
 * @see #SimulationResult(GameContext) for the method to create a simulation result from a single game.
 */
public class SimulationResult implements Cloneable, Serializable {
	private static final long serialVersionUID = -4060954516017134121L;
	private final GameStatistics player1Stats = new GameStatistics();
	private final GameStatistics player2Stats = new GameStatistics();
	private final long startTimestamp;
	private long duration;
	private int numberOfGames;

	public SimulationResult(int numberOfGames) {
		this.startTimestamp = System.currentTimeMillis();
		this.numberOfGames = numberOfGames;
	}

	/**
	 * Creates a simulation result from a single, completed ({@link GameContext#updateAndGetGameOver()} {@code == true})
	 * game.
	 *
	 * @param context The context to analyze.
	 */
	public SimulationResult(GameContext context) {
		this(1);
		this.getPlayer1Stats().merge(context.getPlayer1().getStatistics());
		this.getPlayer2Stats().merge(context.getPlayer2().getStatistics());
		this.calculateMetaStatistics();
	}

	public SimulationResult merge(SimulationResult other) {
		getPlayer1Stats().merge(other.getPlayer1Stats());
		getPlayer2Stats().merge(other.getPlayer2Stats());
		duration += other.getDuration();
		setNumberOfGames(other.getNumberOfGames() + numberOfGames);
		return this;
	}

	public void calculateMetaStatistics() {
		long endTimestamp = System.currentTimeMillis();
		duration = endTimestamp - startTimestamp;
	}


	public long getDuration() {
		return this.duration;
	}

	public int getNumberOfGames() {
		return numberOfGames;
	}

	public GameStatistics getPlayer1Stats() {
		return player1Stats;
	}

	public GameStatistics getPlayer2Stats() {
		return player2Stats;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[SimulationResult]\n");
		builder.append("\nplayer1Stats:\n");
		builder.append(getPlayer1Stats().toString());
		builder.append("\nplayer2Stats:\n");
		builder.append(getPlayer2Stats().toString());
		return builder.toString();
	}

	public void setNumberOfGames(int numberOfGames) {
		this.numberOfGames = numberOfGames;
	}
}


