package net.demilich.metastone.game.statistics;


import java.io.Serializable;

public class SimulationResult implements Cloneable, Serializable {
	private final GameStatistics player1Stats = new GameStatistics();
	private final GameStatistics player2Stats = new GameStatistics();
	private final long startTimestamp;
	private long duration;
	private int numberOfGames;

	public SimulationResult(int numberOfGames) {
		this.startTimestamp = System.currentTimeMillis();
		this.numberOfGames = numberOfGames;
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


