package com.hiddenswitch.cluster.functions;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.gameconfig.GameConfig;
import net.demilich.metastone.game.gameconfig.PlayerConfig;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.statistics.SimulationResult;
import org.apache.spark.api.java.function.Function;


public class Simulator implements Function<GameConfig, SimulationResult> {
	@Override
	public SimulationResult call(GameConfig gameConfig) {
		CardCatalogue.loadCardsFromPackage();
		SimulationResult result = new SimulationResult(gameConfig);

		for (int i = 0; i < gameConfig.getNumberOfGames(); i++) {
			GameContext newGame = GameContext.fromConfig(gameConfig);

			try {
				newGame.play();

				result.getPlayer1Stats().merge(newGame.getPlayer1().getStatistics());
				result.getPlayer2Stats().merge(newGame.getPlayer2().getStatistics());
				result.calculateMetaStatistics();
			} finally {
				newGame.dispose();
			}
		}

		return result;
	}
}
