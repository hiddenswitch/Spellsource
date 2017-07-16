package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.impl.util.ServerGameContext;
import net.demilich.metastone.game.statistics.GameStatistics;
import net.demilich.metastone.game.statistics.Statistic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bberman on 11/18/16.
 */
public class DescribeGameSessionResponse implements Serializable {
	private static final long serialVersionUID = 2L;

	private String gameId;
	private GameState state;
	private Map<String, Object> statistics;

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public Map<String, Object> getStatistics() {
		return statistics;
	}

	public void setStatistics(Map<String, Object> statistics) {
		this.statistics = statistics;
	}

	public GameState getState() {
		return state;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public static DescribeGameSessionResponse fromGameContext(ServerGameContext context) {
		DescribeGameSessionResponse response = new DescribeGameSessionResponse();
		response.setGameId(context.getGameId());
		response.setStatistics(new HashMap<>());
		GameStatistics statistics = context.getPlayer1().getStatistics().clone().merge(context.getPlayer2().getStatistics().clone());
		for (Map.Entry<Statistic, Object> entry : statistics.getStats().entrySet()) {
			response.getStatistics().put(entry.getKey().toString(), entry.getValue());
		}
		response.setState(context.getGameStateCopy());
		return response;
	}
}
